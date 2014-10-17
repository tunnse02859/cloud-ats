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

import org.ats.component.usersmgt.ManagementDAO;
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
public class GroupDAO extends ManagementDAO<Group> {

  private GroupDAO(String dbName) {
    super(dbName, "group");
  }
  
  public static GroupDAO getInstance(String dbName) {
    return new GroupDAO(dbName);
  }
  
  public List<User> getUsers(Group group) {
    if (group.get("user_ids") == null) {
      return Collections.emptyList();
    }
    
    String[] user_ids = group.getString("user_ids").split("::");
    Set<User> users = new HashSet<User>();
    for (String user_id : user_ids) {
      try {
        User user = UserDAO.getInstance(group.getDbName()).findOne(user_id);
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
  
  public List<Role> getRoles(Group group) {
    if (group.get("role_ids") == null) {
      return Collections.emptyList();
    }
    
    Set<String> role_ids = group.stringIDtoSet(group.getString("role_ids"));
    Set<Role> roles = new HashSet<Role>();
    for (String role_id : role_ids) { 
      try {
        Role role = RoleDAO.getInstance(group.getDbName()).findOne(role_id);
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
  
  public LinkedList<Group> buildParentTree(Group group) throws UserManagementException {
    LinkedList<Group> tree = new LinkedList<Group>();
    this.buildParentTree(tree, group);
    return tree;
  }
  
  private void buildParentTree(LinkedList<Group> tree, Group current) throws UserManagementException {
    Pattern p = Pattern.compile(current.getId());
    Collection<Group> col = this.find(new BasicDBObject("group_children_ids", p));
    if (!col.isEmpty() && col.size() == 1) {
      current = this.findOne(col.iterator().next().getId());
      tree.addFirst(current);
      this.buildParentTree(tree, current);
    }
  }
  
  public Set<Group> getGroupChildren(Group group) {
    if (group.get("group_children_ids") == null) {
      return Collections.emptySet();
    }
    
    Set<String> children_ids = group.stringIDtoSet(group.getString("group_children_ids"));
    Set<Group> children = new HashSet<Group>();
    for (String child_id : children_ids) {
      try {
        Group child = this.findOne(child_id);
        children.add(child);
      } catch (UserManagementException e) {
        e.printStackTrace();
        throw new RuntimeException(e);
      }
    }
    return children;
  }
  
  public List<Feature> getFeatures(Group group) {
    if (group.get("feature_ids") == null) return Collections.emptyList();
    
    Set<String> feature_ids = group.stringIDtoSet(group.getString("feature_ids"));
    Set<Feature> features = new HashSet<Feature>();
    for (String feature_id : feature_ids) {
      try {
        Feature feature = FeatureDAO.getInstance(group.getDbName()).findOne(feature_id);
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
  public Group transform(DBObject obj) throws UserManagementException {
    return obj == null ? null : new Group().from(obj);
  }

}
