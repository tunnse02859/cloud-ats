/**
 * TrinhTV3@fsoft.com.vn
 */
package org.ats.services.keyword.report.models;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.ats.services.data.common.Reference;
import org.ats.services.organization.entity.fatory.ReferenceFactory;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

/**
 * @author TrinhTV3
 *
 */
public class CaseReport extends BasicDBObject {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  
  private ReferenceFactory<StepReportReference> stepRefFactory;
  
  public CaseReport(String suite_report_id, String data_source, String name, String case_id, List<StepReportReference> steps) {
    this.put("_id", UUID.randomUUID().toString());
    this.put("suite_report_id", suite_report_id);
    this.put("data_source", data_source);
    this.put("name", name);
    this.put("case_id", case_id);
    BasicDBList list = new BasicDBList();
    for (StepReportReference step : steps) {
      list.add(step.toJSon());
    }
    this.put("steps", list);
  }
  
  public void setSteps(List<StepReportReference> steps) {
    BasicDBList list = new BasicDBList();
    for (StepReportReference ref : steps) {
      list.add(ref.toJSon());
    }
    this.put("steps", list);
  }
  
  public List<StepReportReference> getSteps() {
    
    BasicDBList list = this.get("steps") == null ? new BasicDBList() : (BasicDBList) this.get("steps");
    List<StepReportReference> steps = new ArrayList<StepReportReference>();
    for (Object obj : list) {
      
      steps.add(stepRefFactory.create(((BasicDBObject) obj).getString("_id")));
    }
    return steps;
    
  }
  
  public String getId() {
    return this.getString("_id");
  }
  
  public void setSuiteReportId(String suite_report_id) {
    this.put("suite_report_id", suite_report_id);
  }
  
  public String getSuiteReportId() {
    return this.getString("suite_report_id");
  }
  
  public void setDataSource(String data_source) {
    this.put("data_source", data_source);
  }
  
  public String getDataSource() {
    return this.get("data_source") == null ? null : this.getString("data_source");
  }
  
  public void setName(String name) {
    this.put("name", name);
  }
  
  public String getName(String name) {
    return this.getString("name");
  }
  
  public void setCaseId(String case_id) {
    this.put("case_id", case_id);
  }
  
  public String getCaseId() {
    return this.getString("case_id");
  }
  
}
