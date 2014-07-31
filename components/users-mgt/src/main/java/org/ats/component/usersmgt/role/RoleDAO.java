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
 * Jul 9, 2014
 */
public class RoleDAO extends ManagementDAO<Role> {

  public static final RoleDAO INSTANCE = new RoleDAO();
  
  public RoleDAO() {
    super("role");
  }
  
  @Override
  public Role transform(DBObject obj) throws UserManagementException {
    return obj == null ? null : new Role(obj);
  }
}
