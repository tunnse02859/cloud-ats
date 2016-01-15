/**
 * 
 */
package org.ats.services.data.common;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Mar 11, 2015
 */
public abstract class Reference<T extends DBObject> {

  protected final String id;
  
  public Reference(String id) {
    this.id = id;
  }
  
  public abstract T get();
  
  public abstract T get(String... mixins);
  
  public String getId() {
    return this.id;
  }
  
  public DBObject toJSon() {
    return new BasicDBObject("_id", id);
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj == this) return true;
    if (obj instanceof Reference) {
      Reference<?> that = (Reference<?>) obj;
      return this.getId().equals(that.getId());
    }
    return false;
  }
  
}
