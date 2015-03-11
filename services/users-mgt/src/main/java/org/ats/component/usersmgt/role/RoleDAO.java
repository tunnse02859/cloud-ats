/**
 * 
 */
package org.ats.component.usersmgt.role;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ats.component.usersmgt.ManagementDAO;
import org.ats.component.usersmgt.UserManagementException;
import org.ats.component.usersmgt.user.User;
import org.ats.component.usersmgt.user.UserDAO;

import com.mongodb.DBObject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Jul 9, 2014
 */
public class RoleDAO extends ManagementDAO<Role> {

  private RoleDAO(String dbName) {
    super(dbName, "role");
  }
  
  public static RoleDAO getInstance(String dbName) {
    return new RoleDAO(dbName); 
  }
  
  public List<Permission> getPermissions(Role role) {
    if (role.get("permission_ids") == null) {
      return Collections.emptyList();
    }
    
    Set<String> permission_ids = role.stringIDtoSet(role.getString("permission_ids"));
    Set<Permission> permissions = new HashSet<Permission>();
    for (String permId : permission_ids) {
      try {
        Permission perm = PermissionDAO.getInstance(role.getDbName()).findOne(permId);
        permissions.add(perm);
      } catch (UserManagementException e) {
        e.printStackTrace();
        throw new RuntimeException(e);
      }
    }
    
    List<Permission> list = new ArrayList<Permission>(permissions);
    Collections.sort(list, new Comparator<Permission>() {
      public int compare(Permission o1, Permission o2) {
        return o1.getString("feature_id").compareTo(o2.getString("feature_id"));
      }
    });
    return list;
  }
  
  public List<User> getUsers(Role role) {
    if (role.get("user_ids") == null) {
      return Collections.emptyList();
    }
    
    Set<String> user_ids = role.stringIDtoSet(role.getString("user_ids"));
    Set<User> users = new HashSet<User>();
    for (String user_id : user_ids) {
      try {
        User user = UserDAO.getInstance(role.getDbName()).findOne(user_id);
        users.add(user);
      } catch (UserManagementException e) {
        e.printStackTrace();
        throw new RuntimeException(e);
      }
    }
    
    List<User> list = new ArrayList<User>(users);
    Collections.sort(list, new Comparator<User>() {
      public int compare(User o1, User o2) {
        return o1.getEmail().compareTo(o2.getEmail());
      }
    });
    return list;
  }
  
  @Override
  public Role transform(DBObject obj) throws UserManagementException {
    return obj == null ? null : new Role().from(obj);
  }
}
