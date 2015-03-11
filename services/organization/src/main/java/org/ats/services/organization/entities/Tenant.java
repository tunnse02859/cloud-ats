/**
 * 
 */
package org.ats.services.organization.entities;

import java.util.UUID;

import com.mongodb.BasicDBObject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Mar 9, 2015
 */
public class Tenant extends BasicDBObject {

  /** .*/
  private static final long serialVersionUID = 1L;
  
  public Tenant(String name) {
    this.put("_id", UUID.randomUUID().toString());
    this.put("name", name);
  }

  public String getId() {
    return this.getString("_id");
  }
  
  public String getName() {
    return this.getString("name");
  }
  
  @SuppressWarnings("serial")
  public static class Reference extends BasicDBObject {
    
    public Reference(String id, String name) {
      this.put("_id", id);
      this.put("name", name);
    }
    
    public String getId() {
      return this.getString("_id");
    }
    
    public String getName() {
      return this.getString("name");
    }
  }
}
