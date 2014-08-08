/**
 * 
 */
package controllers;

import play.mvc.Controller;
import play.mvc.Result;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Aug 5, 2014
 */
public class Dashboard extends Controller {

  public static Result body() {
    return ok(views.html.dashboard.body.render());
  }
}
