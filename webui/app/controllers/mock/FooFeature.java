/**
 * 
 */
package controllers.mock;

import org.ats.component.usersmgt.UserManagementException;
import org.ats.component.usersmgt.group.Group;
import org.ats.component.usersmgt.group.GroupDAO;

import interceptor.AuthenticationInterceptor;
import interceptor.Authorization;
import interceptor.WizardInterceptor;
import play.api.templates.Html;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.With;
import scala.collection.mutable.StringBuilder;
import views.html.mock.*;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Aug 22, 2014
 */
@With({WizardInterceptor.class, AuthenticationInterceptor.class})
@Authorization(feature = "Foo Feature", operation = "")
public class FooFeature extends Controller {

  public static Result index() {
    return ok(index.render(foobody.render(), "Foo Feature"));
  }
  
  @Authorization(feature = "Foo Feature", operation = "Foo Action 1")
  public static Result doAction1() throws UserManagementException {
    Group currentGroup = GroupDAO.INSTANCE.findOne(session("group_id"));
    StringBuilder sb = new StringBuilder();
    sb.append("<h4>You have performed <span class='badge badge-blue'>Foo Action 1</span> on Group <span class='badge badge-blue'>").append(currentGroup.get("name")).append("</span></h4>");
    return ok(index.render(new Html(sb), "Foo Feature"));
  }
  
  @Authorization(feature = "Foo Feature", operation = "Foo Action 2")
  public static Result doAction2() throws UserManagementException {
    Group currentGroup = GroupDAO.INSTANCE.findOne(session("group_id"));
    StringBuilder sb = new StringBuilder();
    sb.append("<h4>You have performed <span class='badge badge-blue'>Foo Action 2</span> on Group <span class='badge badge-blue'>").append(currentGroup.get("name")).append("</span></h4>");
    return ok(index.render(new Html(sb), "Foo Feature"));
  }
  
  @Authorization(feature = "Foo Feature", operation = "Foo Action 3")
  public static Result doAction3() throws UserManagementException {
    Group currentGroup = GroupDAO.INSTANCE.findOne(session("group_id"));
    StringBuilder sb = new StringBuilder();
    sb.append("<h4>You have performed <span class='badge badge-blue'>Foo Action 3</span> on Group <span class='badge badge-blue'>").append(currentGroup.get("name")).append("</span></h4>");
    return ok(index.render(new Html(sb), "Foo Feature"));
  }
}
