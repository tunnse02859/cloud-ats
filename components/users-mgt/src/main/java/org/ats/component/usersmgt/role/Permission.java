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
   this.putAll(obj);
    return this;
  }

}
