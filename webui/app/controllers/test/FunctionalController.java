/**
 * 
 */
package controllers.test;

import models.test.TestProjectModel.TestProjectType;
import interceptor.AuthenticationInterceptor;
import interceptor.Authorization;
import interceptor.WizardInterceptor;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.With;
import views.html.test.*;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Oct 27, 2014
 */
@With({WizardInterceptor.class, AuthenticationInterceptor.class})
@Authorization(feature = "Functional", operation = "")
public class FunctionalController extends Controller {

  public static Result index() {
    return ok(index.render(TestProjectType.functional.toString()));
  }
}
