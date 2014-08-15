/**
 * 
 */
package controllers;

import interceptor.AuthenticationInterceptor;
import interceptor.Authorization;
import interceptor.WithSystem;
import interceptor.WithoutSystem;
import interceptor.WizardInterceptor;

import org.ats.component.usersmgt.EventExecutor;
import org.ats.component.usersmgt.UserManagementException;
import org.ats.component.usersmgt.group.Group;
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
    
    Html body = editrole.render(user_, currentGroup);
    return ok(index.render("user" , body, currentGroup.getId()));
  }
  
  public static boolean inRole(Role role, User user_) {
    return role.getUsers().contains(user_) && user_.getRoles().contains(role);
  }
  
  public static Result doEditRole(String u) throws UserManagementException {
    if (request().getQueryString("role") != null) {
      User user_ = UserDAO.INSTANCE.findOne(u);
      String[] roles_id = request().queryString().get("role");
      for (String role_id : roles_id) {
        Role role_ = RoleDAO.INSTANCE.findOne(role_id);
        role_.addUser(user_);
        user_.addRole(role_);
        RoleDAO.INSTANCE.update(role_);
      }
      UserDAO.INSTANCE.update(user_);
    }
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
      if (!"Administration".equals(role.getName())) return false;
      
      //Should prevent administration role if it has only one user
      if (role.getUsers().size() == 1) return true;
      
      //Should prevent administration role if user is edited who is current
      User currentUser = UserDAO.INSTANCE.findOne(session("user_id"));
      return user_.equals(currentUser);
    }
    return false;
  }
}
