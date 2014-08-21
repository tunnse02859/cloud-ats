/**
 * 
 */
package controllers;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

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

import play.api.templates.Html;
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
    Group currentGroup = Organization.setCurrentGroup(null);
    session().put("group_id", currentGroup.getId());

    User user_ = UserDAO.INSTANCE.findOne(u);
    
    if (currentGroup.getBoolean("system")) {
      Group group = Organization.getHighestGroupBelong(user_);
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

    Set<Role> currentGroupRole = currentGroup.getRoles();
    Set<String> currentUserRole = user_.getString("role_ids") == null ? new HashSet<String>() : user_.stringIDtoSet(user_.getString("role_ids"));
    Set<String> actualRole = new HashSet<String>();
    
    if (request().getQueryString("role") != null) {
      Collections.addAll(actualRole, request().queryString().get("role"));
    }


    //Add non-existed role
    for (String r : actualRole) {
      if (!currentUserRole.contains(r)) {
        Role role_ = RoleDAO.INSTANCE.findOne(r);
        role_.addUser(user_);
        user_.addRole(role_);
        RoleDAO.INSTANCE.update(role_);
      }
    }

    //Remove not existed role
    for (String r : currentUserRole) {
      if (!actualRole.contains(r) ) {
        Role role_ = RoleDAO.INSTANCE.findOne(r);
        if (currentGroupRole.contains(role_)) {
        if (role_.getBoolean("system") && currentGroup.getBoolean("system")) continue;
          role_.removeUser(user_);
          user_.removeRole(role_);
          RoleDAO.INSTANCE.update(role_);
        }
      }
    }

    UserDAO.INSTANCE.update(user_);
    
    return redirect(controllers.routes.Organization.index() + "?nav=user");
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
      if (!(role.getBoolean("system") && user_.getBoolean("system"))) return false;
      
      //Should prevent administration role if it has only one user
      if (role.getUsers().size() == 1) return true;
      
      //Should prevent administration role if user is edited who is current
      //User currentUser = UserDAO.INSTANCE.findOne(session("user_id"));
      //return user_.equals(currentUser);
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
}
