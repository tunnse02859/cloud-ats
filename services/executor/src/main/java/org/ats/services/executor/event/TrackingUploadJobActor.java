/**
 * 
 */
package org.ats.services.executor.event;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ats.common.ssh.SSHClient;
import org.ats.jenkins.JenkinsMaster;
import org.ats.jenkins.JenkinsMavenJob;
import org.ats.services.event.Event;
import org.ats.services.event.EventFactory;
import org.ats.services.executor.ExecutorUploadService;
import org.ats.services.executor.job.AbstractJob;
import org.ats.services.executor.job.AbstractJob.Status;
import org.ats.services.executor.job.KeywordUploadJob;
import org.ats.services.generator.GeneratorService;
import org.ats.services.iaas.IaaSServiceProvider;
import org.ats.services.organization.entity.reference.SpaceReference;
import org.ats.services.organization.entity.reference.TenantReference;
import org.ats.services.performance.PerformanceProjectService;
import org.ats.services.upload.KeywordUploadProject;
import org.ats.services.upload.KeywordUploadProjectService;
import org.ats.services.vmachine.VMachine;
import org.ats.services.vmachine.VMachineService;

import akka.actor.UntypedActor;

import com.google.inject.Inject;

/**
 * @author NamBV2
 *
 * Sep 18, 2015
 */
public class TrackingUploadJobActor extends UntypedActor {

  @Inject ExecutorUploadService executorUploadService;
  
  @Inject GeneratorService generatorService;
  
  @Inject KeywordUploadProjectService keywordUploadService;
  
  @Inject PerformanceProjectService perfService;
  
  @Inject VMachineService vmachineService;
  
  @Inject IaaSServiceProvider iaasProvider;
  
  @Inject EventFactory eventFactory;
  
  @Inject Logger logger;
  
  ConcurrentHashMap<String, JenkinsMavenJob> cache = new ConcurrentHashMap<String, JenkinsMavenJob>();
  
  @Override
  public void onReceive(Object obj) throws Exception {
    if (obj instanceof Event) {
      Event event = (Event) obj;
      if ("upload-job-tracking".equals(event.getName())) {
        KeywordUploadJob job = (KeywordUploadJob) event.getSource();
        processKeywordUploadJob(job);
      }
    }
  }

  private void processKeywordUploadJob(KeywordUploadJob job) throws Exception {
    try {
      KeywordUploadProject project = keywordUploadService.get(job.getProjectId(),"raw");
      switch (job.getStatus()) {
      case Queued:
        doExecuteKeywordUploadJob(job, project);
      case Running:
        doTrackingKeywordUploadJob(job, project);
        break;
      default:
        break;
      }
    } catch (Exception e) {
      job.setStatus(Status.Completed);
      executorUploadService.update(job);
      
      VMachine vm = vmachineService.get(job.getTestVMachineId());
      if (vm != null) {
        vm.setStatus(VMachine.Status.Started);
        if (vm.getPublicIp() != null) iaasProvider.get().deallocateFloatingIp(vm);
        else vmachineService.update(vm);
      }
      
      KeywordUploadProject project = keywordUploadService.get(job.getProjectId(),"raw");
      project.setStatus(KeywordUploadProject.Status.READY);
      keywordUploadService.update(project);
      
      //bubble event
      Event event = eventFactory.create(job, "upload-job-tracking");
      event.broadcast();
      throw e;
    }
  }
  
private void doTrackingKeywordUploadJob(KeywordUploadJob job, KeywordUploadProject project) throws Exception {
    TenantReference tenant = project.getTenant();
    SpaceReference space = project.getSpace();
    
    VMachine systemVM = vmachineService.getSystemVM(tenant, space);
    VMachine testVM = vmachineService.get(job.getTestVMachineId());
    
    JenkinsMaster jkMaster = new JenkinsMaster(systemVM.getPublicIp(), "http", "jenkins", 8080);
    JenkinsMavenJob jkJob = cache.get(job.getId());
    if (jkJob == null) {
      jkJob = new JenkinsMavenJob(jkMaster, job.getId(), null, null, null);
      cache.put(job.getId(), jkJob);
    }
    
    boolean isBuilding = jkJob.isBuilding(1, System.currentTimeMillis(), 60 * 1000);
    if (isBuilding) {
      
      updateLog(job, jkJob);
      
      Thread.sleep(5000L);
      
      Event event = eventFactory.create(job, "upload-job-tracking");
      event.broadcast();
      
    } else {
      updateLog(job, jkJob);
      
      //Download result
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      SSHClient.getFile(testVM.getPublicIp(), 22, "cloudats", "#CloudATS", 
          "/home/cloudats/projects/" + job.getId() + "/target/surefire-reports/testng-results.xml",  bos);
      
      if (bos.size() > 0)
        job.put("report", new String(bos.toByteArray()));
      //End download result

      job.setStatus(AbstractJob.Status.Completed);
      executorUploadService.update(job);
      
      //Reset vm status and release floating ip
      testVM.setStatus(VMachine.Status.Started);
      vmachineService.update(testVM);
      
      //TODO:Need review it
      // testVM = openstackService.deallocateFloatingIp(testVM);
      
      project.setStatus(KeywordUploadProject.Status.READY);
      keywordUploadService.update(project);
      
      jkJob.delete();
      cache.remove(job.getId());
      
      Event event = eventFactory.create(job, "upload-job-tracking");
      event.broadcast();
    }
  }
  
  private void doExecuteKeywordUploadJob(KeywordUploadJob job, KeywordUploadProject project) throws Exception {

    VMachine jenkinsVM = vmachineService.getSystemVM(project.getTenant(), project.getSpace());
    VMachine testVM = vmachineService.getTestVMAvailabel(project.getTenant(), project.getSpace(), true);
    if (testVM == null) {
      testVM = iaasProvider.get().createTestVM(project.getTenant(), project.getSpace(), true);
      //Sleep 15s after creating new vm to make sure system be stable
      Thread.sleep(15 * 1000);
    }
    
    if (testVM.getPublicIp() == null) {
      testVM = iaasProvider.get().allocateFloatingIp(testVM);
      Thread.sleep(15 * 1000);
      SSHClient.checkEstablished(testVM.getPublicIp(), 22, 300);
      logger.log(Level.INFO, "Connection to  " + testVM.getPublicIp() + " is established");
    }
    byte [] bFile = project.getRawData();
    String fileName = job.getId();
    String path = "/tmp/"+fileName+".zip";
    FileOutputStream fileOut = new FileOutputStream(path);
    fileOut.write(bFile);
    fileOut.close();

    SSHClient.sendFile(testVM.getPublicIp(), 22, "cloudats", "#CloudATS", "/home/cloudats/projects/"+fileName, fileName + ".zip", new File(path));
    
    Thread.sleep(3000);
    SSHClient.execCommand(testVM.getPublicIp(), 22, "cloudats", "#CloudATS", 
        "cd /home/cloudats/projects/"+fileName+" && unzip " +  fileName + ".zip", null, null);
    StringBuilder goalsBuilder = new StringBuilder("clean test");

    JenkinsMaster jenkinsMaster = new JenkinsMaster(jenkinsVM.getPublicIp(), "http", "/jenkins", 8080);
    JenkinsMavenJob jenkinsJob = new JenkinsMavenJob(jenkinsMaster, job.getId(), 
        testVM.getPrivateIp() , "/home/cloudats/projects/" + fileName + "/pom.xml", goalsBuilder.toString());
    jenkinsJob.submit();

    testVM.setStatus(VMachine.Status.InProgress);
    vmachineService.update(testVM);

    job.setStatus(Status.Running);
    job.setVMachineId(testVM.getId());
    executorUploadService.update(job);
    
    Event event = eventFactory.create(job, "upload-job-tracking");
    event.broadcast();
  }
  
  private void updateLog(AbstractJob<?> job, JenkinsMavenJob jkJob) throws IOException {
    int start = job.getLog() == null ? 0 : job.getLog().length();
    byte[] bytes = jkJob.getConsoleOutput(1, start); 
    byte[] next = new byte[bytes.length - start];
    System.arraycopy(bytes, start, next, 0, next.length);
    if (next.length > 0) {
      job.appendLog(new String(next));
      executorUploadService.update(job);
    }
  }
}
