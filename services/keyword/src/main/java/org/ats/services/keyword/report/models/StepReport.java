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
public class StepReport extends BasicDBObject {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  
  public StepReport(String name) {
    this.put("_id", UUID.randomUUID().toString());
    this.put("name", name);
  }
  
  public void setName(String name) {
    this.put("name", name);
  }
  
  public String getName() {
    return this.getString("name");
  }
  
}
