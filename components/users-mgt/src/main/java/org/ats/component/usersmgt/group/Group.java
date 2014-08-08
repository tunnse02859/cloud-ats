/**
 * 
 */
package org.ats.component.usersmgt.group;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.ats.component.usersmgt.BaseObject;
import org.ats.component.usersmgt.UserManagementException;
import org.ats.component.usersmgt.feature.Feature;
import org.ats.component.usersmgt.feature.FeatureDAO;
import org.ats.component.usersmgt.role.Role;
import org.ats.component.usersmgt.role.RoleDAO;
import org.ats.component.usersmgt.user.User;
import org.ats.component.usersmgt.user.UserDAO;

import com.mongodb.DBObject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Jul 9, 2014
 */
public class Group extends BaseObject<Group> {

  private static final long serialVersionUID = 1L;
  
  public Group(String name) {
    super();
    this.put("name", name);
  }
  
  public Group(DBObject obj) {
    this.from(obj);
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
  
  public Set<User> getUsers() {
    
    if (this.get("user_ids") == null) {
      return Collections.emptySet();
    }
    
    String[] user_ids = this.getString("user_ids").split("::");
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
    return users;
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
  
  public Set<Role> getRoles() {
    
    if (this.get("role_ids") == null) {
      return Collections.emptySet();
    }
    
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
    if (this.get("group_children_ids") == null) {
      return Collections.emptySet();
    }
    
    Set<String> children_ids = this.stringIDtoSet(this.getString("group_children_ids"));
    Set<Group> children = new HashSet<Group>();
    for (String child_id : children_ids) {
      try {
        Group group = GroupDAO.INSTANCE.findOne(child_id);
        children.add(group);
      } catch (UserManagementException e) {
        e.printStackTrace();
        throw new RuntimeException(e);
      }
    }
    return children;
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
  
  public Set<Feature> getFeatures() {
    
    if (this.get("feature_ids") == null) return Collections.emptySet();
    
    Set<String> feature_ids = this.stringIDtoSet(this.getString("feature_ids"));
    Set<Feature> features = new HashSet<Feature>();
    for (String feature_id : feature_ids) {
      try {
        Feature feature = FeatureDAO.INSTANCE.findOne(feature_id);
        features.add(feature);
      } catch (UserManagementException e) {
        e.printStackTrace();
        throw new RuntimeException(e);
      }
    }
    return features;
  }

  @Override
  public Group from(DBObject obj) {
    this.putAll(obj);
    return this;
  }
}
