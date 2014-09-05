/**
 * 
 */
package controllers.vm;

import interceptor.AuthenticationInterceptor;
import interceptor.Authorization;
import interceptor.WithSystem;
import interceptor.WizardInterceptor;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.With;
import views.html.vm.body;
import views.html.vm.index;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Sep 4, 2014
 */
@With({WizardInterceptor.class, AuthenticationInterceptor.class})
@WithSystem
@Authorization(feature = "Virtual Machine", operation = "")
public class VirtualMachine extends Controller {

  public static Result index() {
    return ok(index.render(body.render(), "Virtual Machine"));
  }
}
