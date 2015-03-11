/**
 * 
 */
package org.ats.component.usersmgt.role;

import java.util.List;
import java.util.Set;

import org.ats.component.usersmgt.BaseObject;
import org.ats.component.usersmgt.UserManagementException;
import org.ats.component.usersmgt.group.Group;
import org.ats.component.usersmgt.group.GroupDAO;
import org.ats.component.usersmgt.user.User;

import com.mongodb.DBObject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Jul 9, 2014
 */
public class Role extends BaseObject<Role> {

  private static final long serialVersionUID = 1L;

  public Role() {}
  
  public Role(String dbName, String name, String groupId) {
    super(dbName);
    this.put("name", name);
    this.put("group_id", groupId);
  }
  
  public Group getGroup() {
    try {
      return GroupDAO.getInstance(getDbName()).findOne(this.getString("group_id"));
    } catch (UserManagementException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }
  
  public void addPermission(Permission permission) {
    try {
      PermissionDAO.getInstance(getDbName()).create(permission);
    } catch (UserManagementException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
    this.addPermission(permission.getId());
  }
  
  public void addPermission(String permId) {
    if (this.get("permission_ids") != null) {
      StringBuilder sb = new StringBuilder(this.getString("permission_ids"));
      sb.append("::").append(permId);
      this.put("permission_ids", sb.toString());
    } else {
      this.put("permission_ids", permId);
    }
  }
  
  public boolean removePermission(Permission permission) {
   return this.removePermission(permission.getId()); 
  }
  
  public boolean removePermission(String permId) {
    if (this.get("permission_ids") != null) {
      Set<String> perms = this.stringIDtoSet(this.getString("permission_ids"));
      boolean result = perms.remove(permId);
      String permission_ids = this.setToStringID(perms);
      this.put("permission_ids", permission_ids);
      return result;
    }
    return false;
  }
  
  public List<Permission> getPermissions() {
    return RoleDAO.getInstance(getDbName()).getPermissions(this);
  }
  
  public void addUser(User user) {
    this.addUser(user.getId());
  }
  
  public void addUser(String userId) {
    if (this.get("user_ids") != null) {
      StringBuilder sb = new StringBuilder(this.getString("user_ids"));
      sb.append("::").append(userId);
      this.put("user_ids", sb.toString());
    } else {
      this.put("user_ids", userId);
    }
  }
  
  public boolean removeUser(User user) {
    return this.removeUser(user.getId());
  }
  
  public boolean removeUser(String userId) {
    if (this.get("user_ids") != null) {
      Set<String> users = this.stringIDtoSet(this.getString("user_ids"));
      boolean result = users.remove(userId);
      String user_ids = this.setToStringID(users);
      this.put("user_ids", user_ids);
      return result;
    }
    return false;
  }
  
  public List<User> getUsers() {
    return RoleDAO.getInstance(getDbName()).getUsers(this);
  }
  
  public String getName() {
    return this.getString("name");
  }

  @Override
  public Role from(DBObject obj) {
    this.putAll(obj);
    return this;
  }
}
