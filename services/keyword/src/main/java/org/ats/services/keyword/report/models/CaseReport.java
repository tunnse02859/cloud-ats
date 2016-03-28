/**
 * TrinhTV3@fsoft.com.vn
 */
package org.ats.services.keyword.report.models;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.ats.services.organization.entity.fatory.ReferenceFactory;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

/**
 * @author TrinhTV3
 *
 */
@SuppressWarnings("serial")
public class CaseReport extends BasicDBObject {

  /**
   * 
   */
  @Inject
  private ReferenceFactory<StepReportReference> stepRefFactory;
  
  @Inject
  public CaseReport(@Assisted("suite_report_id") String suite_report_id, @Assisted("data_source") String data_source, @Assisted("name") String name, @Assisted("case_id") String case_id, @Assisted("steps") List<StepReportReference> steps,@Assisted("startTime") long startTime) {
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
    this.put("startTime", startTime);
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
  
  public void setStartTime(long start) {
    this.put("startTime", start);
  }
  
  public long getStartTime() {
    return this.get("startTime") == null ? null : this.getLong("startTime");
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
  
  public String getName() {
    return this.getString("name");
  }
  
  public void setCaseId(String case_id) {
    this.put("case_id", case_id);
  }
  
  public String getCaseId() {
    return this.getString("case_id");
  }
  
}
