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

  public static final PermissionDAO INSTANCE = new PermissionDAO();
  
  public PermissionDAO() {
    super("permission");
  }
  
  @Override
  public Permission transform(DBObject obj) throws UserManagementException {
    return obj == null ? null : new Permission(obj);
  }

}
