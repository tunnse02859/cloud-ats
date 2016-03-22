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
public class SuiteReport extends BasicDBObject {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  
  @Inject private ReferenceFactory<CaseReportReference> caseReportRefFactory;
  
  @Inject
  public SuiteReport(@Assisted("created_date") long created_date, @Assisted("jobId")String jobId, @Assisted("suiteId") String suiteId, @Assisted("name") String name, @Assisted("totalPass") int totalPass, @Assisted("totalFail") int totalFail, @Assisted("totalSkip") int totalSkip, @Assisted("totalCase") int totalCase, @Assisted("cases") List<CaseReportReference> cases, @Assisted("duration") long duration) {
    this.put("_id", UUID.randomUUID().toString());
    this.put("name", name);
    this.put("totalPass", totalPass);
    this.put("totalFail", totalFail);
    this.put("totalSkip", totalSkip);
    this.put("jobId", jobId);
    this.put("totalCase", totalCase);
    this.put("suiteId", suiteId);
    this.put("created_date", created_date);
    BasicDBList list = new BasicDBList();
    for (CaseReportReference caze : cases) {
      list.add(caze.toJSon());
    }
    this.put("cases", list);
    this.put("duration", duration);
  }
  
  public long getDuration() {
    return this.getLong("duration");
  }
  
  public long getCreatedDate() {
    return this.getLong("created_date");
  }
  
  public void setCreatedDate(long time) {
    this.put("created_date", time);
  }
  
  public String getSuiteId() {
    return this.getString("suiteId");
  }
  
  public void setSuiteId(String id) {
    this.put("suiteId", id);
  }
  
  public void setDuration(long time) {
    this.put("duration", time);
  }
  
  public String getId() {
    return this.getString("_id");
  }
  
  public void setId(String id) {
    this.put("_id", id);
  }
  
  public void setTotalCase(int totalCase) {
    this.put("totalCase", totalCase);
  }
  
  public int getTotalCase() {
    return this.getInt("totalCase");
  }
  
  public String getName() {
    return this.getString("name");
  }
  
  public void setName(String name) {
    this.put("name", name);
  }
  
  public String getJobId() {
    return this.getString("jobId");
  }
  
  public void setJobId(String id) {
    this.put("jobId", id);
  }
  
  public int getTotalPass() {
    return this.getInt("totalPass");
  }
  
  public void setTotalPass(int totalPass) {
    this.put("totalPass", totalPass);
  }
  
  public int getTotalFail() {
    return this.getInt("totalFail");
  }
  
  public void setTotalSkip(int totalSkip) {
    this.put("totalSkip", totalSkip);
  }
  
  public int getTotalSkip() {
    return this.getInt("totalSkip");
  }
  
  public void setCases(List<CaseReportReference> cases) {
    BasicDBList list = new BasicDBList();
    for (CaseReportReference ref : cases) {
      list.add(ref.toJSon());
    }
    
    this.put("cases", list);
  }
  
  public List<CaseReportReference> getCases() {
    
    BasicDBList list = (BasicDBList) this.get("cases");
    List<CaseReportReference> cases = new ArrayList<CaseReportReference>();
    for (Object obj : list) {
      String id = ((BasicDBObject) obj).getString("_id");
      CaseReportReference ref = caseReportRefFactory.create(id);
      cases.add(ref);
    }
    return cases;
  }
  
}
