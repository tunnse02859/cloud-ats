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
  
  public void logParser(FileReader file) {
    BufferedReader br = null;
    String jobId = null;
    try {
      String currentLine;
      br = new BufferedReader(file);
      SuiteReport suiteReport = null;
      CaseReport caseReport = null;
      List<SuiteReport> suites = new ArrayList<SuiteReport>();
      List<CaseReport> cases = new ArrayList<CaseReport>();
      List<StepReport> steps = new ArrayList<StepReport>();
      List<CaseReportReference> listCaseReportRef = null;
      String id = null;
      List<StepReportReference> listStepReportRef = null;
      List<String> dataSource = null;
      while ((currentLine = br.readLine()) != null) {
        ObjectMapper mapper = new ObjectMapper();
        if (currentLine.contains("[Start][Suite]")) {
          listCaseReportRef = new ArrayList<CaseReportReference>();
          int start = currentLine.indexOf("{");
          int end = currentLine.lastIndexOf("}");
          String obj = currentLine.substring(start, end + 1);
          JsonNode json = mapper.readTree(obj);
          String suiteName = json.get("name").asText();
          jobId = json.get("jobId").asText();
          suiteReport = suiteReportFactory.create(jobId, suiteName, 0, 0, 0, 0, listCaseReportRef);
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
          int start = currentLine.indexOf("{");
          int end = currentLine.lastIndexOf("}");
          String obj = currentLine.substring(start, end + 1);
          JsonNode json = mapper.readTree(obj);
          String name = json.get("name").asText();
          StepReport step = new StepReport(name);
          step.put("isPass", false);
          JsonNode params = json.get("params");
          BasicDBList list = new BasicDBList();
          for (JsonNode j : params) {
            BasicDBObject object = new BasicDBObject();
            String value = json.get(j.asText()).asText();
            object.put(j.asText(), value);
            list.add(object);
          }
          
          step.put("params", list);
          StepReportReference ref = stepReportRefFactory.create(step.getId());
          listStepReportRef.add(ref);
          steps.add(step);
        }
        
        if (currentLine.contains("[End][Step]")) {
          steps.get(steps.size() -1).put("isPass", true);
        }
        
        if (currentLine.contains("[End][Data]")) {
          
        }
        if (currentLine.contains("[End][Suite]")) {
          suiteReport.setCases(listCaseReportRef);
          suites.add(suiteReport);
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
          suiteReportService.update(suite);
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
