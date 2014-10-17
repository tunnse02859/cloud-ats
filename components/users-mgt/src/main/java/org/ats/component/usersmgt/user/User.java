/**
 * 
 */
package org.ats.component.usersmgt.user;

import java.util.List;
import java.util.Set;

import org.ats.component.usersmgt.BaseObject;
import org.ats.component.usersmgt.group.Group;
import org.ats.component.usersmgt.role.Role;

import com.mongodb.DBObject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Jul 7, 2014
 */
public class User extends BaseObject<User> {

  private static final long serialVersionUID = 1L;

  public User() {}
  
  public User(String dbName, String name, String email) {
    super(dbName);
    this.put("name", name);
    this.put("email", email);
    this.put("active", true);
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
  
  public List<Group> getGroups() {
    return UserDAO.getInstance(getDbName()).getGroups(this);
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
  
  public List<Role> getRoles() {
    return UserDAO.getInstance(getDbName()).getRoles(this);
  }

  @Override
  public User from(DBObject obj) {
    this.putAll(obj);
    return this;
  }
}
