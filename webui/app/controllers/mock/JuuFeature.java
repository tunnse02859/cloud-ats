/**
 * 
 */
package controllers.mock;

import interceptor.Authorization;

import org.ats.component.usersmgt.UserManagementException;
import org.ats.component.usersmgt.group.Group;
import org.ats.component.usersmgt.group.GroupDAO;

import play.api.templates.Html;
import play.mvc.Controller;
import play.mvc.Result;
import scala.collection.mutable.StringBuilder;
import views.html.mock.*;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Aug 25, 2014
 */
public class JuuFeature extends Controller {

  public static Result index() {
    return ok(index.render(juubody.render(), "Juu Feature"));
  }
  
  @Authorization(feature = "Juu Feature", operation = "Juu Action 1")
  public static Result doAction1() throws UserManagementException {
    Group currentGroup = GroupDAO.INSTANCE.findOne(session("group_id"));
    StringBuilder sb = new StringBuilder();
    sb.append("<h4>You have performed <span class='badge badge-blue'>Juu Action 1</span> on Group <span class='badge badge-blue'>").append(currentGroup.get("name")).append("</span></h4>");
    return ok(index.render(new Html(sb), "Juu Feature"));
  }
  
  @Authorization(feature = "Juu Feature", operation = "Juu Action 2")
  public static Result doAction2() throws UserManagementException {
    Group currentGroup = GroupDAO.INSTANCE.findOne(session("group_id"));
    StringBuilder sb = new StringBuilder();
    sb.append("<h4>You have performed <span class='badge badge-blue'>Juu Action 2</span> on Group <span class='badge badge-blue'>").append(currentGroup.get("name")).append("</span></h4>");
    return ok(index.render(new Html(sb), "Juu Feature"));
  }
  
  @Authorization(feature = "Juu Feature", operation = "Juu Action 3")
  public static Result doAction3() throws UserManagementException {
    Group currentGroup = GroupDAO.INSTANCE.findOne(session("group_id"));
    StringBuilder sb = new StringBuilder();
    sb.append("<h4>You have performed <span class='badge badge-blue'>Juu Action 3</span> on Group <span class='badge badge-blue'>").append(currentGroup.get("name")).append("</span></h4>");
    return ok(index.render(new Html(sb), "Juu Feature"));
  }
}
