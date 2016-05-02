/**
 * 
 */
package controllers;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.ats.common.MapBuilder;
import org.ats.common.PageList;
import org.ats.jenkins.JenkinsMaster;
import org.ats.jenkins.JenkinsMavenJob;
import org.ats.service.report.ReportService;
import org.ats.services.OrganizationContext;
import org.ats.services.executor.ExecutorService;
import org.ats.services.executor.job.AbstractJob;
import org.ats.services.executor.job.SeleniumUploadJob;
import org.ats.services.organization.acl.Authenticated;
import org.ats.services.project.MixProject;
import org.ats.services.project.MixProjectService;
import org.ats.services.upload.SeleniumUploadProject;
import org.ats.services.upload.SeleniumUploadProjectFactory;
import org.ats.services.upload.SeleniumUploadProjectService;
import org.ats.services.vmachine.VMachine;
import org.ats.services.vmachine.VMachineService;

import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http.MultipartFormData;
import play.mvc.Result;
import actions.CorsComposition;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.mongodb.BasicDBObject;

/**
 * @author NamBV2
 *
 *         Sep 17, 2015
 */
@CorsComposition.Cors
@Authenticated
public class SeleniumUploadController extends Controller {

  @Inject
  private SeleniumUploadProjectFactory projectFactory;

  @Inject
  private OrganizationContext context;

  @Inject
  private SeleniumUploadProjectService seleniumUploadService;

  @Inject
  private ExecutorService executorService;
  
  @Inject VMachineService vmachineService;
  
  @Inject ReportService reportService;
  
  @Inject MixProjectService mpService;
  
  private static final int BUFFER_SIZE = 4096;

  private SimpleDateFormat formater = new SimpleDateFormat("dd/MM/yyyy HH:mm");

  public Result get(String projectId) {
	MixProject mp = mpService.get(projectId);
    SeleniumUploadProject project = seleniumUploadService.get(mp.getSeleniumId());

    if (project == null)
      return status(404);

    project.put("type", "Selenium Upload");
    
    SeleniumUploadProject upload = seleniumUploadService.get(mp.getSeleniumId(), "raw");
    boolean rawExist = false;
    if (upload.getRawData() != null) {
      rawExist = true;
    }
    project.put("raw_exist", rawExist);
    PageList<AbstractJob<?>> jobList = executorService.query(
        new BasicDBObject("project_id", mp.getSeleniumId()), 1);
    jobList.setSortable(new MapBuilder<String, Boolean>("created_date", false)
        .build());

    if (jobList.totalPage() > 0) {
      AbstractJob<?> lastJob = jobList.next().get(0);
      project.put("lastRunning", formater.format(lastJob.getCreatedDate()));
      project.put("log", lastJob.getLog());
      project.put("lastJobId", lastJob.getId());
    }
    return ok(Json.parse(project.toString()));
  }

  public Result list() {
    PageList<SeleniumUploadProject> list = seleniumUploadService.list();
    ArrayNode array = Json.newObject().arrayNode();
    while (list.hasNext()) {
      for (SeleniumUploadProject project : list.next()) {
        project.put("type", "keyword");
        project.put("upload_project", true);
        project.put("created_date", project.getDate("created_date").getTime());
        BasicDBObject query = new BasicDBObject("project_id", project.getId())
            .append("status", AbstractJob.Status.Completed.toString());
        PageList<AbstractJob<?>> jobList = executorService
            .query(query, 1);
        jobList.setSortable(new MapBuilder<String, Boolean>("created_date",
            false).build());

        if (jobList.totalPage() > 0) {
          AbstractJob<?> lastJob = jobList.next().get(0);
          project.put("lastRunning", formater.format(lastJob.getCreatedDate()));
          project.put("lastJobId", lastJob.getId());
          project.put("log", lastJob.getLog());
        }
        array.add(Json.parse(project.toString()));
      }
    }
    return ok(array);
  }

  public Result update() {
    JsonNode data = request().body().asJson();
    String id = data.get("id").asText();
    String name = data.get("name").asText();

    SeleniumUploadProject project = seleniumUploadService.get(id,"raw");

    if (name.equals(project.getString("name"))) {
      return status(304);
    }

    project.put("name", name);
    seleniumUploadService.update(project);

    return status(202, id);
  }

  public Result delete() {

    String id = request().body().asText();

    SeleniumUploadProject project = seleniumUploadService.get(id);
    if (project == null) {
      return status(404);
    }

    executorService.deleteBy(new BasicDBObject("project_id", id));
    seleniumUploadService.delete(project);

    return status(200);
  }

  public Result run(String projectId) throws Exception {
	MixProject mp = mpService.get(projectId);
    SeleniumUploadProject project = seleniumUploadService.get(mp.getSeleniumId(), "raw");
    if (project == null)
      return status(404);

    if (project.getStatus() == SeleniumUploadProject.Status.RUNNING)
      return status(204);

    SeleniumUploadJob job = executorService.execute(project);
    return status(201, Json.parse(job.toString()));
  }

  public Result create() {
    JsonNode json = request().body().asJson();
    String name = json.get("name").asText();
    SeleniumUploadProject project = projectFactory.create(context, name, "");
    seleniumUploadService.create(project);
    return status(201, project.getId());
  }

  public Result report(String projectId, String jobId) throws Exception {
    AbstractJob<?> job = executorService.get(jobId);
    ObjectNode obj = Json.newObject();
    String result = "";
    obj.put("created_date", formater.format(job.getCreatedDate()));
    if(job.getLog() == null || job.getResult() == null || (job.getResult()!= null && job.getResult().equalsIgnoreCase("Aborted")))
      return status(404);
    
    obj.put("log", job.getLog());
    obj.put("jobId", job.getId());
    if((job.getResult() != null) && ("SUCCESS".equals(job.getResult()))) {
      result = "Pass";
    } else {
      result = "Fail";
    }
    obj.put("result", result);
    return status(200, obj);
  }

  public Result listReport(String projectId) {
    PageList<AbstractJob<?>> jobList = executorService.query(
        new BasicDBObject("project_id", projectId), 1);
    jobList.setSortable(new MapBuilder<String, Boolean>("created_date", false)
        .build());
    ArrayNode array = Json.newObject().arrayNode();
    String result = "";
    while (jobList.hasNext()) {
      for (AbstractJob<?> job : jobList.next()) {
        if (AbstractJob.Status.Completed.equals(job.getStatus()) && !job.getResult().equalsIgnoreCase("Aborted")) {
          ObjectNode obj = Json.newObject();
          obj.put("created_date", formater.format(job.getCreatedDate()));
          obj.put("log", job.getLog());
          obj.put("jobId", job.getId());
          if((job.getResult() != null) && ("SUCCESS".equals(job.getResult()))) {
            result = "Pass";
          } else {
            result = "Fail";
          }
          obj.put("result", result);
          
          array.add(obj);
        }
      }
    }
    return ok(array);
  }

  public Result download(String projectId, String jobId) {
	MixProject mp = mpService.get(projectId);
    AbstractJob<?> absJob = executorService.get(jobId,"raw_report");
    String path = "/tmp/"+mp.getSeleniumId().substring(0, 8);
    File folder = new File(path);
    if(!folder.exists()) {
      folder.mkdir();
    }
    SeleniumUploadJob job = (SeleniumUploadJob) absJob;
    if(job.getRawData() == null)
      return status(404);
    byte[] report = job.getRawData();
    FileOutputStream fileOut;
    try {
      fileOut = new FileOutputStream(path + "/result-" + jobId + ".tar.gz");
      fileOut.write(report);
      fileOut.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
     catch (IOException e) {
      e.printStackTrace();
    }
    
    response().setContentType("application/x-download");
    response().setHeader("Content-Encoding", "gzip");
    response().setHeader("Content-disposition",
        "attachment; filename=report.tar.gz");
    return ok(new File(path + "/result-" + jobId + ".tar.gz"));
  }
  
  private void deleteFolder(File folder) {
    for (File item : folder.listFiles()) {
      if (item.isDirectory()) deleteFolder(item);
      else item.delete();
    }
    folder.delete();
  }

  public Result upload(String projectId) {
	MixProject mp = mpService.get(projectId);
    MultipartFormData body = request().body().asMultipartFormData();
    MultipartFormData.FilePart typeFile = body.getFile("file");
    if (typeFile != null) {
      boolean formatProject = false;
      File file = typeFile.getFile();
      
      FileInputStream fileInputStream = null;
      byte[] bFile = new byte[(int) file.length()];

      // delete file pom.xml if it's exist before uncompress
      String destDirectoryPath = "/tmp/" + mp.getSeleniumId().substring(0, 8);
      File tempFolder = new File(destDirectoryPath);
      if (tempFolder.exists()) deleteFolder(tempFolder);
      
      tempFolder.mkdir();

      // start uncompress file zip
      try {
        ZipInputStream zipIn = new ZipInputStream(new FileInputStream(file));
        ZipEntry entry = zipIn.getNextEntry();
        while (entry != null) {
          String filePath = destDirectoryPath + "/" + entry.getName();
          if (!entry.isDirectory()) {

            File fileEntry = new File(filePath);
            fileEntry.getParentFile().mkdirs();
            
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(fileEntry));
            byte[] bytesIn = new byte[BUFFER_SIZE];
            int read = 0;
            
            while ((read = zipIn.read(bytesIn)) != -1) {
              bos.write(bytesIn, 0, read);
            }
            bos.close();
          } else {
            File dir = new File(filePath);
            dir.mkdir();
          }
          zipIn.closeEntry();
          entry = zipIn.getNextEntry();
        }
        zipIn.close();

        // Check format of file upload
        File folder = new File("/tmp/" + mp.getSeleniumId().substring(0, 8));
        File[] listOfFiles = folder.listFiles();

        for (File item : listOfFiles) {
          if (item.isFile()) {
            if ("pom.xml".equals(item.getName())) {
              formatProject = true;
            }
          }
        }
        if (!formatProject)
          return status(404);

        // convert file into array of bytes
        fileInputStream = new FileInputStream(file);
        fileInputStream.read(bFile);
        fileInputStream.close();

        SeleniumUploadProject project = seleniumUploadService.get(mp.getSeleniumId());
        if (project.getRawData() != null) {
          project.setRawData(null);
        }
        project.setRawData(bFile);
        seleniumUploadService.update(project);

      } catch (Exception ex) {
        ex.printStackTrace();
        return status(404);
      }
      // end uncompress file
      return status(201);
    } else {
      return badRequest();
    }
  }
  
  public Result stop(String projectId) throws IOException {
	MixProject mp = mpService.get(projectId);
    SeleniumUploadProject project = seleniumUploadService.get(mp.getSeleniumId(), "raw");
    if (project == null) return status(404);
    
    VMachine jenkinsVM = vmachineService.getSystemVM(project.getTenant(), project.getSpace());
    
    PageList<AbstractJob<?>> jobList = executorService.query(new BasicDBObject("project_id", mp.getSeleniumId()), 1);
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
    
    project.setStatus(SeleniumUploadProject.Status.READY);
    seleniumUploadService.update(project);
    jenkinsJob.stop();
    
    return status(200);
  }
  
}
