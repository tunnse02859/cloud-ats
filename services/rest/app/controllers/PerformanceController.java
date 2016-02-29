/**
 * TrinhTV3@fsoft.com.vn
 */
package controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.ats.common.MapBuilder;
import org.ats.common.PageList;
import org.ats.common.ssh.SSHClient;
import org.ats.jenkins.JenkinsMaster;
import org.ats.jenkins.JenkinsMavenJob;
import org.ats.service.report.Report;
import org.ats.service.report.ReportService;
import org.ats.service.report.ReportService.Type;
import org.ats.services.OrganizationContext;
import org.ats.services.executor.ExecutorService;
import org.ats.services.executor.job.AbstractJob;
import org.ats.services.executor.job.PerformanceJob;
import org.ats.services.iaas.IaaSService;
import org.ats.services.iaas.IaaSServiceProvider;
import org.ats.services.organization.acl.Authenticated;
import org.ats.services.organization.entity.Tenant;
import org.ats.services.organization.entity.fatory.ReferenceFactory;
import org.ats.services.performance.JMeterScriptReference;
import org.ats.services.performance.JMeterScriptService;
import org.ats.services.performance.PerformanceProject;
import org.ats.services.performance.PerformanceProjectFactory;
import org.ats.services.performance.PerformanceProjectService;
import org.ats.services.vmachine.VMachine;
import org.ats.services.vmachine.VMachineReference;
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
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
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
  
  @Inject IaaSServiceProvider iaasProvider;
  
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
    
    PageList<AbstractJob<?>> pages = executorService.query(new BasicDBObject("project_id", projectId));
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
    
    PageList<AbstractJob<?>> jobsList = executorService.query(new BasicDBObject("project_id", project.getId()).append("status", AbstractJob.Status.Completed.toString()), 10);
    
    jobsList.setSortable(new MapBuilder<String, Boolean>("created_date", false).build());
    int totalJob = 0;
    if (jobsList.totalPage() > 0) {
      ArrayNode arrayJob = Json.newObject().arrayNode();
      while (jobsList.hasNext()) {
        List<AbstractJob<?>> listJobs = jobsList.next();
        ObjectNode object;
        for (AbstractJob<?> job : listJobs) {
          
          if (job.get("report") != null && !job.getRawDataOutput().isEmpty()) {
            object = Json.newObject();
            object.put("created_date", formater.format(job.getCreatedDate()));
            object.put("creator", "Cloud-ATS");
            object.put("scripts", ((PerformanceJob) job).getScripts().size());
            object.put("status", job.getStatus().toString());
            object.put("_id", job.getId());
            arrayJob.add(object);
          }
        }
      }
      totalJob = arrayJob.size();
      project.put("jobs", arrayJob.toString());
    }
    project.put("totalJob", totalJob);
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
              report.put("script_name", script.get() != null ? script.get().getName() : report.getScriptId());
            }
            
            array.add(Json.parse(report.toString()));         
          }
        }
        totals.add(array);
      } catch (Exception e) {
        
        e.printStackTrace();
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
  
public Result stopProject(String projectId) throws IOException, JSchException, InterruptedException {
    
    PerformanceProject project = projectService.get(projectId);
    
    if (project == null) return status(404);
    
    VMachine jenkinsVM = vmachineService.getSystemVM(project.getTenant(), project.getSpace());
    
    PageList<AbstractJob<?>> jobList = executorService.query(new BasicDBObject("project_id", projectId), 1);
    jobList.setSortable(new MapBuilder<String, Boolean>("created_date", false).build());
    
    PerformanceJob lastJob = (PerformanceJob) jobList.next().get(0);
    lastJob = (PerformanceJob) executorService.get(lastJob.getId(), "vms");
    IaaSService service = iaasProvider.get();
    //Restart JMeter Service on Test VM
    for (VMachineReference ref : lastJob.getVMs()) {
      VMachine vm = ref.get();
      vm = service.allocateFloatingIp(vm);
      SSHClient.checkEstablished(vm.getPublicIp(), 22, 300);
      Logger.info("Connection to  " + vm.getPublicIp() + " is established");
      
      String command = "sudo -S -p '' service jmeter-2.13 restart";
      Session session = SSHClient.getSession(vm.getPublicIp(), 22, "cloudats", "#CloudATS");              
      ChannelExec channel = (ChannelExec) session.openChannel("exec");
      channel.setCommand(command);
      
      OutputStream out = channel.getOutputStream();
      channel.connect();
      
      out.write("#CloudATS\n".getBytes());
      out.flush();
      
      while(true) {
        if (channel.isClosed()) {
          Logger.info("Restart Jmeter Service: " + channel.getExitStatus());
          break;
        }
      }
      
      channel.disconnect();
      session.disconnect();
      Logger.info("Restarted Jmeter Service on " + vm.getPrivateIp());
      
      service.deallocateFloatingIp(vm);
    }
    
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

  public Result download(String projectId, String jobId) {
    AbstractJob<?> absJob = executorService.get(jobId);
    String path = "/tmp/"+jobId+"/jtl-file";
    File folder = new File(path);
    if(!folder.exists()) {
      folder.mkdir();
    }
    
    FileOutputStream fileOut;
    try {
      PerformanceJob job = (PerformanceJob) absJob;
      
      if(job.getRawDataOutput() == null || job.getScripts() == null)
        return status(404);
      
      List<JMeterScriptReference> listScripts = job.getScripts();
      
      for(JMeterScriptReference script : listScripts) {
        String scriptId = script.getId();
        File jtlFile = new File(path+"/"+scriptId.substring(0, 8)+".jtl");
        if(!jtlFile.exists()) {
          jtlFile.createNewFile();
        }
        Iterator<Map.Entry<String, String>> iterator = job.getRawDataOutput().entrySet().iterator();
        while (iterator.hasNext()) {
          Map.Entry<String, String> entry = iterator.next();
          if (entry.getKey().equals(scriptId)) {
            byte[] content = entry.getValue().getBytes();
            fileOut = new FileOutputStream(jtlFile);
            fileOut.write(content);
            fileOut.close();
            break;
          }
        }
      }
      compress("jtl-file", path,  path+ ".zip");
      
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
     catch (IOException e) {
      e.printStackTrace();
    }
    
    response().setContentType("application/x-download");
    response().setHeader("Content-Encoding", "x-zip");
    response().setHeader("Content-disposition",
        "attachment; filename=jtl-file.zip");
    return ok(new File(path+".zip"));
  }
  
  private void compress(String projectId, String from, String to) throws IOException {
    File fromDir = new File(from);
    ZipOutputStream outDir = new ZipOutputStream(new FileOutputStream(to));
    write(projectId, fromDir, outDir);
    outDir.close();
  }
  
  private void write(String projectId, File file, ZipOutputStream outDir) throws IOException {
    byte[] buffer = new byte[4096]; // Create a buffer for copying
    int bytes_read;
    
    for (String entry : file.list()) {
      File f = new File(file, entry);
      if (f.isDirectory()) {
        write(projectId, f, outDir);
        continue;
      }
      FileInputStream fis = new FileInputStream(f);
      
      String path = f.getPath().substring(f.getPath().indexOf(projectId));
      ZipEntry zip = new ZipEntry(path);
      outDir.putNextEntry(zip);
      while ((bytes_read = fis.read(buffer)) != -1) {
        outDir.write(buffer, 0, bytes_read);
      }
      fis.close();
    }
  }
}
