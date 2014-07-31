/**
 * 
 */
package org.ats.component.usersmgt.user;

import org.ats.component.usersmgt.ManagementDAO;
import org.ats.component.usersmgt.UserManagementException;

import com.mongodb.DBObject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Jul 7, 2014
 */
public class UserDAO extends ManagementDAO<User> {
  
  public static final UserDAO INSTANCE = new UserDAO();

  public UserDAO() {
    super("user");
  }
  
  @Override
  public User transform(DBObject obj) throws UserManagementException {
    return obj == null ? null : new User(obj);
  }

}
