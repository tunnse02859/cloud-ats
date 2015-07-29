/**
 * 
 */
package org.ats.services.executor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.ats.common.StringUtil;
import org.ats.common.ssh.SSHClient;
import org.ats.jenkins.JenkinsMaster;
import org.ats.jenkins.JenkinsMavenJob;
import org.ats.services.data.MongoDBService;
import org.ats.services.event.Event;
import org.ats.services.event.EventFactory;
import org.ats.services.executor.job.AbstractJob;
import org.ats.services.executor.job.AbstractJob.Status;
import org.ats.services.executor.job.KeywordJob;
import org.ats.services.executor.job.KeywordJobFactory;
import org.ats.services.executor.job.PerformanceJob;
import org.ats.services.executor.job.PerformanceJobFactory;
import org.ats.services.generator.GeneratorService;
import org.ats.services.iaas.CreateVMException;
import org.ats.services.iaas.openstack.OpenStackService;
import org.ats.services.keyword.KeywordProject;
import org.ats.services.keyword.Suite;
import org.ats.services.keyword.SuiteReference;
import org.ats.services.organization.base.AbstractMongoCRUD;
import org.ats.services.organization.entity.fatory.ReferenceFactory;
import org.ats.services.performance.JMeterScriptReference;
import org.ats.services.performance.PerformanceProject;
import org.ats.services.vmachine.VMachine;
import org.ats.services.vmachine.VMachineService;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.jcraft.jsch.JSchException;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Jul 21, 2015
 */
@Singleton
public class ExecutorService extends AbstractMongoCRUD<AbstractJob<?>> {
  
  @Inject
  private GeneratorService generatorService;
  
  @Inject
  private OpenStackService openstackService;
  
  @Inject
  private VMachineService vmachineService;
  
  @Inject
  private KeywordJobFactory keywordFactory;
  
  @Inject
  private PerformanceJobFactory perfFactory;
  
  @Inject
  private ReferenceFactory<JMeterScriptReference> jmeterRefFactory;
  
  @Inject
  private EventFactory eventFactory;
  
  private final String COL_NAME = "job";
  
  @Inject
  public ExecutorService(MongoDBService mongoService, Logger logger) {
    this.col = mongoService.getDatabase().getCollection(COL_NAME);
    this.logger = logger;
  }

  public PerformanceJob execute(PerformanceProject project, List<JMeterScriptReference> scripts) throws IOException, JSchException, CreateVMException, InterruptedException {
    VMachine jenkinsVM = vmachineService.getSystemVM(project.getTenant(), project.getSpace());
    VMachine testVM = vmachineService.getTestVMAvailabel(project.getTenant(), project.getSpace(), false);
    if (testVM == null) {
      testVM = openstackService.createTestVM(project.getTenant(), project.getSpace(), false);
      //Sleep 15s after creating new vm to make sure system be stable
      Thread.sleep(15 * 1000);
    }
    
    String path = generatorService.generate("/tmp", project, true, scripts);
    String projectHash = project.getId().substring(0, 8);
    
    SSHClient.sendFile(jenkinsVM.getPublicIp(), 22, "cloudats", "#CloudATS", "/home/cloudats/projects", projectHash + ".zip", new File(path));
    SSHClient.execCommand(jenkinsVM.getPublicIp(), 22, "cloudats", "#CloudATS", 
        "cd /home/cloudats/projects && unzip " +  projectHash + ".zip", null, System.out);
    
    StringBuilder goalsBuilder = new StringBuilder("clean test ").append(" -Djmeter.hosts=").append(testVM.getPrivateIp());
    
    JenkinsMaster jenkinsMaster = new JenkinsMaster(jenkinsVM.getPublicIp(), "http", "/jenkins", 8080);
    JenkinsMavenJob jenkinsJob = new JenkinsMavenJob(jenkinsMaster, projectHash, 
        null , "/home/cloudats/projects/" + projectHash + "/pom.xml", goalsBuilder.toString());
    
    jenkinsJob.submit();

    testVM.setStatus(VMachine.Status.InProgress);
    vmachineService.update(testVM);
    
    PerformanceJob job = perfFactory.create(projectHash, project.getId(), testVM.getId(), Status.Running, scripts);
    create(job);
    
    Event event = eventFactory.create(job, "performance-job-tracking");
    event.broadcast();
    //
    return job;
  }
  
  public KeywordJob execute(KeywordProject project, List<SuiteReference> suites) throws IOException, JSchException, CreateVMException, InterruptedException {
    VMachine jenkinsVM = vmachineService.getSystemVM(project.getTenant(), project.getSpace());
    VMachine testVM = vmachineService.getTestVMAvailabel(project.getTenant(), project.getSpace(), true);
    if (testVM == null) {
      testVM = openstackService.createTestVM(project.getTenant(), project.getSpace(), true);
      //Sleep 15s after creating new vm to make sure system be stable
      Thread.sleep(15 * 1000);
    }
    
    String path = generatorService.generate("/tmp", project, true);
    testVM = openstackService.allocateFloatingIp(testVM);
    
    String projectHash = project.getId().substring(0, 8);
    
    SSHClient.sendFile(testVM.getPublicIp(), 22, "cloudats", "#CloudATS", "/home/cloudats/projects", projectHash + ".zip", new File(path));
    SSHClient.execCommand(testVM.getPublicIp(), 22, "cloudats", "#CloudATS", 
        "cd /home/cloudats/projects && unzip " +  projectHash + ".zip", null, null);
    
    testVM = openstackService.deallocateFloatingIp(testVM);
    
    StringBuilder goalsBuilder = new StringBuilder("clean test -Dtest=");
    for (Iterator<SuiteReference> i = suites.iterator(); i.hasNext(); ) {
      Suite suite = i.next().get();
      goalsBuilder.append(StringUtil.normalizeName(suite.getString("suite_name")));
      if (i.hasNext()) goalsBuilder.append(',');
    }
    
    JenkinsMaster jenkinsMaster = new JenkinsMaster(jenkinsVM.getPublicIp(), "http", "/jenkins", 8080);
    JenkinsMavenJob jenkinsJob = new JenkinsMavenJob(jenkinsMaster, projectHash, 
        testVM.getPrivateIp() , "/home/cloudats/projects/" + projectHash + "/pom.xml", goalsBuilder.toString());
    
    jenkinsJob.submit();

    testVM.setStatus(VMachine.Status.InProgress);
    vmachineService.update(testVM);
    
    KeywordJob job = keywordFactory.create(projectHash, project.getId(), testVM.getId(), Status.Running);
    create(job);
    
    Event event = eventFactory.create(job, "keyword-job-tracking");
    event.broadcast();
    //
    return job;
  }

  @Override
  public AbstractJob<?> transform(DBObject source) {
    String id = (String) source.get("_id");
    String projectId = (String) source.get("project_id");
    String vmachineId = (String) source.get("vm_id");
    AbstractJob.Status status = AbstractJob.Status.valueOf((String) source.get("status"));
    Date createdDate = (Date) source.get("created_date");
    AbstractJob.Type type = AbstractJob.Type.valueOf((String) source.get("type"));

    String projectHash = projectId.substring(0, 8);
    switch (type) {
    case Keyword:
      KeywordJob keywordJob = keywordFactory.create(projectHash, projectId, vmachineId, status);
      keywordJob.put("_id", id);
      keywordJob.put("created_date", createdDate);
      keywordJob.put("report", source.get("report"));
      keywordJob.put("log", source.get("log"));
      return keywordJob;
    case Performance:
      BasicDBList list = (BasicDBList) source.get("scripts");
      List<JMeterScriptReference> scripts = new ArrayList<JMeterScriptReference>();
      for (Object obj : list) {
        BasicDBObject dbObj = (BasicDBObject) obj;
        scripts.add(jmeterRefFactory.create(dbObj.getString("_id")));
      }
      
      PerformanceJob perfJob = perfFactory.create(projectHash, projectId, vmachineId, status, scripts);
      perfJob.put("_id", id);
      perfJob.put("created_date", createdDate);
      perfJob.put("report", source.get("report"));
      perfJob.put("log", source.get("log"));
      return perfJob;
    default:
      throw new IllegalArgumentException();
    }
  }
}
