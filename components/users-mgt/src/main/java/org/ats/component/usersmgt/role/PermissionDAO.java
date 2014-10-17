/**
 * 
 */
package org.ats.component.usersmgt.role;

import org.ats.component.usersmgt.ManagementDAO;
import org.ats.component.usersmgt.UserManagementException;

import com.mongodb.DBObject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Jul 29, 2014
 */
public class PermissionDAO extends ManagementDAO<Permission> {

  private PermissionDAO(String dbName) {
    super(dbName, "permission");
  }
  
  public static PermissionDAO getInstance(String dbName) {
    return new PermissionDAO(dbName);
  }
  
  @Override
  public Permission transform(DBObject obj) throws UserManagementException {
    return obj == null ? null : new Permission().from(obj);
  }

}
