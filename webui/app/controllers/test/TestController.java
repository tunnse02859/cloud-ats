/**
 * 
 */
package controllers.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.ats.component.usersmgt.UserManagementException;
import org.ats.component.usersmgt.feature.Feature;
import org.ats.component.usersmgt.feature.FeatureDAO;
import org.ats.component.usersmgt.feature.Operation;
import org.ats.component.usersmgt.group.Group;
import org.ats.component.usersmgt.group.GroupDAO;
import org.ats.component.usersmgt.role.Permission;
import org.ats.component.usersmgt.role.Role;
import org.ats.component.usersmgt.user.User;
import org.ats.component.usersmgt.user.UserDAO;

import play.api.templates.Html;
import play.mvc.Controller;
import scala.collection.mutable.StringBuilder;

import com.mongodb.BasicDBObject;

import controllers.Application;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Oct 21, 2014
 */
public class TestController extends Controller {

  public static Html groupMenuList() throws UserManagementException {
    StringBuilder sb = new StringBuilder();
    User currentUser = UserDAO.getInstance(Application.dbName).findOne(session("user_id"));
    
    Feature perfFeature = FeatureDAO.getInstance(Application.dbName).find(new BasicDBObject("name", "Performance")).iterator().next();
    Operation perfAdOperation = null;
    Operation perfTestOperation = null;
    for (Operation op : perfFeature.getOperations()) {
      if (op.getName().equals("Administration")) {
        perfAdOperation = op;
      } else if (op.getName().equals("Test")) {
        perfTestOperation = op;
      }
    }
    
    List<Role> perfAdRoles = new ArrayList<Role>();
    List<Role> perfTestRoles = new ArrayList<Role>();
    
    for (Role role : currentUser.getRoles()) {
      for (Permission per : role.getPermissions()) {
        Feature f = per.getFeature();
        Operation op = per.getOpertion();
        if (f.equals(perfFeature)) {
          if (op.equals(perfAdOperation))
            perfAdRoles.add(role);
          else if (op.equals(perfTestOperation)) {
            perfTestRoles.add(role);
          }
        }
      }
    }
    
    List<Group> adminGroups = getGroupsHasPermission(currentUser, perfAdRoles);
    Collections.sort(adminGroups, new Comparator<Group>() {
      @Override
      public int compare(Group o1, Group o2) {
        return o1.getInt("level") - o2.getInt("level");
      }
    });
    
    for (Group group : adminGroups) {
      buildGroupPath(sb, group, true);
    }
    
    List<Group> testGroups = getGroupsHasPermission(currentUser, perfTestRoles);
    Collections.sort(testGroups, new Comparator<Group>() {
      @Override
      public int compare(Group o1, Group o2) {
        return o1.getInt("level") - o2.getInt("level");
      }
    });
    
    for (Group group : testGroups) {
      buildGroupPath(sb, group, false);
    }
    
    return new Html(sb);
  }
  
  private static void buildGroupPath(StringBuilder sb, Group group, boolean admin) throws UserManagementException {
    sb.append("<li><a href='javascript:void(0);'>");
    LinkedList<Group> parents = GroupDAO.getInstance(Application.dbName).buildParentTree(group);
    for (Group p : parents) {
      sb.append(" / ").append(p.getString("name"));
    }
    sb.append(" / ").append(group.getString("name"));
    sb.append("</a></li>");
    
    if (admin) {
      for (Group g : group.getGroupChildren()) {
        buildGroupPath(sb, g, true);
      }
    }
  }
  
  private static List<Group> getGroupsHasPermission(User currentUser, List<Role> adRoles) {
    List<Group> groups = currentUser.getGroups();
    
    List<Group> holder = new ArrayList<Group>();
    for (Role role : adRoles) {
      Group group = role.getGroup();
      if (groups.contains(group)) holder.add(group);
    }
    
    return holder;
  }
}
