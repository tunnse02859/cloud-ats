/**
 * 
 */
package org.ats.component.usersmgt.user;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.ats.component.usersmgt.BaseObject;
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
public class User extends BaseObject<User> {

  private static final long serialVersionUID = 1L;
  
  public User(String name, String email) {
    super();
    this.put("name", name);
    this.put("email", email);
    this.put("active", true);
  }
  
  public User(DBObject obj) {
    this.from(obj);
  }

  public String getName() {
    return (String) this.get("name");
  }
  
  public String getEmail() {
    return (String) this.get("email");
  }

  public boolean isActive() {
    return this.getBoolean("active");
  }
  
  public void inActive() {
    this.put("active", false);
    this.put("group_ids", null);
    this.put("role_ids", null);
  }
  
  public void active() {
    this.put("active", true);
  }
  
  public void joinGroup(Group group) {
    this.joinGroup(group.getId());
  }
  
  public void joinGroup(String groupId) {
    if (this.get("group_ids") != null) {
      StringBuilder sb = new StringBuilder(this.getString("group_ids"));
      sb.append("::").append(groupId);
      this.put("group_ids", sb.toString());
    } else {
      this.put("group_ids", groupId);
    }
  }
  
  public boolean leaveGroup(Group group) {
    return this.leaveGroup(group.getId());
  }
  
  public boolean leaveGroup(String groupId) {
    
    if (this.get("group_ids") != null) {
      Set<String> groups = this.stringIDtoSet(this.getString("group_ids"));
      boolean result = groups.remove(groupId);
      this.put("group_ids", this.setToStringID(groups));
      return result;
    }
    
    return false;
  }
  
  public Set<Group> getGroups() {
    if (this.get("group_ids") == null) return Collections.emptySet();
    
    Set<String> group_ids = this.stringIDtoSet(this.getString("group_ids"));
    Set<Group> groups = new HashSet<Group>();
    for (String group_id : group_ids) {
      try {
        Group group = GroupDAO.INSTANCE.findOne(group_id);
        groups.add(group);
      } catch (UserManagementException e) {
        e.printStackTrace();
        throw new RuntimeException(e);
      }
    }
    return groups;
  }
  
  public void addRole(Role role) {
    this.addRole(role.getId());
  }
  
  public void addRole(String roleId) {
    if (this.get("role_ids") != null) {
      StringBuilder sb = new StringBuilder(this.getString("role_ids"));
      sb.append("::").append(roleId);
      this.put("role_ids", sb.toString());
    } else {
      this.put("role_ids", roleId);
    }
  }
  
  public boolean removeRole(Role role) {
    return this.removeRole(role.getId());
  }
  
  public boolean removeRole(String roleId) {
    if (this.get("role_ids") != null) {
      Set<String> roles = this.stringIDtoSet(this.getString("role_ids"));
      boolean result = roles.remove(roleId);
      this.put("role_ids", this.setToStringID(roles));
      return result;
    }
    return false;
  }
  
  public Set<Role> getRoles() {
    if (this.get("role_ids") == null) return Collections.emptySet();
    
    Set<String> role_ids = this.stringIDtoSet(this.getString("role_ids"));
    Set<Role> roles = new HashSet<Role>();
    
    for (String role_id : role_ids) {
      try {
        Role role = RoleDAO.INSTANCE.findOne(role_id);
        roles.add(role);
      } catch (UserManagementException e) {
        e.printStackTrace();
        throw new RuntimeException(e);
      }
    }
    
    return roles;
  }

  @Override
  public User from(DBObject obj) {
    if (obj == null) throw new NullPointerException("Object source is not allowed nullable");
    this.put("_id", obj.get("_id"));
    this.put("name", obj.get("name"));
    this.put("email", obj.get("email"));
    this.put("active", obj.get("active"));
    this.put("group_ids", obj.get("group_ids"));
    this.put("role_ids", obj.get("role_ids"));
    return this;
  }
}
