/**
 * 
 */
package org.ats.component.usersmgt.role;

import org.ats.component.usersmgt.BaseObject;
import org.ats.component.usersmgt.UserManagementException;
import org.ats.component.usersmgt.feature.Feature;
import org.ats.component.usersmgt.feature.FeatureDAO;
import org.ats.component.usersmgt.feature.Operation;
import org.ats.component.usersmgt.feature.OperationDAO;

import com.mongodb.DBObject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Jul 29, 2014
 */
public class Permission extends BaseObject<Permission>{

  private static final long serialVersionUID = 1L;
  
  public Permission() {}
  
  public Permission(String dbName, String featureId, String operationId) {
    super(dbName);
    this.put("feature_id", featureId);
    this.put("operation_id", operationId);
  }

  public Feature getFeature() throws UserManagementException {
    return FeatureDAO.getInstance(getDbName()).findOne(this.getString("feature_id"));
  }
  
  public Operation getOpertion() throws UserManagementException {
    return OperationDAO.getInstance(getDbName()).findOne(this.getString("operation_id"));
  }

  @Override
  public Permission from(DBObject obj) {
   this.putAll(obj);
    return this;
  }

}
