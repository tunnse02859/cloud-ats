/**
 * TrinhTV3@fsoft.com.vn
 */
package controllers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
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
  
  private SimpleDateFormat formater = new SimpleDateFormat("dd/MM/yyyy HH:mm");
  
  public Result summary() throws Exception {
    
    Tenant currentTenant = context.getTenant();
    
    // get list recent finished keyword project
    PageList<KeywordProject> listKeywordProjects = keywordProjectService.query(new BasicDBObject("tenant", new BasicDBObject("_id", currentTenant.getId())));
    ArrayNode arrayRecentKeywordProject = Json.newObject().arrayNode();
    
    List<ObjectNode> listRecentProject = new LinkedList<ObjectNode>();
    
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
              object.put("created_date", formater.format(lastJob.getCreatedDate()));
              object.put("P", totalPass);
              object.put("F", totalFail);
              object.put("S", totalSkip);
              object.put("_id", project.getId());
              
              insert(listRecentProject, object, "created_date", true, 10, true);
              
            }
          }
        }
      }
    }
    
    // get all projects passed by desc order
    PageList<KeywordProject> listSortedByDescKeywordProjects = keywordProjectService.query(new BasicDBObject("tenant", new BasicDBObject("_id", currentTenant.getId())));
    LinkedList<ObjectNode> topKeywordListPass = new LinkedList<ObjectNode>();
    getProjects(listSortedByDescKeywordProjects, topKeywordListPass, "percentPass", true, 5);
    
    // get all projects pass by asc order
    PageList<KeywordProject> listSortedByAscKeywordProjects = keywordProjectService.query(new BasicDBObject("tenant", new BasicDBObject("_id", currentTenant.getId())));
    LinkedList<ObjectNode> topKeywordListFail = new LinkedList<ObjectNode>();
    getProjects(listSortedByAscKeywordProjects, topKeywordListFail, "percentPass", false, 5);
    
    // get all projects by number of test case
    PageList<KeywordProject> listSortedByTestCaseKeywordProjects = keywordProjectService.query(new BasicDBObject("tenant", new BasicDBObject("_id", currentTenant.getId())));
    LinkedList<ObjectNode> topBiggestKeyword = new LinkedList<ObjectNode>();
    getProjects(listSortedByTestCaseKeywordProjects, topBiggestKeyword, "totalCases", true, 5);
    
    // get top error performance projects
    PageList<PerformanceProject> listPers = performanceProjectService.query(new BasicDBObject("tenant", new BasicDBObject("_id", currentTenant.getId())));
    LinkedList<ObjectNode> topErrorPerformanceProjects = new LinkedList<ObjectNode>();
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
//                object.put("duration", script.get().getDuration());
                object.put("number_engines", script.get().getNumberEngines());
                
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
              insert(topErrorPerformanceProjects, object, "error_percent", true, 10, false);
            }
          }
        }
      }
    }
    
    ArrayNode arraySortedByDescKeywordProject = Json.newObject().arrayNode();
    for (ObjectNode obj : topKeywordListPass) {
      arraySortedByDescKeywordProject.add(obj);
    }
    ArrayNode arraySortedByAscKeywordProject = Json.newObject().arrayNode();
    for (ObjectNode obj : topKeywordListFail) {
      arraySortedByAscKeywordProject.add(obj);
    }
    ArrayNode arraySortedByTestCaseKeywordProject = Json.newObject().arrayNode();
    for (ObjectNode obj : topBiggestKeyword) {
      arraySortedByTestCaseKeywordProject.add(obj);
    }
    ArrayNode arrayPersProject = Json.newObject().arrayNode();
    for (ObjectNode obj : topErrorPerformanceProjects) {
      arrayPersProject.add(obj);
    }
    
    for (ObjectNode obj : listRecentProject) {
      arrayRecentKeywordProject.add(obj);
    }
    
    ObjectNode object = Json.newObject();
    object.put("recentProjects", arrayRecentKeywordProject.toString());
    object.put("topKeywordPass", arraySortedByDescKeywordProject.toString());
    object.put("topKeywordFail", arraySortedByAscKeywordProject.toString());
    object.put("topBiggestProject", arraySortedByTestCaseKeywordProject.toString());
    object.put("persProjects", arrayPersProject.toString());
    
    return ok(object);
  }
  
  private void sortByDate (List<ObjectNode> source, final String asertionText, final boolean desc, boolean sortByDate) {
    
    if (!sortByDate) {
      Collections.sort(source,  new Comparator<ObjectNode>() {

        @Override
        public int compare(ObjectNode o1, ObjectNode o2) {
          return desc ? (int) (o2.get(asertionText).asDouble() - o1.get(asertionText).asDouble()) : (int) (o1.get(asertionText).asDouble() - o2.get(asertionText).asDouble());
        }
      });
    } else {
      Collections.sort(source, new Comparator<ObjectNode>() {
  
        @Override
        public int compare(ObjectNode o1, ObjectNode o2) {
          
          try {
            long t2 = formater.parse(o2.get(asertionText).asText()).getTime();
            long t1 = formater.parse(o1.get(asertionText).asText()).getTime();
            
            return desc ? (int) (t2 -t1) : (int) (t1 - t2);
          } catch (ParseException e) {
            e.printStackTrace();
          }
          return 0;
        }
        
      });
    }
  }
  
  private void sort(List<ObjectNode> source, final String asertionText, final boolean desc) {
    sortByDate(source, asertionText, desc, false);
  }
  
  private List<ObjectNode> insert (List<ObjectNode> source, ObjectNode insertion, final String asertionText, boolean desc, int numberOfElement, boolean insertByDate) {
    if (source.size() == 0) {
      source.add(insertion);
      return source;
      
    } else if (source.size() < numberOfElement) {
      source.add(insertion);
      if (!insertByDate) sort(source, asertionText, desc);
      else sortByDate(source, asertionText, desc, true);
      return source;
      
    } else if (source.size() == numberOfElement) {
      source.add(insertion);
      if (!insertByDate) sort(source, asertionText, desc);
      else sortByDate(source, asertionText, desc, true);
      source.remove(numberOfElement);
      return source;
      
    } else {
      throw new IllegalStateException("The list has more than "+numberOfElement+" items");
    }
  }
  
  private List<ObjectNode> getProjects(PageList<KeywordProject> list, List<ObjectNode> objectList, final String assertionText, boolean desc, int numberOfElement) throws Exception {
    while (list.hasNext()) {
      for (KeywordProject project : list.next()) {
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
                
              double percentPass = (totalPass * 100.0) / totalCases;
              double percentFail = (totalFail * 100.0) /totalCases; 
                
              object.put("percentPass", percentPass);
              object.put("percentFail", percentFail);
              object.put("totalCases", totalCases);
  
              insert(objectList, object, assertionText, desc, numberOfElement, false);
            }
          }
        }
      }
    }
    return objectList;
  }
  
}
