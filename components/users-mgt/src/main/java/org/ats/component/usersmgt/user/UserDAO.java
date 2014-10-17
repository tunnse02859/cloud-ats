/**
 * 
 */
package org.ats.component.usersmgt.user;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ats.component.usersmgt.ManagementDAO;
import org.ats.component.usersmgt.UserManagementException;
import org.ats.component.usersmgt.group.Group;
import org.ats.component.usersmgt.group.GroupDAO;
import org.ats.component.usersmgt.role.Role;
import org.ats.component.usersmgt.role.RoleDAO;

import com.mongodb.DBObject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Jul 7, 2014
 */
public class UserDAO extends ManagementDAO<User> {
  
  private UserDAO(String dbName) {
    super(dbName, "user");
  }
  
  public static UserDAO getInstance(String dbName) {
    return new UserDAO(dbName);
  }
  
  public List<Group> getGroups(User user) {
    if (user.get("group_ids") == null) return Collections.emptyList();
    
    Set<String> group_ids = user.stringIDtoSet(user.getString("group_ids"));
    Set<Group> groups = new HashSet<Group>();
    for (String group_id : group_ids) {
      try {
        Group group = GroupDAO.getInstance(user.getDbName()).findOne(group_id);
        groups.add(group);
      } catch (UserManagementException e) {
        e.printStackTrace();
        throw new RuntimeException(e);
      }
    }
    List<Group> list = new ArrayList<Group>(groups);
    Collections.sort(list, new Comparator<Group>() {
      public int compare(Group o1, Group o2) {
        return o1.getString("name").compareTo(o2.getString("name"));
      }
    });
    return list;
  }
  
  public List<Role> getRoles(User user) {
    if (user.get("role_ids") == null) return Collections.emptyList();
    
    Set<String> role_ids = user.stringIDtoSet(user.getString("role_ids"));
    Set<Role> roles = new HashSet<Role>();
    
    for (String role_id : role_ids) {
      try {
        Role role = RoleDAO.getInstance(user.getDbName()).findOne(role_id);
        roles.add(role);
      } catch (UserManagementException e) {
        e.printStackTrace();
        throw new RuntimeException(e);
      }
    }
    
    List<Role> list = new ArrayList<Role>(roles);
    Collections.sort(list, new Comparator<Role>() {
      public int compare(Role o1, Role o2) {
        return o1.getString("name").compareTo(o2.getString("name"));
      }
    });
    return list;
  }
  
  @Override
  public User transform(DBObject obj) throws UserManagementException {
    return obj == null ? null : new User().from(obj);
  }

}
