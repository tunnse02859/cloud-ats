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
import org.ats.component.usersmgt.user.User;
import org.ats.component.usersmgt.user.UserDAO;

import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.With;

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
    User user = UserDAO.INSTANCE.findOne(u);
    user.put("joined", true);
    UserDAO.INSTANCE.update(user);
    return redirect(controllers.routes.Organization.index() + "?nav=user");
  }
  
  @WithSystem
  public static Result remove(String u) throws UserManagementException {
    User user = UserDAO.INSTANCE.findOne(u);
    UserDAO.INSTANCE.delete(user);
    while(EventExecutor.INSTANCE.isInProgress()) {
    }
    return redirect(controllers.routes.Organization.index() + "?nav=user");
  }
  
  @WithSystem
  public static Result inactive(String u) throws UserManagementException {
    User user = UserDAO.INSTANCE.findOne(u);
    user.inActive();
    UserDAO.INSTANCE.update(user);
    return redirect(controllers.routes.Organization.index() + "?nav=user");
  }
  
  @WithSystem
  public static Result active(String u) throws UserManagementException {
    User user = UserDAO.INSTANCE.findOne(u);
    user.active();
    UserDAO.INSTANCE.update(user);
    return redirect(controllers.routes.Organization.index() + "?nav=user");
  }
}
