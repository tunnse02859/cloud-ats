/**
 * 
 */
package controllers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.ats.component.usersmgt.UserManagementException;
import org.ats.component.usersmgt.feature.Feature;
import org.ats.component.usersmgt.feature.FeatureDAO;
import org.ats.component.usersmgt.feature.Operation;
import org.ats.component.usersmgt.group.Group;
import org.ats.component.usersmgt.group.GroupDAO;
import org.ats.component.usersmgt.role.Permission;
import org.ats.component.usersmgt.role.Role;
import org.ats.component.usersmgt.role.RoleDAO;
import org.ats.component.usersmgt.user.User;
import org.ats.component.usersmgt.user.UserDAO;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.BasicDBObject;

import scala.collection.mutable.StringBuilder;
import play.api.templates.Html;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.With;
import setup.AuthenticatedInterceptor;
import setup.WizardInterceptor;
import views.html.organization.*;
import views.html.organization.group.*;
import views.html.organization.user.*;
import views.html.organization.role.*;
import views.html.organization.feature.*;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Aug 4, 2014
 */
@With({WizardInterceptor.class, AuthenticatedInterceptor.class})
public class Organization extends Controller {

  /**
   * Present a full of content
   * @return
   * @throws UserManagementException
   */
  public static Result index() throws UserManagementException {
    Map<String, String[]> parameters = request().queryString();
    Group current = null;
    if (parameters.containsKey("group")) {
      current = GroupDAO.INSTANCE.findOne(parameters.get("group")[0]);
      session().put("group_id", current.getId());
    } else {
      current = getDefaultGroup().iterator().next();
      session().put("group_id", current.getId());
    }
    
    Html body = bodyComposite(request().queryString());
    return ok(index.render(request().getQueryString("nav") == null ? "group" : request().getQueryString("nav"), body, current.getId()));
  }
  
  /**
   * Present a part of content to call by ajax.
   * @return
   * @throws UserManagementException
   */
  public static Result indexAjax() throws UserManagementException {
    //Default: current group is highest level group
    Group current = getDefaultGroup().iterator().next();
    session().put("group_id", current.getId());
    
    Html body = bodyComposite(request().queryString());
    return ok(indexajax.render("group", body, current.getId()));
  }
  
  public static Result body() throws UserManagementException {
    Map<String, String[]> parameters = request().queryString();
    Group current = null;
    if (parameters.containsKey("group")) {
       current = GroupDAO.INSTANCE.findOne(parameters.get("group")[0]);
      session().put("group_id", current.getId());
    } else {
      current = getDefaultGroup().iterator().next();
      session().put("group_id", current.getId());
    }
    
    ObjectNode json = Json.newObject();

    Html leftMenu = leftmenu.render(request().getQueryString("nav") == null ? "group" : request().getQueryString("nav"), current.getId());
    
    Html body = bodyComposite(request().queryString());
    
    Html breadcrumb = groupBreadcrumb(request().getQueryString("nav") == null ? "group" : request().getQueryString("nav"));
    
    json.put("breadcrumb", breadcrumb.toString());
    json.put("leftmenu", leftMenu.toString());
    json.put("body", body.toString());
    
    return ok(json);
  }
  
  /**
   * Build a part of content body base on current state.
   * @param parameters
   * @return
   * @throws UserManagementException
   */
  private static Html bodyComposite(Map<String, String[]> parameters) throws UserManagementException {
    
    String nav = parameters.containsKey("nav") ? parameters.get("nav")[0] : "group";
    User currentUser = UserDAO.INSTANCE.findOne(session("user_id"));
    Group currentGroup = GroupDAO.INSTANCE.findOne(session("group_id"));
    
    if ("group".equals(nav)) {
      
      StringBuilder sb = new StringBuilder();
      List<Group> all = listGroupVisible();
      for (Group g : all) {
        sb.append(group.render(g));
      }
      
      return groups.render(new Html(sb), currentUser.getBoolean("system"));
    
    } else if ("user".equals(nav)) {
      
      StringBuilder sb = new StringBuilder();
      List<User> all = listUserVisible();
      for (User u : all) {
        sb.append(user.render(u, currentUser.getBoolean("system")));
      }
      
      return users.render(new Html(sb), currentUser.getBoolean("system"));
          
    } else if ("role".equals(nav)) {
      StringBuilder sb = new StringBuilder();
      for (Role r : currentGroup.getRoles()) {
        sb.append(role.render(r, new ArrayList<Permission>(r.getPermissions())));
      }
      return roles.render(new Html(sb));
    } else if ("feature".equals(nav)) {
      StringBuilder sb = new StringBuilder();
      for (Feature f : currentGroup.getFeatures()) {
        sb.append(feature.render(f, new ArrayList<Operation>(f.getOperations())));
      }
      return features.render(new Html(sb));
    }
    return null;
  }
  
  /**
   * List all of groups those are visible for current group;
   * @return
   * @throws UserManagementException
   */
  private static List<Group> listGroupVisible() throws UserManagementException {
    Group current = GroupDAO.INSTANCE.findOne(session("group_id"));
    
    List<Group> all = null;
    if (current.getBoolean("system")) {
      all = new ArrayList<Group>(GroupDAO.INSTANCE.find(new BasicDBObject()));
    }  else {
      all = new ArrayList<Group>(current.getGroupChildren());
    }
    
    Collections.sort(all, new Comparator<Group>() {
      @Override
      public int compare(Group o1, Group o2) {
        int l1 = o1.getInt("level");
        int l2 = o2.getInt("level");
        return l1 - l2;
      }
    });
    return all;
  }
  
  private static List<User> listUserVisible() throws UserManagementException {
    Group current = GroupDAO.INSTANCE.findOne(session("group_id"));
    List<User> all = null;
    if (current.getBoolean("system")) {
      all = new ArrayList<User>(UserDAO.INSTANCE.find(new BasicDBObject()));
    } else {
      Pattern p = Pattern.compile(current.getId());
      Collection<User> users = UserDAO.INSTANCE.find(new BasicDBObject("group_ids", p));
      all = new ArrayList<User>(users);
    }
    return all;
  }
  
  /**
   * 
   * @return The set of groups with highest level.
   * @throws UserManagementException
   */
  private static Collection<Group> getDefaultGroup() throws UserManagementException {
    User user = UserDAO.INSTANCE.findOne(session("user_id"));
    if (user.getBoolean("system")) {
      return GroupDAO.INSTANCE.find(new BasicDBObject("system", true));
    } else {
      List<Group> list = new ArrayList<Group>(user.getGroups());
      Collections.sort(list, new Comparator<Group>() {
        @Override
        public int compare(Group o1, Group o2) {
          int l1 = o1.getInt("level");
          int l2 = o2.getInt("level");
          return l1 - l2;
        }
      });
      
      return list;
    }
  }
  
  /**
   * Build the group path which depends on current group. The group path presents the relationship of current group.
   * @param nav
   * @return
   * @throws UserManagementException
   */
  public static Html groupBreadcrumb(String nav) throws UserManagementException {
    User user = UserDAO.INSTANCE.findOne(session("user_id"));
    Group current = GroupDAO.INSTANCE.findOne(session("group_id"));
    
    StringBuilder sb = new StringBuilder();
    if (current.getBoolean("system")) {
      sb.append("<li class='active'>").append(current.get("name")).append("</li>");
      return new Html(sb);
    } else if (user.getBoolean("system")) {
      Group sys = GroupDAO.INSTANCE.find(new BasicDBObject("system", true)).iterator().next();
      String href = controllers.routes.Organization.index().toString() + "?nav=" + nav + "&group=" + sys.getId();
      String ajax = controllers.routes.Organization.body().toString() + "?nav=" + nav + "&group=" + sys.getId();
      sb.append("<li>").append("<a href='").append(href).append("' ajax-url='").append(ajax).append("'>").append(sys.get("name")).append("</a> <span class='divider'>/</span></li>");
    }
    sb.append("<li class='active'>").append(current.get("name")).append("</li>");
    return new Html(sb);
  }

  /**
   * Count entities of current group. Use for left menu.
   * @param nav The alias of entity
   * @return
   * @throws UserManagementException
   */
  public static long count(String nav) throws UserManagementException {
    Group current = GroupDAO.INSTANCE.findOne(session("group_id"));
    if ("role".equals(nav))
      return current.getRoles().size();
    else if ("feature".equals(nav))
      return current.getFeatures().size();
    else if ("group".equals(nav)) {
      if (current.getBoolean("system")) {
        return GroupDAO.INSTANCE.count();
      }
      return current.getGroupChildren().size();
    } 
    else if ("user".equals(nav)) {
      if (current.getBoolean("system")) {
        return UserDAO.INSTANCE.count();
      }
      return current.getUsers().size();
    }
    return -1;
  }
  
  /**
   * Filter group and build presentation of content. The content should update by ajax.
   * @return
   * @throws UserManagementException
   */
  public static Result filter(String nav) throws UserManagementException {
    
    Map<String, String[]> parameters = request().queryString();
    
    Group current = GroupDAO.INSTANCE.findOne(session("group_id"));
    
    if ("group".equals(nav)) {

      Set<Group> filter = new HashSet<Group>();
      
      if (current.getBoolean("system")) {
        
        BasicDBObject query = new BasicDBObject();
        
        if (parameters.containsKey("name")) {
          String name = parameters.get("name")[0];
          query.put("$text", new BasicDBObject("$search", name));
        }
        if (parameters.containsKey("level")) {
          int level = Integer.parseInt(parameters.get("level")[0]);
          query.put("level", level);
        }
        
        filter.addAll(GroupDAO.INSTANCE.find(query));
        
        ObjectNode json = Json.newObject();
        ArrayNode array = json.putArray("groups");
        for (Group g : filter) {
          array.add(g.getId());
        }
        
        return ok(json);
      } else {
        List<Group> all = listGroupVisible();
        BasicDBObject query = new BasicDBObject();
        
        if (parameters.containsKey("name")) {
          String name = parameters.get("name")[0];
          query.put("$text", new BasicDBObject("$search", name));
        }
        if (parameters.containsKey("level")) {
          int level = Integer.parseInt(parameters.get("level")[0]);
          query.put("level", level);
        }
        
        ObjectNode json = Json.newObject();
        ArrayNode array = json.putArray("groups");
        
        if (query.isEmpty()) {
          for (Group g : all) {
            array.add(g.getId());
          }
          return ok(json);
        }
        
        filter.addAll(GroupDAO.INSTANCE.find(query));
        for (Group g : all) {
          if (filter.contains(g)) array.add(g.getId());
        }
        
        return ok(json);
      }
    } else if ("user".equals(nav)) {
      
      Set<User> filter = new HashSet<User>();
      BasicDBObject query = new BasicDBObject();
      if (parameters.containsKey("email")) {
        String name = parameters.get("email")[0];
        query.put("$text", new BasicDBObject("$search", name));
      }
      
      filter.addAll(UserDAO.INSTANCE.find(query));
      
      ObjectNode json = Json.newObject();
      ArrayNode array = json.putArray("users");
      
      if (current.getBoolean("system")) {
        for (User u : filter) {
          array.add(u.getId());
        }
      } else {
        for (User u : current.getUsers()) {
          if (filter.contains(u)) array.add(u.getId());
        }
      }
      return ok(json);
    } else if ("role".equals(nav)) {
      Set<Role> filter = new HashSet<Role>();
      ObjectNode json = Json.newObject();
      ArrayNode array = json.putArray("roles");

      BasicDBObject query = new BasicDBObject();
      if (parameters.containsKey("name")) {
        String name = parameters.get("name")[0];
        query.put("$text", new BasicDBObject("$search", name));
      } else {
        for (Role r : current.getRoles()) {
          array.add(r.getId());
        }
      }
      filter.addAll(RoleDAO.INSTANCE.find(query));
      for (Role r : current.getRoles()) {
        if (filter.contains(r)) array.add(r.getId());
      }
      return ok(json);
    } else if ("feature".equals(nav)) {
      Set<Feature> filter = new HashSet<Feature>();
      ObjectNode json = Json.newObject();
      ArrayNode array = json.putArray("features");
      
      BasicDBObject query = new BasicDBObject();
      if (parameters.containsKey("name")) {
        String name = parameters.get("name")[0];
        query.put("$text", new BasicDBObject("$search", name));
      } else {
        for (Feature f : current.getFeatures()) {
          array.add(f.getId());
        }
      }
      filter.addAll(FeatureDAO.INSTANCE.find(query));
      for (Feature f : current.getFeatures()) {
        if (filter.contains(f)) array.add(f.getId());
      }
      return ok(json);
      
    }
    return status(404);
  }
}
