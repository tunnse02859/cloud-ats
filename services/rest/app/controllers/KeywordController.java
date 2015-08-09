/**
 * 
 */
package controllers;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.ats.common.MapBuilder;
import org.ats.common.PageList;
import org.ats.service.report.Report;
import org.ats.service.report.ReportService;
import org.ats.service.report.ReportService.Type;
import org.ats.services.OrganizationContext;
import org.ats.services.executor.ExecutorService;
import org.ats.services.executor.job.AbstractJob;
import org.ats.services.executor.job.KeywordJob;
import org.ats.services.keyword.CaseFactory;
import org.ats.services.keyword.CaseReference;
import org.ats.services.keyword.CaseService;
import org.ats.services.keyword.KeywordProject;
import org.ats.services.keyword.KeywordProjectFactory;
import org.ats.services.keyword.KeywordProjectService;
import org.ats.services.keyword.SuiteReference;
import org.ats.services.keyword.SuiteService;
import org.ats.services.organization.acl.Authenticated;
import org.ats.services.organization.entity.fatory.ReferenceFactory;

import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import actions.CorsComposition;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
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
  
  @Inject KeywordProjectFactory keywordProjectFactory;
  
  @Inject OrganizationContext context;
  
  @Inject ReferenceFactory<SuiteReference> suiteRefFactory;
  
  @Inject KeywordProjectService keywordProjectService;
  
  @Inject ExecutorService executorService;
  
  @Inject ReportService reportService;
  
  private SimpleDateFormat formater = new SimpleDateFormat("dd/MM/yyyy HH:mm");
  
  public Result list() {
    PageList<KeywordProject> list = keywordProjectService.list();
    ArrayNode array = Json.newObject().arrayNode();
    
    while(list.hasNext()) {
      for (KeywordProject project : list.next()) {
        project.put("type", "keyword");
        project.put("totalSuites", suiteService.getSuites(project.getId()).count());
        project.put("totalCases", caseService.getCases(project.getId()).count());
        PageList<AbstractJob<?>> jobList = executorService.query(new BasicDBObject("project_id", project.getId()), 1);
        jobList.setSortable(new MapBuilder<String, Boolean>("created_date", false).build());
        
        if (jobList.totalPage() > 0) {
          AbstractJob<?> lastJob = jobList.next().get(0);
          project.put("lastRunning", formater.format(lastJob.getCreatedDate()));
          project.put("job_id", lastJob.getId());
        }
        array.add(Json.parse(project.toString()));
      }
    }
    return ok(array);
  }
  
  public Result get(String projectId) {
    KeywordProject project = keywordProjectService.get(projectId);
    
    if (project == null) return status(404);
    
    project.put("type", "keyword");
    project.put("totalSuites", suiteService.getSuites(project.getId()).count());
    project.put("totalCases", caseService.getCases(project.getId()).count());
    
    PageList<AbstractJob<?>> jobList = executorService.query(new BasicDBObject("project_id", projectId), 1);
    jobList.setSortable(new MapBuilder<String, Boolean>("created_date", false).build());
    
    if (jobList.totalPage() > 0) {
      AbstractJob<?> lastJob = jobList.next().get(0);
      project.put("lastRunning", formater.format(lastJob.getCreatedDate()));
      project.put("log", lastJob.getLog());
      project.put("lastSuites", lastJob.get("suites"));
      project.put("job_id", lastJob.getId());
    }
    
    return ok(Json.parse(project.toString()));
  }

  public Result create() {
    JsonNode json = request().body().asJson();
    String name = json.get("name").asText();
    
    KeywordProject project = keywordProjectFactory.create(context, name);
    keywordProjectService.create(project);
    return status(201, project.getId());
  }
  
  public Result run(String projectId) throws Exception {
    JsonNode data = request().body().asJson();
    List<SuiteReference> suites = new ArrayList<SuiteReference>(data.size());
    for (JsonNode sel : data) {
      suites.add(suiteRefFactory.create(sel.asText()));
    }
    
    KeywordProject project = keywordProjectService.get(projectId);
    if (project == null) return status(404);
    
    if (project.getStatus() == KeywordProject.Status.RUNNING) return status(204);
    
    KeywordJob job = executorService.execute(project, suites);
    return status(201, Json.parse(job.toString()));
  }
  
  public Result report(String projectId,String jobId) throws Exception{                    
    
    PageList<Report> pages = reportService.getList(jobId, Type.FUNCTIONAL, null);
    
    ArrayNode array = Json.newObject().arrayNode();
    while (pages.hasNext()) {
       List<Report> list = pages.next();       
       for (Report report : list) {
         System.out.println(report.toString());
         array.add(Json.parse(report.toString()));   
         
       }
    }
    return status(200, array);    
  }
  
}
