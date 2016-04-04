/**
 * TrinhTV3@fsoft.com.vn
 */
package org.ats.services.keyword.report;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ats.common.PageList;
import org.ats.services.keyword.Case;
import org.ats.services.keyword.CaseService;
import org.ats.services.keyword.report.models.CaseReport;
import org.ats.services.keyword.report.models.CaseReportFactory;
import org.ats.services.keyword.report.models.CaseReportReference;
import org.ats.services.keyword.report.models.StepReport;
import org.ats.services.keyword.report.models.StepReportReference;
import org.ats.services.keyword.report.models.SuiteReport;
import org.ats.services.keyword.report.models.SuiteReportFactory;
import org.ats.services.organization.entity.fatory.ReferenceFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.util.JSON;

/**
 * @author TrinhTV3
 *
 */
@Singleton
public class KeywordReportService {
  
  @Inject CaseReportService caseReportService;
  
  @Inject SuiteReportService suiteReportService;
  
  @Inject StepReportService stepReportService;
  
  @Inject ReferenceFactory<CaseReportReference> caseReportRefFactory;
  
  @Inject ReferenceFactory<StepReportReference> stepReportRefFactory;
  
  @Inject SuiteReportFactory suiteReportFactory;
  
  @Inject CaseReportFactory caseReportFactory;

  @Inject CaseService caseService;
  
  public void processLog(InputStream is) throws IOException {
    BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
    String jobId = null;
    String currentLine = null;
    try {
      SuiteReport suiteReport = null;
      CaseReport caseReport = null;
      StepReport stepReport = null;
      List<SuiteReport> suites = new ArrayList<SuiteReport>();
      List<CaseReport> cases = new ArrayList<CaseReport>();
      List<StepReport> steps = new ArrayList<StepReport>();
      List<CaseReportReference> listCaseReportRef = null;
      List<StepReportReference> listStepReportRef = null;
      List<String> dataSource = null;
      BasicDBList listParams = null;
      long start_time = 0;
      long end_time = 0;
      StringBuilder sb = null;
      while ((currentLine = br.readLine()) != null) {
        ObjectMapper mapper = new ObjectMapper();
        if (currentLine.contains("[Start][Suite]")) {
          listCaseReportRef = new ArrayList<CaseReportReference>();
          int start = currentLine.indexOf("{");
          int end = currentLine.lastIndexOf("}");
          String obj = currentLine.substring(start, end + 1);
          JsonNode json = mapper.readTree(obj);
          
          start_time = Long.parseLong(json.get("timestamp").asText());
          String suiteName = json.get("name").asText();
          String suiteId = json.get("id").asText();
          jobId = json.get("jobId").asText();
          suiteReport = suiteReportFactory.create(start_time, jobId, suiteId, suiteName, 0, 0, 0, 0, listCaseReportRef, 0);
        }
        if (currentLine.contains("[Start][Case]")) {
          dataSource = new ArrayList<String>();
          
          if (steps.size() > 0) {
            if (steps.get(steps.size() - 1).get("isPass") == null){ 
              steps.get(steps.size() -1).put("isPass", false);
              cases.get(cases.size()-1).put("isPass", false);
            }
          }
          
          if (listStepReportRef != null) {
            if (!listStepReportRef.isEmpty()) {
              cases.get(cases.size()-1).setSteps(listStepReportRef);
            }
          }
          
          listStepReportRef = new ArrayList<StepReportReference>();
          int start = currentLine.indexOf("{");
          int end = currentLine.lastIndexOf("}");
          String obj = currentLine.substring(start, end + 1);
          JsonNode json = mapper.readTree(obj);
          long timeStamp = Long.parseLong(json.get("timestamp").asText());
          caseReport = caseReportFactory.create(suiteReport.getId(), "", json.get("name").asText(), json.get("id").asText(), listStepReportRef, timeStamp);
          caseReport.setDataSource(dataSource.toString());
          caseReport.put("isPass", null);
          cases.add(caseReport);
          CaseReportReference ref = caseReportRefFactory.create(caseReport.getId());
          listCaseReportRef.add(ref);
          //caseReport.setSteps(listStepReportRef);
        }
        if (currentLine.contains("[End][Case]")) {
          caseReport.setSteps(listStepReportRef);
          if (cases.get(cases.size() - 1).get("isPass") == null) {
            cases.get(cases.size()-1).put("isPass", true);
            listStepReportRef.clear();
          }
        }
        
        if (currentLine.contains("[Start][Data]")) {
          dataSource.clear();
          int start = currentLine.indexOf("{");
          int end = currentLine.lastIndexOf("}");
          String obj = currentLine.substring(start, end + 1);
          dataSource.add(obj);
          cases.get(cases.size() - 1).put("data_source", dataSource.toString()); 
        }
        if (currentLine.contains("[Start][Step]")) {
          
          if (steps.size() > 0) {
            if (steps.get(steps.size() - 1).get("isPass") == null){ 
              steps.get(steps.size() -1).put("isPass", false);
              cases.get(cases.size()-1).put("isPass", false);
            }
          }
          if (listParams != null) {
            if (!listParams.isEmpty()) {
              steps.get(steps.size()-1).put("params", listParams);
            }
          }
          sb = new StringBuilder();
          int start = currentLine.indexOf("{");
          int end = currentLine.lastIndexOf("}");
          String obj = currentLine.substring(start, end + 1);
          
          JsonNode json = mapper.readTree(obj);
          
          String name = json.get("keyword_type").asText();
          long timestamp = Long.parseLong(json.get("timestamp").asText());
          stepReport = new StepReport(name, timestamp);
          stepReport.put("isPass", null);
          ArrayNode params = (ArrayNode) json.get("params");
          listParams = new BasicDBList();
          for (JsonNode j : params) {
            String param = j.asText();
            BasicDBObject object;
            if ("locator".equals(param)) {
              JsonNode locator = json.get(param);
              String locatorType =  locator.get("type").asText();
              String locatorValue = locator.get("value").asText();
              object = new BasicDBObject("type", locatorType).append("value", locatorValue);
            } else {
              String value = json.get(param).asText();
              object = new BasicDBObject(param, value);
            }
              
            listParams.add(object);
          }
          
          stepReport.put("params", listParams);
          StepReportReference ref = stepReportRefFactory.create(stepReport.getId());
          listStepReportRef.add(ref);
          steps.add(stepReport);
        }
        
        if (currentLine.contains("[End][Step]")) {
          steps.get(steps.size() -1).put("isPass", true);
        }
        
        if (currentLine.contains("[End][Data]")) {
          
        }
        if (currentLine.contains("[End][Suite]")) {
          int start = currentLine.indexOf("{");
          int end = currentLine.lastIndexOf("}");
          String obj = currentLine.substring(start, end + 1);
          JsonNode json = mapper.readTree(obj);
          end_time = Long.parseLong(json.get("timestamp").asText());
          long duration = end_time - start_time;
          
          suiteReport.setDuration(duration);
          suiteReport.setCases(listCaseReportRef);
          suites.add(suiteReport);
          
          if (listStepReportRef != null) {
            if (!listStepReportRef.isEmpty()) {
              cases.get(cases.size() - 1).setSteps(listStepReportRef);
            }
          }
          if (steps.get(steps.size() - 1).get("isPass") == null) {
            steps.get(steps.size() - 1).put("isPass", false);
            cases.get(cases.size()-1).put("isPass", false);
          }
        }
        if (   !currentLine.contains("[Start][Suite]") 
            && !currentLine.contains("[Start][Case]")
            && !currentLine.contains("[Start][Step]")
            && !currentLine.contains("[End][Case]")
            && !currentLine.contains("[End][Suite]")
            && !currentLine.contains("[End][Step]")
            && !currentLine.contains("[Start][Data]")
            && !currentLine.contains("[End][Data]")) {
          if (steps.size() > 0) {
            if (steps.get(steps.size() -1).get("isPass") == null){
              sb.append(currentLine);
              steps.get(steps.size() - 1).put("output", sb.toString());
            }
            
          }
        }
      }
      
      //save skipped step for each case
      for (CaseReport report : cases) {
        int index = report.getSteps().size();
        Case caze = caseService.get(report.getCaseId());
        List<JsonNode> full_step = caze.getActions();
        List<JsonNode> skipped_step = full_step.subList(index, full_step.size());
        
        BasicDBList list = new BasicDBList();
        for (JsonNode action : skipped_step) {
          list.add(JSON.parse(action.toString()));
        }
       
        report.put("skipped_steps", list);
      }
      
      caseReportService.createCases(cases);
      suiteReportService.createSuites(suites);
      stepReportService.createSteps(steps);
      
      
      
      PageList<SuiteReport> pageSuites = suiteReportService.query(new BasicDBObject("jobId", jobId));
      
      while (pageSuites.hasNext()) {
        for (SuiteReport suite : pageSuites.next()) {
          
          Set<String> set = new HashSet<String>();
          for (CaseReportReference ref : suite.getCases()) {
            CaseReport report = ref.get();
            set.add(report.getCaseId());
          }
          
          suite.put("totalCase", set.size());
          int totalPass = set.size();
          for (String s : set) {
            PageList<CaseReport> listCase = caseReportService.query(new BasicDBObject("isPass", false).append("suite_report_id", suite.getId()).append("case_id", s));
            
            if (listCase.count() > 0) {
              totalPass -= 1;
            }
          }
          suite.put("totalPass", totalPass);
          suite.put("totalFail", set.size() - totalPass);
          suiteReportService.update(suite);
        }
      }
      
      br.close();
    } catch (IOException e) {
      e.printStackTrace();
      throw e;
    }
  }
  
  public void processLog(byte[] bytes) throws IOException {
    InputStream is = new ByteArrayInputStream(bytes);
    processLog(is);
  }
}
