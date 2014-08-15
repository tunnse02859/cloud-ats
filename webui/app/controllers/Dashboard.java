/**
 * 
 */
package controllers;

import interceptor.AuthenticationInterceptor;
import interceptor.WizardInterceptor;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.With;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Aug 5, 2014
 */

@With({WizardInterceptor.class, AuthenticationInterceptor.class})
public class Dashboard extends Controller {

  public static Result body() {
    return ok(views.html.dashboard.body.render());
  }
}
