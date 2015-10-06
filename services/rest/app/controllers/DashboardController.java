/**
 * TrinhTV3@fsoft.com.vn
 */
package controllers;

import java.util.Iterator;
import java.util.List;

import org.ats.common.MapBuilder;
import org.ats.common.PageList;
import org.ats.service.report.Report;
import org.ats.service.report.ReportService;
import org.ats.service.report.ReportService.Type;
import org.ats.service.report.function.SuiteReport;
import org.ats.services.OrganizationContext;
import org.ats.services.executor.ExecutorService;
import org.ats.services.executor.job.AbstractJob;
import org.ats.services.executor.job.PerformanceJob;
import org.ats.services.keyword.KeywordProject;
import org.ats.services.keyword.KeywordProjectService;
import org.ats.services.organization.acl.Authenticated;
import org.ats.services.organization.entity.Tenant;
import org.ats.services.performance.JMeterScriptReference;
import org.ats.services.performance.JMeterScriptService;
import org.ats.services.performance.PerformanceProject;
import org.ats.services.performance.PerformanceProjectService;

import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import actions.CorsComposition;

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
public class DashboardController extends Controller {
  
  @Inject OrganizationContext context;
  
  @Inject PerformanceProjectService performanceProjectService;
  
  @Inject JMeterScriptService jmeterService;
  
  @Inject ExecutorService executorService;
  
  @Inject KeywordProjectService keywordProjectService;
  
  @Inject ReportService reportService;
  
  
  public Result summary() throws Exception {
    
    Tenant currentTenant = context.getTenant();
    
    PageList<KeywordProject> listKeywordProjects = keywordProjectService.query(new BasicDBObject("tenant", new BasicDBObject("_id", currentTenant.getId())));
    ArrayNode arrayRecentKeywordProject = Json.newObject().arrayNode();
    // get list recent finished keyword project
    while(listKeywordProjects.hasNext()) {
      for (KeywordProject project : listKeywordProjects.next()) {
        ObjectNode object = null;
        BasicDBObject query = new BasicDBObject("project_id", project.getId()).append("status", AbstractJob.Status.Completed.toString());
        PageList<AbstractJob<?>> jobList = executorService.query(query, 1);
        jobList.setSortable(new MapBuilder<String, Boolean>("created_date", false).build());
        
        if (jobList.totalPage() > 0) {
          AbstractJob<?> lastJob = jobList.next().get(0);
          
          if(lastJob.getRawDataOutput() != null) {
            PageList<Report> pages = reportService.getList(lastJob.getId(), Type.FUNCTIONAL, null);
            
            if (pages.totalPage() > 0) {
              object = Json.newObject();
              object.put("x", project.getString("name"));
              Report report = pages.next().get(0);
              Iterator<SuiteReport> iterator = report.getSuiteReports().values().iterator();
              int totalPass = 0;
              int totalFail = 0;
              int totalSkip = 0;
              
              while (iterator.hasNext()) {
                SuiteReport suiteReport = iterator.next();
                totalPass += suiteReport.getTotalPass();
                totalFail += suiteReport.getTotalFail();
                totalSkip += suiteReport.getTotalSkip();
              }
              
              object.put("P", totalPass);
              object.put("F", totalFail);
              object.put("S", totalSkip);
              object.put("_id", project.getId());
              arrayRecentKeywordProject.add(object);
              
            }
          }
        }
        if (arrayRecentKeywordProject.size() == 10) {
          break;
        }
      }
    }
    
    // get all projects with percent pass and percent fail
    PageList<KeywordProject> listSortedKeywordProjects = keywordProjectService.query(new BasicDBObject("tenant", new BasicDBObject("_id", currentTenant.getId())));
    ArrayNode arraySortedKeywordProject = Json.newObject().arrayNode();
    
    while (listSortedKeywordProjects.hasNext()) {
      
      for (KeywordProject project : listSortedKeywordProjects.next()) {
        BasicDBObject query = new BasicDBObject("project_id", project.getId()).append("status", AbstractJob.Status.Completed.toString());
        PageList<AbstractJob<?>> jobList = executorService.query(query, 1);
        jobList.setSortable(new MapBuilder<String, Boolean>("created_date", false).build());
        
        ObjectNode object;
        if (jobList.totalPage() > 0) {
          AbstractJob<?> lastJob = jobList.next().get(0);
          
          if (lastJob.getRawDataOutput() != null) {
            PageList<Report> pages = reportService.getList(lastJob.getId(), Type.FUNCTIONAL, null);
            if (pages.totalPage() > 0) {
              
              object = Json.newObject();
              
              Report report = pages.next().get(0);
              object.put("_id", project.getId());
              object.put("name", project.getString("name"));
              Iterator<SuiteReport> iterator = report.getSuiteReports().values().iterator();
              int totalPass = 0;
              int totalFail = 0;
              int totalCases = 0;
              while (iterator.hasNext()) {
                SuiteReport suiteReport = iterator.next();
                totalPass += suiteReport.getTotalPass();
                totalFail += suiteReport.getTotalFail();
                totalCases += suiteReport.getTotalTestCase();
              }
              
              float percentPass = (totalPass * 100.0f) / totalCases;
              float percentFail = (totalFail * 100.0f) /totalCases; 
              
              object.put("percentPass", percentPass);
              object.put("percentFail", percentFail);
              object.put("totalCases", totalCases);
              arraySortedKeywordProject.add(object);
            }
          }
        }
      }
    }
    // get top error performance projects
    
    PageList<PerformanceProject> listPers = performanceProjectService.query(new BasicDBObject("tenant", new BasicDBObject("_id", currentTenant.getId())));
    ArrayNode arrayPersProject = Json.newObject().arrayNode();
    
    while (listPers.hasNext()) {
      for (PerformanceProject project : listPers.next()) {
        BasicDBObject query = new BasicDBObject("project_id", project.getId()).append("status", AbstractJob.Status.Completed.toString());
        PageList<AbstractJob<?>> jobList = executorService.query(query, 1);
        jobList.setSortable(new MapBuilder<String, Boolean>("created_date", false).build());
        ObjectNode object = null;
        if (jobList.totalPage() > 0) {
          
          AbstractJob<?> lastJob = jobList.next().get(0);
          
          PerformanceJob job = (PerformanceJob) executorService.get(lastJob.getId());
          
          double errorPercent = 0;
          
          int numberOfUser = 0;
          int numberOfScript = job.getScripts().size();
          int numberOfSamples = 0;
          PageList<Report> pages;
          for (JMeterScriptReference script : job.getScripts()) {
            try {
              pages = reportService.query(new BasicDBObject("performane_job_id", lastJob.getId()).append("script_id", script.getId()).append("label", "*SummaryReport*"));
              if (pages.totalPage() > 0) {
                object = Json.newObject();
                List<Report> list = pages.next();
                Report report = list.get(0);
                numberOfUser += script.get().getNumberThreads();
                errorPercent += report.getSummaryReport().getErrorPercent();
                numberOfSamples += report.getSummaryReport().getSamples();
                
                object.put("_id", project.getId());
                object.put("projectName", project.getName());
                object.put("ram_up", script.get().getRamUp());
                object.put("loops", script.get().getLoops());
                object.put("duration", script.get().getDuration());
                
              }
            } catch (Exception e) {
              e.printStackTrace();
            }
            
          }
          if (object != null) {
            
            double errorPercentCoverage = errorPercent / numberOfScript;
            if (errorPercentCoverage != 0 ) {
              object.put("samples", numberOfSamples);
              object.put("error_percent", errorPercentCoverage);
              object.put("users", numberOfUser);
              arrayPersProject.add(object);
            }
          }
        }
      }
    }
    
    ObjectNode object = Json.newObject();
    object.put("recentProjects", arrayRecentKeywordProject.toString());
    object.put("percentsProjects", arraySortedKeywordProject.toString());
    object.put("persProjects", arrayPersProject.toString());
    
    return ok(object);
  }
}
