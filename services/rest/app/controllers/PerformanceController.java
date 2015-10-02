/**
 * TrinhTV3@fsoft.com.vn
 */
package controllers;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.ats.common.MapBuilder;
import org.ats.common.PageList;
import org.ats.jenkins.JenkinsMaster;
import org.ats.jenkins.JenkinsMavenJob;
import org.ats.service.report.Report;
import org.ats.service.report.ReportService;
import org.ats.service.report.ReportService.Type;
import org.ats.services.OrganizationContext;
import org.ats.services.executor.ExecutorService;
import org.ats.services.executor.job.AbstractJob;
import org.ats.services.executor.job.PerformanceJob;
import org.ats.services.organization.acl.Authenticated;
import org.ats.services.organization.entity.Tenant;
import org.ats.services.organization.entity.fatory.ReferenceFactory;
import org.ats.services.performance.JMeterScriptReference;
import org.ats.services.performance.JMeterScriptService;
import org.ats.services.performance.PerformanceProject;
import org.ats.services.performance.PerformanceProjectFactory;
import org.ats.services.performance.PerformanceProjectService;
import org.ats.services.vmachine.VMachine;
import org.ats.services.vmachine.VMachineService;

import play.Logger;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import actions.CorsComposition;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

/**
 * @author TrinhTV3
 *
 */
@CorsComposition.Cors
@Authenticated
public class PerformanceController extends Controller {
  
  @Inject
  private JMeterScriptService jmeterService;
  
  @Inject
  private PerformanceProjectFactory projectFactory;
  
  @Inject
  private PerformanceProjectService projectService;
  
  @Inject
  private ReferenceFactory<JMeterScriptReference> jmeterReferenceFactory;
  
  @Inject OrganizationContext context;
  
  @Inject ExecutorService executorService;
  
  @Inject ReportService reportService;
  
  @Inject VMachineService vmachineService;
  private SimpleDateFormat formater = new SimpleDateFormat("dd/MM/yyyy HH:mm");
  
  public Result list() {
    Tenant currentTenant = context.getTenant();
    PageList<PerformanceProject> list = projectService.query(new BasicDBObject("tenant", new BasicDBObject("_id", currentTenant.getId())));
    ArrayNode array = Json.newObject().arrayNode();
    
    while(list.hasNext()) {
      for (PerformanceProject project : list.next()) {
        project.put("type", "performance");
        project.put("totalScripts", jmeterService.getJmeterScripts(project.getId()).count());
        
        PageList<AbstractJob<?>> pages = executorService.query(new BasicDBObject("project_id", project.getId()).append("status", AbstractJob.Status.Completed.toString()));
        pages.setSortable(new MapBuilder<String, Boolean>("created_date", false).build());
        if (pages.count() > 0) {
          
          AbstractJob<?> lastJob = pages.next().get(0);
          
          project.put("lastRunning", formater.format(lastJob.getCreatedDate()));
          project.put("lastJobId", lastJob.getId());
          List<JMeterScriptReference> scripts = ((PerformanceJob) lastJob).getScripts();
          if (scripts.size() > 0) {
            BasicDBList lastScripts = new BasicDBList();
            for (JMeterScriptReference script : scripts) lastScripts.add(script.toJSon());
            project.put("lastScripts", lastScripts);
          }
        }
        array.add(Json.parse(project.toString()));
      }
    }
    
    return ok(array);
  }
  
  public Result viewLog(String projectId) {
    
    PageList<AbstractJob<?>> pages = executorService.query(new BasicDBObject("project_id", projectId).append("status", AbstractJob.Status.Completed.toString()));
    pages.setSortable(new MapBuilder<String, Boolean>("created_date", false).build());
   
    String log = "";
    if (pages.count() > 0) {
      
      AbstractJob<?> lastJob = pages.next().get(0);
      log = lastJob.getLog();
    }
    
    return status(200, log);
  }
  
  public Result create() {
    
    JsonNode json = request().body().asJson();
    String name = json.get("name").asText();
    
    PerformanceProject project = projectFactory.create(name);
    projectService.create(project);
    return ok(project.getId());
  }
  
  public Result get(String projectId) {
    
    PerformanceProject project = projectService.get(projectId);
    if (project == null) return status(404);
    
    PageList<AbstractJob<?>> jobList = executorService.query(new BasicDBObject("project_id", project.getId()), 1);
    jobList.setSortable(new MapBuilder<String, Boolean>("created_date", false).build());
    
    if (jobList.count() > 0) {
      AbstractJob<?> lastRunningJob = jobList.next().get(0);
      project.put("last_running", formater.format(lastRunningJob.getCreatedDate()));
      project.put("lastScripts", lastRunningJob.get("scripts"));
      project.put("log", true);
     
    }
    
    PageList<AbstractJob<?>> jobsList = executorService.query(new BasicDBObject("project_id", project.getId()), 10);
    
    jobsList.setSortable(new MapBuilder<String, Boolean>("created_date", false).build());
    if (jobsList.totalPage() > 0) {
      ArrayNode arrayJob = Json.newObject().arrayNode();
      while (jobsList.hasNext()) {
        List<AbstractJob<?>> listJobs = jobsList.next();
        ObjectNode object;
        for (AbstractJob<?> job : listJobs) {
          object = Json.newObject();
          object.put("created_date", formater.format(job.getCreatedDate()));
          object.put("creator", "Cloud-ATS");
          object.put("scripts", ((PerformanceJob) job).getScripts().size());
          object.put("status", job.getStatus().toString());
          object.put("_id", job.getId());
          arrayJob.add(object);
        }
      }
      project.put("jobs", arrayJob.toString());
    }
    project.put("type", "performance");
    project.put("totalScripts", jmeterService.getJmeterScripts(projectId).count());
    return ok(Json.parse(project.toString()));
  }
  
  public Result update() {
    
    JsonNode data = request().body().asJson();
    String id = data.get("id").asText();
    String name = data.get("name").asText();
    
    PerformanceProject project = projectService.get(id);
    
    if (name.equals(project.getName())) {
      return status(304);
    }
    
    project.put("name", name);
    projectService.update(project);

    return status(202, id);
  }
  
  public Result delete() {
    
    String id = request().body().asText();
    
    PerformanceProject project = projectService.get(id);
    
    if (project == null) {
      return status(404);
    }
    
    PageList<AbstractJob<?>> listJob = executorService.query(new BasicDBObject("project_id", id));
    
    while (listJob.hasNext()) {
      List<AbstractJob<?>> list = listJob.next();
      for (AbstractJob<?> job : list) {
        reportService.deleteBy(new BasicDBObject("performane_job_id", job.getId()));
      }
    }
    
    executorService.deleteBy(new BasicDBObject("project_id", id));
    jmeterService.deleteBy(new BasicDBObject("project_id", id));
    projectService.delete(id);
    
    return status(200);
  }
  
  public Result run(String projectId) throws Exception {
    
    JsonNode data = request().body().asJson();
    
    List<JMeterScriptReference> scripts = new ArrayList<JMeterScriptReference>(); 
    JMeterScriptReference ref;
    for (JsonNode json : data) {
      
      ref = jmeterReferenceFactory.create(json.asText());
      
      if (jmeterService.get(ref.getId()) == null) {
        return status(400);
      }
      scripts.add(ref);
    }
    
    PerformanceProject project = projectService.get(projectId);
    if (project == null) {
      return status(404);
    }
    
    if (project.getStatus() == PerformanceProject.Status.RUNNING) {
      return status(204);
    }
    
    PerformanceJob job = executorService.execute(project, scripts);
    
    ObjectNode node = (ObjectNode) Json.parse(job.toString());
    node.put("created_date", formater.format(job.getCreatedDate()));
    
    return status(200, node);
  }
  
  public Result report(String projectId, String jobId) {
    
    PerformanceJob job = (PerformanceJob) executorService.get(jobId);
    
    ArrayNode totals = Json.newObject().arrayNode();
    
    for (JMeterScriptReference script : job.getScripts()) {
      ArrayNode array = Json.newObject().arrayNode();
      
      try {
        PageList<Report> pages = reportService.getList(jobId, Type.PERFORMANCE, script.getId());
        while (pages.hasNext()) {
          List<Report> list = pages.next();
          
          for (Report report : list) {
            
            if (report.getScriptId() != null) {
              report.put("script_name", script.get().getName());
            }
            
            array.add(Json.parse(report.toString()));         
          }
        }
        totals.add(array);
      } catch (Exception e) {
        
        Logger.info("Can not read reports with this job");
        
        return status(404, jobId);
      }
    }
    
    return status(200, totals);
  }
  
  public Result getReport(String id) {
    
    Report report = reportService.get(id);
    return status(200, Json.parse(report.toString()));
  }
  
public Result stopProject(String projectId) throws IOException {
    
    PerformanceProject project = projectService.get(projectId);
    
    if (project == null) return status(404);
    
    VMachine jenkinsVM = vmachineService.getSystemVM(project.getTenant(), project.getSpace());
    
    PageList<AbstractJob<?>> jobList = executorService.query(new BasicDBObject("project_id", projectId), 1);
    jobList.setSortable(new MapBuilder<String, Boolean>("created_date", false).build());
    
    AbstractJob<?> lastJob = jobList.next().get(0);
    
    JenkinsMaster jenkinsMaster = new JenkinsMaster(jenkinsVM.getPublicIp(), "http", "/jenkins", 8080);
    JenkinsMavenJob jenkinsJob = new JenkinsMavenJob(jenkinsMaster, lastJob.getId(), 
     null , null, null);
    
    lastJob.setStatus(AbstractJob.Status.Completed);
    executorService.update(lastJob);
    
    if (lastJob.getTestVMachineId() != null) {
      VMachine testVM = vmachineService.get(lastJob.getTestVMachineId());
      
      if (testVM.getStatus() == VMachine.Status.InProgress) {
        testVM.setStatus(VMachine.Status.Started);
        vmachineService.update(testVM);
      }
    } 
    
    project.setStatus(PerformanceProject.Status.READY);
    projectService.update(project);
    
    jenkinsJob.stop();
    
    return status(200, Json.parse(lastJob.toString()));
  }
}
