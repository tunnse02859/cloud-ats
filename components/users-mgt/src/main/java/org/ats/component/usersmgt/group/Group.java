/**
 * 
 */
package org.ats.component.usersmgt.group;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.ats.component.usersmgt.BaseObject;
import org.ats.component.usersmgt.UserManagementException;
import org.ats.component.usersmgt.feature.Feature;
import org.ats.component.usersmgt.feature.FeatureDAO;
import org.ats.component.usersmgt.role.Role;
import org.ats.component.usersmgt.role.RoleDAO;
import org.ats.component.usersmgt.user.User;
import org.ats.component.usersmgt.user.UserDAO;

import com.mongodb.BasicDBObject;
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
  
  public List<User> getUsers() {
    
    if (this.get("user_ids") == null) {
      return Collections.emptyList();
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
    
    List<User> list = new ArrayList<User>(users);
    Collections.sort(list, new Comparator<User>() {
      public int compare(User o1, User o2) {
        return o1.getEmail().compareTo(o2.getEmail());
      }
    });
    return list;
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
    
    if (this.get("role_ids") == null) {
      return Collections.emptyList();
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
    
    List<Role> list = new ArrayList<Role>(roles);
    Collections.sort(list, new Comparator<Role>() {
      public int compare(Role o1, Role o2) {
        return o1.getString("name").compareTo(o2.getString("name"));
      }
    });
    return list;
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
  
  public LinkedList<Group> buildParentTree() throws UserManagementException {
    LinkedList<Group> tree = new LinkedList<Group>();
    this.buildParentTree(tree, this);
    return tree;
  }
  
  private void buildParentTree(LinkedList<Group> tree, Group current) throws UserManagementException {
    Pattern p = Pattern.compile(current.getId());
    Collection<Group> col = GroupDAO.INSTANCE.find(new BasicDBObject("group_children_ids", p));
    if (!col.isEmpty() && col.size() == 1) {
      current = GroupDAO.INSTANCE.findOne(col.iterator().next().getId());
      tree.addFirst(current);
      this.buildParentTree(tree, current);
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
    
    if (this.get("feature_ids") == null) return Collections.emptyList();
    
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
    
    List<Feature> list = new ArrayList<Feature>(features);
    Collections.sort(list, new Comparator<Feature>() {
      public int compare(Feature o1, Feature o2) {
        return o1.getString("name").compareTo(o2.getString("name"));
      }
    });
    return list;
  }

  @Override
  public Group from(DBObject obj) {
    this.putAll(obj);
    return this;
  }
}
