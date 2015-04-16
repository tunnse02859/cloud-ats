/**
 * 
 */
package org.ats.services.organization.entity;

import com.mongodb.BasicDBObject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Apr 14, 2015
 */
@SuppressWarnings("serial")
public abstract class AbstractEntity<T extends AbstractEntity<T>> extends BasicDBObject {

  public void setActive(boolean active) {
    this.put("active", active);
  }
  
  public boolean isActive() {
    return this.getBoolean("active");
  }
}
