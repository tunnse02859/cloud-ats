/**
 * TrinhTV3@fsoft.com.vn
 */
package org.ats.services.keyword.report;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ats.common.PageList;
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
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

/**
 * @author TrinhTV3
 *
 */
public class KeywordReportService {
  
  @Inject CaseReportService caseReportService;
  
  @Inject SuiteReportService suiteReportService;
  
  @Inject StepReportService stepReportService;
  
  @Inject ReferenceFactory<CaseReportReference> caseReportRefFactory;
  
  @Inject ReferenceFactory<StepReportReference> stepReportRefFactory;
  
  @Inject SuiteReportFactory suiteReportFactory;
  
  @Inject CaseReportFactory caseReportFactory;
  
  @SuppressWarnings({ "rawtypes", "deprecation" })
  public void logParser(FileReader file) {
    BufferedReader br = null;
    String jobId = null;
    try {
      String currentLine;
      br = new BufferedReader(file);
      SuiteReport suiteReport = null;
      CaseReport caseReport = null;
      StepReport stepReport = null;
      List<SuiteReport> suites = new ArrayList<SuiteReport>();
      List<CaseReport> cases = new ArrayList<CaseReport>();
      List<StepReport> steps = new ArrayList<StepReport>();
      List<CaseReportReference> listCaseReportRef = null;
      String id = null;
      List<StepReportReference> listStepReportRef = null;
      List<String> dataSource = null;
      BasicDBList listParams = null;
      long start_time = 0;
      long end_time = 0;
      while ((currentLine = br.readLine()) != null) {
        ObjectMapper mapper = new ObjectMapper();
        if (currentLine.contains("[Start][Suite]")) {
          listCaseReportRef = new ArrayList<CaseReportReference>();
          int start = currentLine.indexOf("{");
          int end = currentLine.lastIndexOf("}");
          String obj = currentLine.substring(start, end + 1);
          JsonNode json = mapper.readTree(obj);
          String suiteId = json.get("id").asText();
          start_time = Long.parseLong(json.get("timestamp").asText());
          String suiteName = json.get("name").asText();
          jobId = json.get("jobId").asText();
          suiteReport = suiteReportFactory.create(start_time, jobId, suiteId, suiteName, 0, 0, 0, 0, listCaseReportRef, 0);
        }
        if (currentLine.contains("[Start][Case]")) {
          dataSource = new ArrayList<String>();
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
          id = json.get("id").toString();
          
          caseReport = caseReportFactory.create(suiteReport.getId(), "", json.get("name").asText(), json.get("id").asText(), listStepReportRef);
          caseReport.setDataSource(dataSource.toString());
          caseReport.put("isPass", false);
          cases.add(caseReport);
          CaseReportReference ref = caseReportRefFactory.create(caseReport.getId());
          listCaseReportRef.add(ref);
          caseReport.setSteps(listStepReportRef);
        }
        if (currentLine.contains("[End][Case]")) {
          caseReport.setSteps(listStepReportRef);
          cases.get(cases.size()-1).put("isPass", true);
          listStepReportRef.clear();
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
          if (listParams != null) {
            if (!listParams.isEmpty()) {
              steps.get(steps.size()-1).put("params", listParams);
            }
          }
          int start = currentLine.indexOf("{");
          int end = currentLine.lastIndexOf("}");
          String obj = currentLine.substring(start, end + 1);
          JsonNode json = mapper.readTree(obj);
          String name = json.get("keyword_type").asText();
          stepReport = new StepReport(name);
          stepReport.put("isPass", false);
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
              cases.get(cases.size()-1).setSteps(listStepReportRef);
            }
          }
        }
      }
      
      caseReportService.createCases(cases);
      suiteReportService.createSuites(suites);
      stepReportService.createSteps(steps);
      
      PageList<SuiteReport> pageSuites = suiteReportService.list();
      
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
      
      
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  
  @SuppressWarnings({ "rawtypes", "deprecation" })
  public void logParserByBuffer(BufferedReader br) {
    String jobId = null;
    try {
      String currentLine;
      SuiteReport suiteReport = null;
      CaseReport caseReport = null;
      StepReport stepReport = null;
      List<SuiteReport> suites = new ArrayList<SuiteReport>();
      List<CaseReport> cases = new ArrayList<CaseReport>();
      List<StepReport> steps = new ArrayList<StepReport>();
      List<CaseReportReference> listCaseReportRef = null;
      String id = null;
      List<StepReportReference> listStepReportRef = null;
      List<String> dataSource = null;
      BasicDBList listParams = null;
      long start_time = 0;
      long end_time = 0;
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
          id = json.get("id").toString();
          
          caseReport = caseReportFactory.create(suiteReport.getId(), "", json.get("name").asText(), json.get("id").asText(), listStepReportRef);
          caseReport.setDataSource(dataSource.toString());
          caseReport.put("isPass", false);
          cases.add(caseReport);
          CaseReportReference ref = caseReportRefFactory.create(caseReport.getId());
          listCaseReportRef.add(ref);
          caseReport.setSteps(listStepReportRef);
        }
        if (currentLine.contains("[End][Case]")) {
          caseReport.setSteps(listStepReportRef);
          cases.get(cases.size()-1).put("isPass", true);
          listStepReportRef.clear();
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
          if (listParams != null) {
            if (!listParams.isEmpty()) {
              steps.get(steps.size()-1).put("params", listParams);
            }
          }
          int start = currentLine.indexOf("{");
          int end = currentLine.lastIndexOf("}");
          String obj = currentLine.substring(start, end + 1);
          JsonNode json = mapper.readTree(obj);
          String name = json.get("keyword_type").asText();
          stepReport = new StepReport(name);
          stepReport.put("isPass", false);
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
              cases.get(cases.size()-1).setSteps(listStepReportRef);
            }
          }
        }
      }
      
      caseReportService.createCases(cases);
      suiteReportService.createSuites(suites);
      stepReportService.createSteps(steps);
      
      PageList<SuiteReport> pageSuites = suiteReportService.list();
      
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
      
      
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
