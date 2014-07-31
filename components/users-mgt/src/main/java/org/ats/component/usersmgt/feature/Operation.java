/**
 * 
 */
package org.ats.component.usersmgt.feature;

import org.ats.component.usersmgt.BaseObject;

import com.mongodb.DBObject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Jul 11, 2014
 */
public class Operation extends BaseObject<Operation> {

  private static final long serialVersionUID = 1L;

  public Operation(String name) {
    super();
    this.put("name", name);
  }
  
  public Operation(DBObject obj) {
    this.from(obj);
  }
  
  public void setName(String name) {
    this.put("name", name);
  }
  
  public String getName() {
    return (String) this.get("name");
  }

  @Override
  public Operation from(DBObject obj) {
    this.put("_id", obj.get("_id"));
    this.put("name", obj.get("name"));
    return this;
  }
}
