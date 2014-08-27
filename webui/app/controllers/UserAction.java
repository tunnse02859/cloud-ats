/**
 * 
 */
package controllers;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import interceptor.AuthenticationInterceptor;
import interceptor.Authorization;
import interceptor.WithSystem;
import interceptor.WithoutSystem;
import interceptor.WizardInterceptor;

import org.ats.component.usersmgt.Event;
import org.ats.component.usersmgt.EventExecutor;
import org.ats.component.usersmgt.UserManagementException;
import org.ats.component.usersmgt.group.Group;
import org.ats.component.usersmgt.group.GroupDAO;
import org.ats.component.usersmgt.role.Role;
import org.ats.component.usersmgt.role.RoleDAO;
import org.ats.component.usersmgt.user.User;
import org.ats.component.usersmgt.user.UserDAO;

import com.mongodb.BasicDBObject;

import play.api.templates.Html;
import play.data.DynamicForm;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.With;
import views.html.organization.*;
import views.html.organization.user.*;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Aug 14, 2014
 */
@With({WizardInterceptor.class, AuthenticationInterceptor.class})
@Authorization(feature = "Organization", operation = "Administration")
public class UserAction extends Controller {

  @WithoutSystem
  public static Result approve(String u) throws UserManagementException {
    User user_ = UserDAO.INSTANCE.findOne(u);
    user_.put("joined", true);
    UserDAO.INSTANCE.update(user_);
    return redirect(controllers.routes.Organization.index() + "?nav=user");
  }
  
  @WithSystem
  public static Result remove(String u) throws UserManagementException {
    User user_ = UserDAO.INSTANCE.findOne(u);
    UserDAO.INSTANCE.delete(user_);
    while(EventExecutor.INSTANCE.isInProgress()) {
    }
    return redirect(controllers.routes.Organization.index() + "?nav=user");
  }
  
  @WithSystem
  public static Result inactive(String u) throws UserManagementException {
    User user_ = UserDAO.INSTANCE.findOne(u);
    user_.inActive();
    UserDAO.INSTANCE.update(user_);
    return redirect(controllers.routes.Organization.index() + "?nav=user");
  }
  
  @WithSystem
  public static Result active(String u) throws UserManagementException {
    User user_ = UserDAO.INSTANCE.findOne(u);
    user_.active();
    UserDAO.INSTANCE.update(user_);
    return redirect(controllers.routes.Organization.index() + "?nav=user");
  }
  
  public static Result editRoleIndex(String u) throws UserManagementException {
    Group currentGroup = Organization.setCurrentGroup(session("group_id"));
    session().put("group_id", currentGroup.getId());

    User user_ = UserDAO.INSTANCE.findOne(u);
    
    if (currentGroup.getBoolean("system")) {
      Group group = Organization.getHighestGroupBelong(user_);
      session().put("group_id", group.getId());
      
      Html body = editrole.render(user_, group);
      return ok(index.render("user" , body, group.getId()));
    }
    
    Html body = editrole.render(user_, currentGroup);
    return ok(index.render("user" , body, currentGroup.getId()));
  }
  
  public static boolean inRole(Role role, User user_) {
    return role.getUsers().contains(user_) && user_.getRoles().contains(role);
  }
  
  public static Result doEditRole(String u) throws UserManagementException {
    User user_ = UserDAO.INSTANCE.findOne(u);
    Group currentGroup = GroupDAO.INSTANCE.findOne(session("group_id"));
    User currentUser = UserDAO.INSTANCE.findOne(session("user_id"));

    List<Role> currentGroupRole = currentGroup.getRoles();
    Set<String> userRole = user_.getString("role_ids") == null ? new HashSet<String>() : user_.stringIDtoSet(user_.getString("role_ids"));
    Set<String> actualRole = new HashSet<String>();
    
    if (request().getQueryString("role") != null) {
      Collections.addAll(actualRole, request().queryString().get("role"));
    }

    //Add non-existed role
    for (String r : actualRole) {
      if (!userRole.contains(r)) {
        Role role_ = RoleDAO.INSTANCE.findOne(r);
        role_.addUser(user_);
        user_.addRole(role_);
        RoleDAO.INSTANCE.update(role_);
        UserDAO.INSTANCE.update(user_);
      }
    }

    //Remove not existed role
    for (String r : userRole) {
      if (!actualRole.contains(r) ) {
        Role role_ = RoleDAO.INSTANCE.findOne(r);
        if (currentGroupRole.contains(role_)) {
          //should not remove administration role
          if (role_.getBoolean("system") && role_.getName().contains("Administration")) {
            LinkedList<Group> parents = currentGroup.buildParentTree();
            for (Group g : parents) {
              BasicDBObject query = new BasicDBObject("name", "Administration");
              query.append("system", true);
              query.append("group_id", g.getId());
              query.append("user_ids", Pattern.compile(user_.getId()));
              
              if (!RoleDAO.INSTANCE.find(query).isEmpty()) {
                role_.removeUser(user_);
                user_.removeRole(role_);
                
                RoleDAO.INSTANCE.update(role_);
                UserDAO.INSTANCE.update(user_);
                break;
              }
            }
          } else {
            role_.removeUser(user_);
            user_.removeRole(role_);
            
            RoleDAO.INSTANCE.update(role_);
            UserDAO.INSTANCE.update(user_);
          }
          
          if (currentUser.getBoolean("system") && !user_.getBoolean("system")) {
            role_.removeUser(user_);
            user_.removeRole(role_);
            
            RoleDAO.INSTANCE.update(role_);
            UserDAO.INSTANCE.update(user_);
          }
        } else if (currentGroup.getBoolean("system") && !user_.getBoolean("system")) {
          role_.removeUser(user_);
          user_.removeRole(role_);
          
          RoleDAO.INSTANCE.update(role_);
          UserDAO.INSTANCE.update(user_);
        }
      }
    }

    
    return redirect(controllers.routes.Organization.index() + "?nav=user&group=" + currentGroup.getId());
  }
  
  /**
   * Check should or not disable check administration role
   * @param role
   * @param user_
   * @return
   * @throws UserManagementException
   */
  public static boolean shouldDisable(Role role, User user_) throws UserManagementException {
    if (inRole(role, user_)) {
      Group currentGroup = GroupDAO.INSTANCE.findOne(session("group_id"));
      User currentUser = UserDAO.INSTANCE.findOne(session("user_id"));
      
      if (currentGroup.buildParentTree().isEmpty() && user_.equals(currentUser) && role.getBoolean("system")) {
        return true;
      }
      
      if (role.getBoolean("system")) {
        LinkedList<Group> parents = currentGroup.buildParentTree();
        for (Group g : parents) {
          BasicDBObject query = new BasicDBObject("name", "Administration");
          query.append("system", true);
          query.append("group_id", g.getId());
          query.append("user_ids", Pattern.compile(currentUser.getId()));
          if (!RoleDAO.INSTANCE.find(query).isEmpty()) return false;
        }
        
        if (currentUser.getBoolean("system")) return false;
        
        return true;
      }
      
    }
    return false;
  }
  
  public static Result leaveCurrentGroup(String u) throws UserManagementException {
    Group currentGroup = GroupDAO.INSTANCE.findOne(session("group_id"));
    User user_ = UserDAO.INSTANCE.findOne(u);
    user_.leaveGroup(currentGroup);
    Event event = new Event(user_) {
      @Override
      public String getType() {
        return "leave-group";
      }
    };
    event.broadcast();
    while(EventExecutor.INSTANCE.isInProgress()) {
    }
    user_ = UserDAO.INSTANCE.findOne(u);
    if (user_.getGroups().size() == 0) {
      user_.put("joined", false);
      UserDAO.INSTANCE.update(user_);
    }
    return redirect(controllers.routes.Organization.index() + "?nav=user");
  }
  
  public static boolean shouldLeaveGroup(User user_) throws UserManagementException {
    Group currentGroup = GroupDAO.INSTANCE.findOne(session("group_id"));
    
    if (currentGroup.getBoolean("system")) return false;
    
    User currentUser = UserDAO.INSTANCE.findOne(session("user_id"));
    if (!currentUser.equals(user_)) {
      //should not leave user have same administration role in current group (level1)
      if (currentGroup.getInt("level") == 1) {
        BasicDBObject query  = new BasicDBObject("name", "Administration");
        query.append("system", true);
        query.append("group_id", currentGroup.getId());
        Role adRole = RoleDAO.INSTANCE.find(query).iterator().next();
        if (adRole.getUsers().contains(user_) && adRole.getUsers().contains(currentUser)) return false;
      }
      return true;
    }
    
    if (!currentGroup.getBoolean("system") && currentUser.getBoolean("system")) return true;
    
    LinkedList<Group> parents = currentGroup.buildParentTree();
    for (Group g : parents) {
      BasicDBObject query = new BasicDBObject("name", "Administration");
      query.append("system", true);
      query.append("group_id", g.getId());
      query.append("user_ids", Pattern.compile(currentUser.getId()));
      if (!RoleDAO.INSTANCE.find(query).isEmpty()) return true;
    }
    return false;
  }
  
  @WithSystem
  public static Result createUserSystem() throws UserManagementException {
    Group systemGroup = GroupDAO.INSTANCE.find(new BasicDBObject("system", true)).iterator().next();
    Html body = systemuser.render();  
    return ok(index.render("user" , body, systemGroup.getId()));
  }
  
  @WithSystem
  public static Result doCreateUserSystem() throws UserManagementException {
    Group systemGroup = GroupDAO.INSTANCE.find(new BasicDBObject("system", true)).iterator().next();
    DynamicForm form = Form.form().bindFromRequest();
    String email = form.get("email");
    String password = form.get("password");
    User user = new User(email, email);
    user.put("password", password);
    user.put("joined", true);
    user.joinGroup(systemGroup);
    systemGroup.addUser(user);
    
    UserDAO.INSTANCE.create(user);
    GroupDAO.INSTANCE.update(systemGroup);
    
    return redirect(controllers.routes.Organization.index() + "?nav=user");
  }
}
