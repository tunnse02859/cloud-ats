/**
 * 
 */
package controllers;

import interceptor.AuthenticationInterceptor;
import interceptor.Authorization;
import interceptor.WizardInterceptor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.ats.component.usersmgt.UserManagementException;
import org.ats.component.usersmgt.feature.Feature;
import org.ats.component.usersmgt.feature.FeatureDAO;
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
@With({WizardInterceptor.class, AuthenticationInterceptor.class})
@Authorization(feature = "Organization", operation = "Administration")
public class Organization extends Controller {
  
  /**
   * Set session current group by group id pass through query string.
   * Perform set if current user has right administration permission on this group 
   * Or has right administration permission on parent of this group
   * @param requestGroupId
   * @return
   * @throws UserManagementException
   */
  public static Group setCurrentGroup(String requestGroupId) throws UserManagementException {
   
    //Check right permission on group pass through query string
    if (requestGroupId != null) {
      User currentUser = UserDAO.INSTANCE.findOne(session("user_id"));
      if (currentUser.getBoolean("system")) {
        return GroupDAO.INSTANCE.findOne(requestGroupId);
      }
      
      //Check have right administration permission on exactly group
      BasicDBObject query = new BasicDBObject("group_id", requestGroupId);
      query.put("name", "Administration");
      query.put("user_ids", Pattern.compile(currentUser.getId()));
      Collection<Role> administration = RoleDAO.INSTANCE.find(query);
      
      if (!administration.isEmpty()) {
        return GroupDAO.INSTANCE.findOne(requestGroupId);
      }
      
      //Or on parent group
      Group requestGroup = GroupDAO.INSTANCE.findOne(requestGroupId);
      for (Group g : getAministrationGroup(currentUser)) {
        if (g.getAllChildren().contains(requestGroup)) return requestGroup;
      }
      
    }
    
    if (session("group_id") == null) {
      return getAdministrationGroup().iterator().next();
    }
    
    Group current = GroupDAO.INSTANCE.findOne(session("group_id"));
    if (current == null) {
      session().remove("group_id");
      current = setCurrentGroup(null);
    }
    return current;
  }

  /**
   * Present a full of content
   * @return
   * @throws UserManagementException
   */
  public static Result index() throws UserManagementException {
    Group current = setCurrentGroup(request().getQueryString("group"));
    session().put("group_id", current.getId());
    
    Html body = bodyComposite(request().queryString());
    return ok(index.render(request().getQueryString("nav") == null ? "group" : request().getQueryString("nav"), body, current.getId()));
  }
  
  /**
   * Present a part of content to call by ajax.
   * @return
   * @throws UserManagementException
   */
  public static Result indexAjax() throws UserManagementException {
    Group current = setCurrentGroup(request().getQueryString("group"));
    session().put("group_id", current.getId());
    
    Html body = bodyComposite(request().queryString());
    return ok(indexajax.render("group", body, current.getId()));
  }
  
  public static Result body() throws UserManagementException {
    Group current = setCurrentGroup(request().getQueryString("group"));
    session().put("group_id", current.getId());
    
    ObjectNode json = Json.newObject();

    Html leftMenu = leftmenu.render(request().getQueryString("nav") == null ? "group" : request().getQueryString("nav"), current.getId());
    
    Html body = bodyComposite(request().queryString());
    
    Html breadcrumb = groupBreadcrumb(request().getQueryString("nav") == null ? "group" : request().getQueryString("nav"), current.getId());
    
    json.put("breadcrumb", breadcrumb.toString());
    json.put("leftmenu", leftMenu.toString());
    json.put("body", body.toString());
    json.put("group", current.getId());
    
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
        sb.append(user.render(u, currentUser.getBoolean("system"), currentGroup.getRoles()));
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
        sb.append(feature.render(f, currentUser.getBoolean("system")));
      }
      return features.render(new Html(sb));
    }
    return null;
  }
  
  /**
   * Return user's group with highest level
   * @param u
   * @return
   * @throws UserManagementException
   */
  public static Group getHighestGroupBelong(User u) throws UserManagementException {
    List<Group> list = new ArrayList<Group>(u.getGroups());
    Collections.sort(list, new Comparator<Group>() {
      @Override
      public int compare(Group o1, Group o2) {
        int l1 = o1.getInt("level");
        int l2 = o2.getInt("level");
        return l2 - l1;
      }
    });
    return list.isEmpty() ? null : list.get(0);
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
      all = new ArrayList<Group>(current.getAllChildren());
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
   * Build a list of group have administration permission of current user
   * @return The list of groups order by lowest level.
   * @throws UserManagementException
   */
  static Collection<Group> getAdministrationGroup() throws UserManagementException {
    User user = UserDAO.INSTANCE.findOne(session("user_id"));
    
    if (user.getBoolean("system")) {
      return GroupDAO.INSTANCE.find(new BasicDBObject("system", true));
    }
    
    return getAministrationGroup(user);
  }
  
  /**
   * List all groups have administration permission of specified user
   * @param user
   * @return
   * @throws UserManagementException
   */
  static Collection<Group> getAministrationGroup(User user) throws UserManagementException {
    List<Group> list = new ArrayList<Group>();
    
    for (Group g : user.getGroups()) {
      BasicDBObject query = new BasicDBObject("name", "Administration");
      query.put("group_id", g.getId());
      query.put("user_ids", Pattern.compile(user.getId()));
      if (!RoleDAO.INSTANCE.find(query).isEmpty()) {
        list.add(g);
      }
    }
    
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
  
  /**
   * Build the group path which depends on current group. The group path presents the relationship of current group.
   * @param nav
   * @return
   * @throws UserManagementException
   */
  public static Html groupBreadcrumb(String nav, String group_id) throws UserManagementException {
    User currentUser = UserDAO.INSTANCE.findOne(session("user_id"));
    Group currentGroup = setCurrentGroup(group_id);
    session().put("group_id", currentGroup.getId());
    
    StringBuilder sb = new StringBuilder();
    if (currentGroup.getBoolean("system")) {
      sb.append("<li class='active'>").append(currentGroup.get("name")).append("</li>");
      return new Html(sb);
    } else if (currentUser.getBoolean("system")) {
      Group sys = GroupDAO.INSTANCE.find(new BasicDBObject("system", true)).iterator().next();
      String href = controllers.routes.Organization.index().toString() + "?nav=" + nav + "&group=" + sys.getId();
      String ajax = controllers.routes.Organization.body().toString() + "?nav=" + nav + "&group=" + sys.getId();
      sb.append("<li>").append("<a href='").append(href).append("' ajax-url='").append(ajax).append("'>").append(sys.get("name")).append("</a> <span class='divider'>/</span></li>");
    }
    
    LinkedList<Group> parents = currentGroup.buildParentTree();
    
    if (!currentUser.getBoolean("system")) {
      //Prevent current user lookup parent group with no right permission
      Collection<Group> adGroup = getAministrationGroup(currentUser);
      Set<Group> allChildren = new HashSet<Group>();
      for (Group g : adGroup) {
        allChildren.addAll(g.getAllChildren());
      }
      
      for (Group p : parents) {
        if (adGroup.contains(p) || allChildren.contains(p)) {
          String href = controllers.routes.Organization.index().toString() + "?nav=" + nav + "&group=" + p.getId();
          String ajax = controllers.routes.Organization.body().toString() + "?nav=" + nav + "&group=" + p.getId();
          sb.append("<li>").append("<a href='").append(href).append("' ajax-url='").append(ajax).append("'>").append(p.get("name"));
        } else {
          sb.append("<li class='active'>").append(p.get("name")).append("</li>");
        }
        sb.append("</a> <span class='divider'> / </span></li>");
      }
    } else {
      for (Group p : parents) {
        String href = controllers.routes.Organization.index().toString() + "?nav=" + nav + "&group=" + p.getId();
        String ajax = controllers.routes.Organization.body().toString() + "?nav=" + nav + "&group=" + p.getId();
        sb.append("<li>").append("<a href='").append(href).append("' ajax-url='").append(ajax).append("'>").append(p.get("name"));
        sb.append("</a> <span class='divider'> / </span></li>");
      }
    }
    
    sb.append("<li class='active'>").append(currentGroup.get("name")).append("</li>");
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
      return current.getAllChildren().size();
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
