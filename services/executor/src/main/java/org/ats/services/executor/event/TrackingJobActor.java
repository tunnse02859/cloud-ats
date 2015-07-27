/**
 * 
 */
package org.ats.services.executor.event;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.ats.common.ssh.SSHClient;
import org.ats.jenkins.JenkinsMaster;
import org.ats.jenkins.JenkinsMavenJob;
import org.ats.services.event.Event;
import org.ats.services.event.EventFactory;
import org.ats.services.executor.ExecutorService;
import org.ats.services.executor.job.AbstractJob;
import org.ats.services.executor.job.KeywordJob;
import org.ats.services.executor.job.PerformanceJob;
import org.ats.services.iaas.openstack.OpenStackService;
import org.ats.services.keyword.KeywordProject;
import org.ats.services.keyword.KeywordProjectService;
import org.ats.services.organization.entity.reference.SpaceReference;
import org.ats.services.organization.entity.reference.TenantReference;
import org.ats.services.performance.JMeterScriptReference;
import org.ats.services.performance.PerformanceProject;
import org.ats.services.performance.PerformanceProjectService;
import org.ats.services.vmachine.VMachine;
import org.ats.services.vmachine.VMachineService;

import akka.actor.UntypedActor;

import com.google.inject.Inject;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Jul 23, 2015
 */
public class TrackingJobActor extends UntypedActor {

  @Inject ExecutorService executorService;
  
  @Inject KeywordProjectService keywordService;
  
  @Inject PerformanceProjectService perfService;
  
  @Inject VMachineService vmachineService;
  
  @Inject OpenStackService openstackService;
  
  @Inject EventFactory eventFactory;
  
  @Inject Logger logger;
  
  ConcurrentHashMap<String, JenkinsMavenJob> cache = new ConcurrentHashMap<String, JenkinsMavenJob>();
  
  @Override
  public void onReceive(Object obj) throws Exception {
    if (obj instanceof Event) {
      Event event = (Event) obj;
      if ("keyword-job-tracking".equals(event.getName())) {
        KeywordJob job = (KeywordJob) event.getSource();
        processKeywordJob(job);
      } else if ("performance-job-tracking".equals(event.getName())) {
        PerformanceJob job = (PerformanceJob) event.getSource();
        processPerformanceJob(job);
      }
    }
  }

  private void processPerformanceJob(PerformanceJob job) throws Exception {
    PerformanceProject project = perfService.get(job.getProjectId());
    TenantReference tenant = project.getTenant();
    SpaceReference space = project.getSpace();
    
    VMachine systemVM = vmachineService.getSystemVM(tenant, space);
    JenkinsMaster jkMaster = new JenkinsMaster(systemVM.getPublicIp(), "http", "jenkins", 8080);
    JenkinsMavenJob jkJob = cache.get(job.getId());
    if (jkJob == null) {
      jkJob = new JenkinsMavenJob(jkMaster, job.getId(), null, null, null);
      cache.put(job.getId(), jkJob);
    }
    
    boolean isBuilding = jkJob.isBuilding(1, System.currentTimeMillis(), 60 * 1000);
    if (isBuilding) {
      updateLog(job, jkJob);
      
      Thread.sleep(15000L);
      
      Event event  = eventFactory.create(job, "performance-job-tracking");
      event.broadcast();
    } else {
      updateLog(job, jkJob);
      
      //Download result
      BasicDBList list = new BasicDBList();
      for (JMeterScriptReference ref : job.getScripts()) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        SSHClient.getFile(systemVM.getPublicIp(), 22, "cloudats", "#CloudATS", 
            "/home/cloudats/projects/" + job.getId() + "/target/" + ref.getId() + ".jtl",  bos);
        list.add(new BasicDBObject("_id", ref.getId()).append("content", new String(bos.toByteArray())));
      }
      job.put("report", list);
      //End download result

      job.setStatus(AbstractJob.Status.Completed);
      executorService.update(job);
      
      VMachine testVM = vmachineService.get(job.getTestVMachineId());
      testVM.setStatus(VMachine.Status.Started);
      vmachineService.update(testVM);
      
      jkJob.delete();
      cache.remove(job.getId());
    }
  }

  private void processKeywordJob(KeywordJob job) throws Exception {
    KeywordProject project = keywordService.get(job.getProjectId());
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
      
      Thread.sleep(3000L);
      
      Event event  = eventFactory.create(job, "keyword-job-tracking");
      event.broadcast();
    } else {
      updateLog(job, jkJob);
      
      testVM = openstackService.allocateFloatingIp(testVM);
      
      //Download result
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      SSHClient.getFile(testVM.getPublicIp(), 22, "cloudats", "#CloudATS", 
          "/home/cloudats/projects/" + job.getId() + "/target/surefire-reports/testng-results.xml",  bos);
      
      job.put("report", new String(bos.toByteArray()));
      //End download result

      testVM = openstackService.deallocateFloatingIp(testVM);
      
      job.setStatus(AbstractJob.Status.Completed);
      executorService.update(job);
      testVM.setStatus(VMachine.Status.Started);
      vmachineService.update(testVM);
      
      jkJob.delete();
      cache.remove(job.getId());
    }
  }
  
  private void updateLog(AbstractJob<?> job, JenkinsMavenJob jkJob) throws IOException {
    int start = job.getLog() == null ? 0 : job.getLog().length();
    byte[] bytes = jkJob.getConsoleOutput(1, start); 
    byte[] next = new byte[bytes.length - start];
    System.arraycopy(bytes, start, next, 0, next.length);
    if (next.length > 0) {
      job.appendLog(new String(next));
      executorService.update(job);
    }
  }
}
