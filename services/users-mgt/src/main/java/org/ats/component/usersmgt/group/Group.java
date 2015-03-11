/**
 * 
 */
package org.ats.component.usersmgt.group;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ats.component.usersmgt.BaseObject;
import org.ats.component.usersmgt.feature.Feature;
import org.ats.component.usersmgt.role.Role;
import org.ats.component.usersmgt.user.User;

import com.mongodb.DBObject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Jul 9, 2014
 */
public class Group extends BaseObject<Group> {

  private static final long serialVersionUID = 1L;
  
  public Group() {}
  
  public Group(String dbName, String name) {
    super(dbName);
    this.put("name", name);
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
    return GroupDAO.getInstance(getDbName()).getUsers(this);
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
      String role_ids = this.setToStringID(roles);
      this.put("role_ids", role_ids);
      return result;
    }
    
    return false;
  }
  
  public List<Role> getRoles() {
    return GroupDAO.getInstance(getDbName()).getRoles(this);
  }
  
  public void addGroupChild(Group child) {
    this.addGroupChild(child.getId());
  }
  
  public void addGroupChild(String childId) {
    if (this.get("group_children_ids") != null) {
      StringBuilder sb = new StringBuilder(this.getString("group_children_ids"));
      sb.append("::").append(childId);
      this.put("group_children_ids", sb.toString());
    } else {
      this.put("group_children_ids", childId);
    }
  }
  
  public boolean removeGroupChild(Group child) {
    return this.removeGroupChild(child.getId());
  }
  
  public boolean removeGroupChild(String childId) {
    if (this.get("group_children_ids") != null) {
      Set<String> children = this.stringIDtoSet(this.getString("group_children_ids"));
      boolean result = children.remove(childId);
      String group_children_ids = this.setToStringID(children);
      this.put("group_children_ids", group_children_ids);
      return result;
    }
    return false;
  }
  
  public Set<Group> getGroupChildren() {
    return GroupDAO.getInstance(getDbName()).getGroupChildren(this);
  }
  
  public Set<Group> getAllChildren() {
    Set<Group> children = new HashSet<Group>();
    this.getAllChildren(children, this);
    return children;
  }
  
  private void getAllChildren(Set<Group> holder, Group parent) {
    Set<Group> children = parent.getGroupChildren();
    if (!children.isEmpty()) {
      holder.addAll(children);
      for (Group g : children) {
        this.getAllChildren(holder, g);
      }
    }
  }
  
  public void addFeature(Feature feature) {
    this.addFeature(feature.getId());
  }
  
  public void addFeature(String featureId) {
    if (this.get("feature_ids") != null) {
      StringBuilder sb = new StringBuilder(this.getString("feature_ids"));
      sb.append("::").append(featureId);
      this.put("feature_ids", sb.toString());
    } else {
      this.put("feature_ids", featureId);
    }
  }
  
  public boolean removeFeature(Feature feature) {
    return this.removeFeature(feature.getId());
  }
  
  public boolean removeFeature(String featureId) {
    if (this.get("feature_ids") != null) {
      Set<String> features = this.stringIDtoSet(this.getString("feature_ids"));
      boolean result = features.remove(featureId);
      String feature_ids = this.setToStringID(features);
      this.put("feature_ids", feature_ids);
      return result;
    }
    
    return false;
  }
  
  public List<Feature> getFeatures() {
    return GroupDAO.getInstance(getDbName()).getFeatures(this);
  }

  @Override
  public Group from(DBObject obj) {
    this.putAll(obj);
    return this;
  }
}
