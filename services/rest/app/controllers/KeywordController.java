/**
 * 
 */
package controllers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


import org.ats.common.MapBuilder;
import org.ats.common.PageList;
import org.ats.jenkins.JenkinsMaster;
import org.ats.jenkins.JenkinsMavenJob;
import org.ats.service.report.ReportService;
import org.ats.services.OrganizationContext;
import org.ats.services.executor.ExecutorService;
import org.ats.services.executor.job.AbstractJob;
import org.ats.services.executor.job.KeywordJob;
import org.ats.services.keyword.CaseFactory;
import org.ats.services.keyword.CaseReference;
import org.ats.services.keyword.CaseService;
import org.ats.services.keyword.CustomKeywordService;
import org.ats.services.keyword.KeywordProject;
import org.ats.services.keyword.KeywordProjectFactory;
import org.ats.services.keyword.KeywordProjectService;
import org.ats.services.keyword.Suite;
import org.ats.services.keyword.SuiteReference;
import org.ats.services.keyword.SuiteService;
import org.ats.services.keyword.report.CaseReportService;
import org.ats.services.keyword.report.StepReportService;
import org.ats.services.keyword.report.SuiteReportService;
import org.ats.services.keyword.report.models.CaseReport;
import org.ats.services.keyword.report.models.StepReport;
import org.ats.services.keyword.report.models.StepReportReference;
import org.ats.services.keyword.report.models.CaseReportReference;
import org.ats.services.keyword.report.models.SuiteReport;
import org.ats.services.organization.acl.Authenticated;
import org.ats.services.organization.entity.Tenant;
import org.ats.services.organization.entity.fatory.ReferenceFactory;
import org.ats.services.vmachine.VMachine;
import org.ats.services.vmachine.VMachineService;

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
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Jul 28, 2015
 */
@CorsComposition.Cors
@Authenticated
public class KeywordController extends Controller {

  @Inject CaseService caseService;
  
  @Inject CaseFactory caseFactory;
  
  @Inject ReferenceFactory<CaseReference> caseRefFactory;
  
  @Inject SuiteService suiteService;
  
  @Inject SuiteReportService suiteReportService;
  
  @Inject CaseReportService caseReportService;
  
  @Inject StepReportService stepReportService;
  
  @Inject KeywordProjectFactory keywordProjectFactory;
  
  @Inject OrganizationContext context;
  
  @Inject ReferenceFactory<SuiteReference> suiteRefFactory;
  
  @Inject KeywordProjectService keywordProjectService;
  
  @Inject ExecutorService executorService;
  
  @Inject ReportService reportService;
  
  @Inject CustomKeywordService customKeywordService;
  
  @Inject VMachineService vmachineService;
  
  private SimpleDateFormat formater = new SimpleDateFormat("dd/MM/yyyy HH:mm");
  
  public Result list() {
    Tenant currentTenant = context.getTenant();
    PageList<KeywordProject> list = keywordProjectService.query(new BasicDBObject("tenant", new BasicDBObject("_id", currentTenant.getId())));
    ArrayNode array = Json.newObject().arrayNode();
    
    while(list.hasNext()) {
      for (KeywordProject project : list.next()) {
        project.put("type", "keyword");
        project.put("totalSuites", suiteService.getSuites(project.getId()).count());
        project.put("totalCases", caseService.getCases(project.getId()).count());
        
        BasicDBObject query = new BasicDBObject("project_id", project.getId()).append("status", AbstractJob.Status.Completed.toString());
        PageList<AbstractJob<?>> jobList = executorService.query(query, 1);
        jobList.setSortable(new MapBuilder<String, Boolean>("created_date", false).build());
        
        if (jobList.totalPage() > 0) {
          AbstractJob<?> lastJob = jobList.next().get(0);
          project.put("lastRunning", formater.format(lastJob.getCreatedDate()));
          project.put("lastJobId", lastJob.getId());
          
          List<SuiteReference> suites = ((KeywordJob) lastJob).getSuites();
          if (suites.size() > 0) {
            BasicDBList lastSuites = new BasicDBList();
            for (SuiteReference suite : suites) lastSuites.add(suite.toJSon());
            project.put("lastSuites", lastSuites);
          }
        }
        array.add(Json.parse(project.toString()));
      }
    }
    return ok(array);
  }
  
  public Result viewLog(String projectId) {
    
    BasicDBObject query = new BasicDBObject("project_id", projectId);
    PageList<AbstractJob<?>> jobList = executorService.query(query, 1);
    jobList.setSortable(new MapBuilder<String, Boolean>("created_date", false).build());
    
    String log = "";
    if (jobList.totalPage() > 0) {
      AbstractJob<?> lastJob = jobList.next().get(0);
      log = lastJob.getLog();
    }
    
    return log.isEmpty() ? status(404) : status(200, log);
  }
  
  public Result get(String projectId) {
    KeywordProject project = keywordProjectService.get(projectId,"show_action","value_delay");
    if (project == null) return status(404);
    
    project.put("type", "keyword");
    project.put("totalSuites", suiteService.getSuites(project.getId()).count());
    project.put("totalCases", caseService.getCases(project.getId()).count());
    
    PageList<AbstractJob<?>> jobList = executorService.query(new BasicDBObject("project_id", projectId), 1);
    jobList.setSortable(new MapBuilder<String, Boolean>("created_date", false).build());
    
    if (jobList.totalPage() > 0) {
      AbstractJob<?> lastJob = jobList.next().get(0);
      project.put("lastRunning", formater.format(lastJob.getCreatedDate()));
      project.put("log", true);
      project.put("lastSuites", lastJob.get("suites"));
      project.put("lastJobId", lastJob.getId());
    }
    
    return ok(Json.parse(project.toString()));
  }

  public Result create() {
    JsonNode json = request().body().asJson();
    String name = json.get("name").asText();
    boolean showAction = json.get("showAction").asBoolean();
    int valueDelay = json.get("valueDelay").asInt();
    KeywordProject project = keywordProjectFactory.create(context, name);
    project.setShowAction(showAction);
    project.setValueDelay(valueDelay);
    keywordProjectService.create(project);
    return status(201, project.getId());
  }
  
  public Result update() {
    JsonNode data = request().body().asJson();
    String id = data.get("id").asText();
    String name = data.get("name").asText();
    boolean showAction = data.get("showAction").asBoolean();
    int valueDelay = data.get("valueDelay").asInt();
    KeywordProject project = keywordProjectService.get(id,"show_action","value_delay");
    
    if (name.equals(project.getString("name"))) {
      if((project.getShowAction() == showAction) && (project.getValueDelay() == valueDelay)) {
        return status(304);
      }
    }
    project.put("name", name);
    project.setShowAction(showAction);
    project.setValueDelay(valueDelay);
    keywordProjectService.update(project);

    return status(202, id);
  }
  
  public Result delete() {
    
    String id = request().body().asText();
    
    KeywordProject project = keywordProjectService.get(id);
    if (project == null) {
      return status(404);
    }
    
    PageList<AbstractJob<?>> pages = executorService.query(new BasicDBObject("project_id", id));
    
    List<AbstractJob<?>> list = null;
    while (pages.hasNext()) {
      list = pages.next();
      for (AbstractJob<?> job : list) {
        reportService.deleteBy(new BasicDBObject("functional_job_id", job.getId()));
      }
    }
    
    executorService.deleteBy(new BasicDBObject("project_id", id));
    suiteService.deleteBy(new BasicDBObject("project_id", id));
    caseService.deleteBy(new BasicDBObject("project_id", id));
    customKeywordService.deleteBy(new BasicDBObject("project_id", id));
    keywordProjectService.delete(id);
    return status(200);
  }
  
  public Result run(String projectId) throws Exception {
    JsonNode data = request().body().asJson();
    JsonNode jsonSuites = data.get("suites");
    JsonNode jsonOptions = data.get("options");
    
    List<SuiteReference> suites = new ArrayList<SuiteReference>(jsonSuites.size());
    String versionSelenium = jsonOptions.get("versionSelenium") != null ? jsonOptions.get("versionSelenium").asText() : KeywordProjectFactory.DEFAULT_INIT_VERSION_SELENIUM;
    SuiteReference ref;
    for (JsonNode sel : jsonSuites) {
      ref = suiteRefFactory.create(sel.asText());
      
      if (suiteService.get(ref.getId()) == null) {
        return status(400);
      }
      
      String browser = jsonOptions.get("browser") != null ? jsonOptions.get("browser").asText() : null;
      String version = jsonOptions.get("version") != null ? jsonOptions.get("version").asText() : null;
      
      StringBuilder initDriver = new StringBuilder();
      if ("firefox".equals(browser)) {
        initDriver.append("System.setProperty(\"webdriver.firefox.bin\", \"/home/cloudats/firefox-store/").append(version).append("/firefox\");\n");
        initDriver.append("wd = new FirefoxDriver();");
      } else if ("chrome".equals(browser)) {
        initDriver.append("System.setProperty(\"webdriver.chrome.driver\", \"/home/cloudats/chromedriver\");\n wd = new ChromeDriver();");
      }
      
      if (initDriver.length() > 0) {
        Suite suite = ref.get();
        suite.put("init_driver", initDriver.toString());
        suiteService.update(suite);
      }
      
      suites.add(suiteRefFactory.create(sel.asText()));
    }
    
    KeywordProject project = keywordProjectService.get(projectId,"show_action","value_delay");
    if (project == null) return status(404);
    
    project.setVersionSelenium(versionSelenium);
    keywordProjectService.update(project);
    
    if (project.getStatus() == KeywordProject.Status.RUNNING) return status(204);
    
    KeywordJob job = executorService.execute(project, suites);
    return status(201, Json.parse(job.toString()));
  }
  
  public Result report(String projectId,String jobId) throws Exception{
    ArrayNode array = Json.newObject().arrayNode();
    
    AbstractJob<?> job = executorService.get(jobId);
    
    if(job.getRawDataOutput() != null) {
      PageList<SuiteReport> suites = suiteReportService.query(new BasicDBObject("jobId", job.getId()));
      
      while (suites.hasNext()) {
    	  for (SuiteReport suite : suites.next()) {
    		  array.add(Json.parse(suite.toString()));
    	  }
      }
      return status(200, array);
    }
    
    return status(404);
  }
  
  public Result getReportCase(String projectId, String jobId, String caseReportId){
	  ObjectNode objNode = Json.newObject() ;
	  ArrayNode array = Json.newObject().arrayNode();
	  
	  PageList<CaseReport> caseReport = caseReportService.query(new BasicDBObject("_id",caseReportId));
	  CaseReport caze = caseReport.next().get(0);
	  objNode.put("caseName", caze.getName());
	  objNode.put("dataSource", caze.getDataSource());
	  List<StepReportReference> listStepReport = caze.getSteps() ;
	  for (StepReportReference stepReportReference : listStepReport) {
		StepReport stepReport = stepReportService.get(stepReportReference.getId(), "params", "isPass");
		array.add(Json.parse(stepReport.toString()));
	  }
	  objNode.put("listStep", Json.parse(array.toString()));
	  return status(200, objNode);
  }
  
  public Result listReport(String projectId) throws Exception {
	  
	String indexRequest = request().getQueryString("index");
    BasicDBObject query = new BasicDBObject();
    BasicDBList andCondition = new BasicDBList();
    andCondition.add(new BasicDBObject("project_id", projectId));
    andCondition.add(new BasicDBObject("report", new BasicDBObject("$ne", null)));
    query.append("$and", andCondition);
    
    PageList<AbstractJob<?>> jobs= executorService.query(query);
    jobs.setSortable(new MapBuilder<String, Boolean>("created_date", false).build());
    
    ArrayNode array = Json.newObject().arrayNode();
    int index = Integer.parseInt(indexRequest);
    
    List<AbstractJob<?>> list = jobs.getPage(index);
    for (int i = 1; i <= list.size(); i ++) {
    	AbstractJob<?> job = list.get(i -1);
    	ObjectNode obj = Json.newObject();
    	PageList<SuiteReport> suites = suiteReportService.query(new BasicDBObject("jobId", job.getId()));
    	
    	long duration = 0;
    	int count_fail = 0;
    	while (suites.hasNext()) {
    	  for (SuiteReport suite : suites.next()) {
    	    duration += (suite.getDuration()/1000);
    	    PageList<CaseReport> cases = caseReportService.query(new BasicDBObject("suite_report_id", suite.getId()).append("isPass", false));
    	    
    	    if (cases.count() > 0) {
    	      count_fail ++;
    	    }
    	    
    	  }
    	}
    	obj.put("total", jobs.count());
    	obj.put("jobId", job.getId());
    	obj.put("duration", duration);
    	obj.put("numberPassedSuite", suites.count() - count_fail);
    	obj.put("numberFailedSuite", count_fail);
    	obj.put("created_date", formater.format(job.getCreatedDate()));
    	obj.put("stt", ((index - 1) * 10 ) + i);
    	array.add(obj);
    }
    return ok(array);
  }
  
  public Result stopProject(String projectId) throws IOException {
    
    KeywordProject project = keywordProjectService.get(projectId);
    
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
    
    project.setStatus(KeywordProject.Status.READY);
    keywordProjectService.update(project);
    
    jenkinsJob.stop();
    
    return status(200);
  }
  
  public Result suiteReport(String projectId, String jobId, String suiteId, String suite_report_id) {
    
    int index = Integer.parseInt(request().getQueryString("index"));
    
    // get suite report with suiteId and jobId
    PageList<SuiteReport> suites = suiteReportService.query(new BasicDBObject("jobId", jobId).append("suiteId", suiteId));
    SuiteReport suite = suites.next().get(0);
    List<CaseReportReference> caseRefs = suite.getCases();
    
    // store unique case id by set
    Set<String> set = new HashSet<String>();
    for (CaseReportReference ref : caseRefs) {
      CaseReport report = ref.get();
      set.add(report.getCaseId());
    }
    //create list unique case id
    List<String> list = new ArrayList<>(set);
    
    //result object is used to return data to client
    ObjectNode result = Json.newObject();
    
    // array node to store  list case report with data driven
    ArrayNode caseArray = Json.newObject().arrayNode();
    
    //get report with 10 caseId
    int startIndex = index * 10;
    for (int i = startIndex; i < list.size() && i < (startIndex + 10); i ++) {
      ObjectNode obj = Json.newObject();
      
      //get all case report with case id and suite report id
      PageList<CaseReport> reports = caseReportService.query(new BasicDBObject("case_id", list.get(i)).append("suite_report_id", suite_report_id));
      
      
      if (reports.count() > 1) {
        obj.put("data_driven", true);
        ArrayNode array = Json.newObject().arrayNode();
        while (reports.hasNext()) {
          for (CaseReport report : reports.next()) {
            array.add(Json.parse(report.toString()));
          }
        }
        obj.put("data_source", array.toString());
        
      } else {
        obj.put("report", reports.next().get(0).toString());
        obj.put("data_driven", false);
      }
      
      reports = caseReportService.query(new BasicDBObject("case_id", list.get(i)).append("suite_report_id", suite_report_id));
      
      //Get failed case 
      PageList<CaseReport> failedReports = caseReportService.query(new BasicDBObject("case_id", list.get(i)).append("suite_report_id", suite_report_id).append("isPass", false));
      //if one data line is failed, the test case is failed
      if (failedReports.count() > 0) {
        obj.put("result", "Fail");
      } else obj.put("result", "Pass");
      
      obj.put("name", reports.next().get(0).getName());
      obj.put("stt", i+1+"");
      caseArray.add(obj);
      
      result.put("totalCase", list.size());
      result.put("reports", caseArray.toString());
      
    }
    
    // 10 last job contains test suite
    suites = suiteReportService.query(new BasicDBObject("suiteId", suiteId));
    suites.setSortable(new MapBuilder<String, Boolean>("created_date", false).build());
    List<SuiteReport> listSuites = suites.next();
    
    ArrayNode suiteArray = Json.newObject().arrayNode();
    for (int i = 0; i < listSuites.size() && i < 10; i ++) {
      SuiteReport report = listSuites.get(i);
      suiteArray.add(Json.parse(report.toString()));
    }
    result.put("suites", suiteArray.toString());
    
    return ok(result);
  }
  
  public Result download(String projectId, String jobId) {
    AbstractJob<?> absJob = executorService.get(jobId,"raw_data");
    String path = "/tmp/"+projectId.substring(0, 8);
    File folder = new File(path);
    if(!folder.exists()) {
      folder.mkdir();
    }
    KeywordJob job = (KeywordJob) absJob;
    if(job.getRawData() == null)
      return status(404);
    byte[] report = job.getRawData();
    FileOutputStream fileOut;
    try {
      fileOut = new FileOutputStream(path+"/resource-"+jobId+".tar.gz");
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
        "attachment; filename=resource.tar.gz");
    return ok(new File(path+"/resource-"+jobId+".tar.gz"));
  }
}
