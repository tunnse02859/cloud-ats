/**
 * 
 */
package org.ats.component.usersmgt.feature;

import org.ats.component.usersmgt.ManagementDAO;
import org.ats.component.usersmgt.UserManagementException;

import com.mongodb.DBObject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Jul 11, 2014
 */
public class OperationDAO extends ManagementDAO<Operation> {
  
  private OperationDAO(String dbName) {
    super(dbName, "operation");
  }
  
  public static OperationDAO getInstance(String dbName) {
    return new OperationDAO(dbName);
  }
  
  @Override
  public Operation transform(DBObject obj) throws UserManagementException {
    return obj == null ? null : new Operation().from(obj);
  }

}
