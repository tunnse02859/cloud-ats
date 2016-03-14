/**
 * TrinhTV3@fsoft.com.vn
 */
package org.ats.services.keyword.report.models;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.ats.services.keyword.CaseReference;
import org.ats.services.organization.entity.fatory.ReferenceFactory;

import com.google.inject.Inject;
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
  
  @Inject ReferenceFactory<CaseReference> caseRefFactory;
  
  public SuiteReport(String jobId, String name, int totalPass, int totalFail, int totalSkip, List<CaseReference> cases) {
    this.put("_id", UUID.randomUUID().toString());
    this.put("name", name);
    this.put("totalPass", totalPass);
    this.put("totalFail", totalFail);
    this.put("totalSkip", totalSkip);
    this.put("jobId", jobId);
    BasicDBList list = new BasicDBList();
    for (CaseReference caze : cases) {
      list.add(caze.toJSon());
    }
    this.put("cases", list);
  }
  
  public String getId() {
    return this.getString("_id");
  }
  
  public void setId(String id) {
    this.put("_id", id);
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
  
  public void setCases(List<CaseReference> cases) {
    BasicDBList list = new BasicDBList();
    for (CaseReference ref : cases) {
      list.add(ref.toJSon());
    }
    
    this.put("cases", list);
  }
  
  public List<CaseReference> getCases() {
    
    BasicDBList list = (BasicDBList) (this.get("cases") == null ? new BasicDBList() : this.get("cases"));
    List<CaseReference> cases = new ArrayList<CaseReference>();
    for(Object obj : list) {
      cases.add(caseRefFactory.create(((BasicDBObject) obj).getString("_id")));
    }
    return cases;
  }
  
}
