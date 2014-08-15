/**
 * 
 */
package controllers;

import org.ats.component.usersmgt.UserManagementException;
import org.ats.component.usersmgt.user.User;
import org.ats.component.usersmgt.user.UserDAO;

import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.With;
import setup.AuthenticationInterceptor;
import setup.Authorization;
import setup.WithoutSystem;
import setup.WizardInterceptor;

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
}
