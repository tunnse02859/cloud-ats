/**
 * TrinhTV3@fsoft.com.vn
 */
package controllers;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.ats.common.MapBuilder;
import org.ats.common.PageList;
import org.ats.common.StringUtil;
import org.ats.service.report.Report;
import org.ats.service.report.ReportService;
import org.ats.service.report.ReportService.Type;
import org.ats.services.OrganizationContext;
import org.ats.services.executor.ExecutorService;
import org.ats.services.executor.job.AbstractJob;
import org.ats.services.executor.job.KeywordJob;
import org.ats.services.executor.job.PerformanceJob;
import org.ats.services.keyword.KeywordProject;
import org.ats.services.organization.acl.Authenticated;
import org.ats.services.organization.entity.fatory.ReferenceFactory;
import org.ats.services.performance.JMeterArgument;
import org.ats.services.performance.JMeterFactory;
import org.ats.services.performance.JMeterParser;
import org.ats.services.performance.JMeterSampler;
import org.ats.services.performance.JMeterSampler.Method;
import org.ats.services.performance.JMeterScript;
import org.ats.services.performance.JMeterScriptReference;
import org.ats.services.performance.JMeterScriptService;
import org.ats.services.performance.PerformanceProject;
import org.ats.services.performance.PerformanceProjectFactory;
import org.ats.services.performance.PerformanceProjectService;

import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.Result;
import actions.CorsComposition;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.mongodb.BasicDBObject;

/**
 * @author TrinhTV3
 *
 */
@CorsComposition.Cors
@Authenticated
public class PerformanceController extends Controller {
  
  @Inject
  private JMeterFactory jmeterFactory;
  
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
  
  private SimpleDateFormat formater = new SimpleDateFormat("dd/MM/yyyy HH:mm");
  /**
   * 
   * @return
   */
  public Result listPerformanceProject() {
    
    PageList<PerformanceProject> list = projectService.list();
    
    ArrayNode array = Json.newObject().arrayNode();
    
    while (list.hasNext()) {
      
      List<PerformanceProject> listPer = list.next();
      ObjectNode object;
      for (PerformanceProject per : listPer) {
         object = Json.newObject();
         object.put("_id", per.getString("_id"));
         array.add(object);
      }
      
    }
    System.out.println(array);
    return ok(array);
  }
  
  
  /**
   * Create test performance project by using wizard
   * @return
   * @throws UnsupportedEncodingException
   */
  public Result createPerformanceTestWizard () throws UnsupportedEncodingException {
    
    JsonNode data = request().body().asJson();
    
    String projectName = data.get("project_name").toString();
    int loops = data.get("loops").asInt();
    int ramup = data.get("ramup").asInt();
    int duration = data.get("duration").asInt();
    int users = data.get("users").asInt();
    
    JsonNode nodeSamplers = data.get("samplers");
    JMeterSampler[] arraySamplers = new JMeterSampler[nodeSamplers.size()];
    
    // declare variables
    String sampler_url;
    String sampler_assertionTime = null;
    long sampler_constantTime;
    String sampler_method;
    String sampler_name;
    JsonNode nodeParams;
    JMeterArgument[] arrayParams;
    // loops through all samplers
    int i = 0;
    for (JsonNode jsonSampler : nodeSamplers) {
      
      sampler_url = jsonSampler.get("sampler_url").toString();
      
      if (jsonSampler.get("sampler_assertionTime") != null && "".equals(jsonSampler.get("sampler_assertionTime").toString())) {
        sampler_assertionTime = jsonSampler.get("sampler_assertionTime").toString();
      }
      
      sampler_constantTime = jsonSampler.get("sampler_constantTime").asLong();
      sampler_method = jsonSampler.get("sampler_method").asText();
      sampler_name = jsonSampler.get("sampler_name").toString();
      
      nodeParams = jsonSampler.get("params");
      arrayParams = new JMeterArgument[nodeParams.size()];
      
      // loops through all parameters in each sampler
      int j = 0;
      for (JsonNode jsonParam: nodeParams) {
        
        //create parameter object
        arrayParams[j] = jmeterFactory.createArgument(jsonParam.get("name").toString(), jsonParam.get("value").toString());
        j ++;
      }
      
      //create sampler object
      arraySamplers[i] = jmeterFactory.createHttpRequest(Method.valueOf(sampler_method), sampler_name, sampler_url, sampler_assertionTime, sampler_constantTime, arrayParams);
      
      i ++;
      
    }

    PerformanceProject project = projectFactory.create(projectName);
    projectService.create(project);
    
    //create jmeter script
    JMeterScript script = jmeterFactory.createJmeterScript("Script 1", loops, users, ramup, false, duration, project.getId(), arraySamplers);
    jmeterService.create(script); // save jmeter script into database
    
    return status(200);
  }
  
  /**
   * Update performance test script by wizard
   * @return
   * @throws UnsupportedEncodingException 
   */
  public Result updatePerformanceTestWizard() throws UnsupportedEncodingException {
    
    JsonNode data = request().body().asJson();
        
    String scriptId = data.get("script_id").asText();
    
    int loops = data.get("loops").asInt();
    System.out.println("AAAloop:" + loops);
    int ramup = data.get("ramup").asInt();
    int duration = data.get("duration").asInt();
    int users = data.get("users").asInt();
    
    JsonNode nodeSamplers = data.get("samplers");
    JMeterSampler[] arraySamplers = new JMeterSampler[nodeSamplers.size()];
    
    // declare variables
    String sampler_url;
    String sampler_assertionTime = null;
    long sampler_constantTime;
    String sampler_method;
    String sampler_name;
    JsonNode nodeParams;
    JMeterArgument[] arrayParams;
    // loops through all samplers
    int i = 0;
    for (JsonNode jsonSampler : nodeSamplers) {
      
      sampler_url = jsonSampler.get("sampler_url").toString();
      
      if (jsonSampler.get("sampler_assertionTime") != null && "".equals(jsonSampler.get("sampler_assertionTime").toString())) {
        sampler_assertionTime = jsonSampler.get("sampler_assertionTime").toString();
      }
      
      sampler_constantTime = jsonSampler.get("sampler_constantTime").asLong();
      sampler_method = jsonSampler.get("sampler_method").asText();
      sampler_name = jsonSampler.get("sampler_name").toString();
      
      nodeParams = jsonSampler.get("params");
      arrayParams = new JMeterArgument[nodeParams.size()];
      
      // loops through all parameters in each sampler
      int j = 0;
      for (JsonNode jsonParam: nodeParams) {
        
        //create parameter object
        arrayParams[j] = jmeterFactory.createArgument(jsonParam.get("name").toString(), jsonParam.get("value").toString());
        j ++;
      }
      
      //create sampler object
      arraySamplers[i] = jmeterFactory.createHttpRequest(Method.valueOf(sampler_method), sampler_name, sampler_url, sampler_assertionTime, sampler_constantTime, arrayParams);
      
      i ++;
      
    }
    
    JMeterScript script = jmeterService.get(scriptId);
    script.setLoops(loops);
    script.setDuration(duration);
    script.setNumberThreads(users);
    script.setRamUp(ramup);
    script.addSampler(arraySamplers);
    
    jmeterService.update(script);
    
    return status(200);
  }
  
  /**
   * Create performance by upload jmx file
   * @return
   * @throws FileNotFoundException
   * @throws IOException
   */
  public Result createPerformanceTestByFile() throws FileNotFoundException, IOException {
    
    MultipartFormData body = request().body().asMultipartFormData();
    List<FilePart> listFiles = body.getFiles();
    
    String scriptName = request().getQueryString("script_name");
    String projectId = request().getQueryString("project_id");
    
    PerformanceProject project = projectService.get(projectId);
    // return badRequest if file amount is lower 1
    if (listFiles.size() <= 0) {
      return badRequest();
    }
    long i = jmeterService.getJmeterScripts(projectId).count();
     
    // create performance project model 
    FileInputStream fis;
    String content;
    JMeterParser parse;
    JMeterScript script;
    
    ArrayNode array = Json.newObject().arrayNode();
    // loop though files
    for (FilePart file : listFiles) {
      i ++;
      try {
        // read each file and get file content
        fis = new FileInputStream(file.getFile());
        content = StringUtil.readStream(fis);
        
        // create jmeter parser by file content and build jmeter script 
        parse = jmeterFactory.createJMeterParser(content, project.getId());
        script = parse.parse();
        
        script.put("name", "script" + i);
        script.put("project_id", project.getId());
        
        array.add(Json.parse(script.toString()));
        // save jmeter script into database
        jmeterService.create(script);
        
      } catch(Exception e) {
         throw new RuntimeException();
      }
    }
    
    return ok(array);
  }
  
  public Result list() {
    PageList<PerformanceProject> list = projectService.list();
    ArrayNode array = Json.newObject().arrayNode();
    
    
    while(list.hasNext()) {
      for (PerformanceProject project : list.next()) {
        project.put("type", "performance");
        project.put("totalScripts", jmeterService.getJmeterScripts(project.getId()).count());
        
        PageList<AbstractJob<?>> pages = executorService.query(new BasicDBObject("project_id", project.getId()).append("status", AbstractJob.Status.Completed.toString()));
        pages.setSortable(new MapBuilder<String, Boolean>("created_date", false).build());
        if (pages.count() > 0) {
          project.put("lastRunning", formater.format(pages.next().get(0).getCreatedDate()));
        }
        array.add(Json.parse(project.toString()));
      }
    }
    
    return ok(array);
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
    
    PageList<JMeterScript> pages = jmeterService.getJmeterScripts(projectId);
    ArrayNode arrayScript = Json.newObject().arrayNode();
    List<JMeterScript> list;
    while (pages.hasNext()) {
      
      list = pages.next();
      for (JMeterScript script : list) {
        
        arrayScript.add(Json.parse(script.toString()));
      }
    }
    PageList<AbstractJob<?>> pageJobs = executorService.query(new BasicDBObject("project_id", projectId));
    pageJobs.setSortable(new MapBuilder<String, Boolean>("created_date", false).build());
    
    ArrayNode arrayJob = Json.newObject().arrayNode();
    while (pageJobs.hasNext()) {
      List<AbstractJob<?>> listJobs = pageJobs.next();
      
      for (AbstractJob<?> job : listJobs) {
        
        job.put("created_date", formater.format(job.getCreatedDate()));
        arrayJob.add(Json.parse(job.toString()));
      }
    }
    PageList<AbstractJob<?>> jobFirstList = executorService.query(new BasicDBObject("project_id", project.getId()), 1);
    jobFirstList.setSortable(new MapBuilder<String, Boolean>("created_date", false).build());
    
    if (jobFirstList.count() > 0) {
      AbstractJob<?> lastRunningJob = jobFirstList.next().get(0);
      project.put("last_running", formater.format(lastRunningJob.getCreatedDate()));
      project.put("jobs", arrayJob.toString());
      project.put("lastScripts", lastRunningJob.get("scripts"));
      project.put("log", lastRunningJob.getLog());
    }
    
    project.put("type", "performance");
    project.put("totalScripts", jmeterService.getJmeterScripts(projectId).count());
    return ok(Json.parse(project.toString()));
  }
  
  public Result run(String projectId) throws Exception {
    
    JsonNode data = request().body().asJson();
    
    List<JMeterScriptReference> scripts = new ArrayList<JMeterScriptReference>(); 
    JMeterScriptReference ref;
    for (JsonNode json : data) {
      
      ref = jmeterReferenceFactory.create(json.asText());
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
  
  public Result report(String projectId, String jobId) throws Exception {
    
    PerformanceJob job = (PerformanceJob) executorService.get(jobId);
    
    ArrayNode totals = Json.newObject().arrayNode();
    
    for (JMeterScriptReference script : job.getScripts()) {
      ArrayNode array = Json.newObject().arrayNode();
      PageList<Report> pages = reportService.getList(jobId, Type.PERFORMANCE, script.getId());
      while (pages.hasNext()) {
        List<Report> list = pages.next();
        
        for (Report report : list) {         
          array.add(Json.parse(report.toString()));         
        }
      }
      totals.add(array);
    }
    
    return status(200, totals);
  }
  
  public Result getReport(String id) {
    
    Report report = reportService.get(id);
    return status(200, Json.parse(report.toString()));
  }
  
  public Result getLastestRun(String projectId) throws Exception {
    BasicDBObject query = new BasicDBObject("project_id", projectId)
    .append("status", AbstractJob.Status.Completed.toString());
    
    PageList<AbstractJob<?>> jobFirstList = executorService.query(query, 1);
    jobFirstList.setSortable(new MapBuilder<String, Boolean>("created_date", false).build());
    
    AbstractJob<?> lastRunningJob = jobFirstList.next().get(0);
    
    String jobId = lastRunningJob.getId();
    
    PerformanceJob job = (PerformanceJob) executorService.get(jobId);
    
    ArrayNode total = Json.newObject().arrayNode();
    ObjectNode object = Json.newObject();
    for (JMeterScriptReference scriptRef : job.getScripts()) {
      
      ArrayNode reports = Json.newObject().arrayNode();
      PageList<Report> listReport = reportService.getList(jobId, Type.PERFORMANCE, scriptRef.getId());
      
      while (listReport.hasNext()) {
        List<Report> list = listReport.next();
        
        for (Report report : list) {
          
          reports.add(Json.parse(report.toString()));
        }
      }
      
      total.add(reports);
      
    }
    object.put("jobId", jobId);
    object.put("total", total);
    
    return status(200, object);
  }
  
}
