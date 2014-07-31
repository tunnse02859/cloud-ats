/**
 * 
 */
package org.ats.component.usersmgt.role;

import org.ats.component.usersmgt.BaseObject;

import com.mongodb.DBObject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Jul 29, 2014
 */
public class Permission extends BaseObject<Permission>{

  private static final long serialVersionUID = 1L;
  
  public Permission(String featureId, String operationId) {
    super();
    this.put("feature_id", featureId);
    this.put("operation_id", operationId);
  }
  
  public Permission(DBObject obj) {
    this.from(obj);
  }

  @Override
  public Permission from(DBObject obj) {
    if (obj == null) throw new NullPointerException("Object source is not allowed nullable");
    this.put("_id", obj.get("_id"));
    this.put("feature_id", obj.get("feature_id"));
    this.put("operation_id", obj.get("operation_id"));
    return this;
  }

}
