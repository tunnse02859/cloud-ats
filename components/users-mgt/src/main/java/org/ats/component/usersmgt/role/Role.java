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

import org.ats.component.usersmgt.BaseObject;
import org.ats.component.usersmgt.UserManagementException;
import org.ats.component.usersmgt.group.Group;
import org.ats.component.usersmgt.group.GroupDAO;
import org.ats.component.usersmgt.user.User;
import org.ats.component.usersmgt.user.UserDAO;

import com.mongodb.DBObject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Jul 9, 2014
 */
public class Role extends BaseObject<Role> {

  private static final long serialVersionUID = 1L;

  public Role(String name, String groupId) {
    super();
    this.put("name", name);
    this.put("group_id", groupId);
  }
  
  public Role(DBObject obj) {
    this.from(obj);
  }
  
  public Group getGroup() {
    try {
      return GroupDAO.INSTANCE.findOne(this.getString("group_id"));
    } catch (UserManagementException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }
  
  public void addPermission(Permission permission) {
    try {
      PermissionDAO.INSTANCE.create(permission);
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
    if (this.get("permission_ids") == null) {
      return Collections.emptyList();
    }
    
    Set<String> permission_ids = this.stringIDtoSet(this.getString("permission_ids"));
    Set<Permission> permissions = new HashSet<Permission>();
    for (String permId : permission_ids) {
      try {
        Permission perm = PermissionDAO.INSTANCE.findOne(permId);
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
    if (this.get("user_ids") == null) {
      return Collections.emptyList();
    }
    
    Set<String> user_ids = this.stringIDtoSet(this.getString("user_ids"));
    Set<User> users = new HashSet<User>();
    for (String user_id : user_ids) {
      try {
        User user = UserDAO.INSTANCE.findOne(user_id);
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
  
  public String getName() {
    return this.getString("name");
  }

  @Override
  public Role from(DBObject obj) {
    this.putAll(obj);
    return this;
  }
}
