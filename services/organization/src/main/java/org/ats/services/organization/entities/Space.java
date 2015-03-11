/**
 * 
 */
package org.ats.services.organization.entities;

import com.mongodb.BasicDBObject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Mar 9, 2015
 */
public class Space extends BasicDBObject {

  /** .*/
  private static final long serialVersionUID = 1L;


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
