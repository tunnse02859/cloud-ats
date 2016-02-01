/**
 * TrinhTV3@fsoft.com.vn
 */
package org.ats.services.performance;

import java.util.UUID;

import com.mongodb.BasicDBObject;

/**
 * @author TrinhTV3
 *
 */
public class CSV extends BasicDBObject {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  /**
   * 
   */
  public CSV(String id, String name) {
    this.put("_id", id);
    this.put("name", name);
  }
  
  public CSV(String name) {
    this.put("_id", UUID.randomUUID().toString());
    this.put("name", name);
  }
  
  public void setName(String name) {
    this.put("name", name);
  }
  
  public String getName() {
    return this.getString("name");
  }
  
  public void setId(String id) {
    this.put("_id", id);
  }
  
  public String getId() {
    return this.getString("_id");
  }
}
