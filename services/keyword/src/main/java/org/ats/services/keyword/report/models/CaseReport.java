/**
 * TrinhTV3@fsoft.com.vn
 */
package org.ats.services.keyword.report.models;

import java.util.UUID;

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
  
  public CaseReport(String suite_report_id, String data_source, String name, String case_id) {
    this.put("_id", UUID.randomUUID().toString());
    this.put("suite_report_id", suite_report_id);
    this.put("data_source", data_source);
    this.put("name", name);
    this.put("case_id", case_id);
    
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
