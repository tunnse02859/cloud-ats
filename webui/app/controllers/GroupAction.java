/**
 * 
 */
package controllers;

import java.util.Map;

import org.ats.component.usersmgt.UserManagementException;
import org.ats.component.usersmgt.feature.FeatureDAO;
import org.ats.component.usersmgt.group.Group;
import org.ats.component.usersmgt.group.GroupDAO;

import play.api.templates.Html;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.With;
import setup.AuthenticationInterceptor;
import setup.Authorization;
import setup.WithoutSystem;
import setup.WizardInterceptor;
import views.html.organization.*;
import views.html.organization.group.*;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Aug 12, 2014
 */
@With({WizardInterceptor.class, AuthenticationInterceptor.class})
@Authorization(feature = "Organization", operation = "Administration")

public class GroupAction extends Controller {

  @WithoutSystem
  public static Result newGroup() throws UserManagementException {
    Map<String, String[]> parameters = request().queryString();
    Group current = null;
    if (parameters.containsKey("group")) {
      current = GroupDAO.INSTANCE.findOne(parameters.get("group")[0]);
      session().put("group_id", current.getId());
    } else {
      current = Organization.getDefaultGroup().iterator().next();
      session().put("group_id", current.getId());
    }
    
    Html body = newgroup.render(current.getFeatures());
    return ok(index.render("group" , body, current.getId()));
  }
  
  @WithoutSystem
  public static Result newGroupBody() throws UserManagementException {
    Group current = GroupDAO.INSTANCE.findOne(session("group_id"));
    return ok(newgroup.render(current.getFeatures()));
  }
  
  @WithoutSystem
  public static Result doCreate() throws UserManagementException {
    String name = request().getQueryString("name");
    Group group = new Group(name);
    String[] features = request().queryString().get("feature");
    for (String f : features) {
      group.addFeature(FeatureDAO.INSTANCE.findOne(f));
    }
    Group current = GroupDAO.INSTANCE.findOne(session("group_id"));
    int level = current.getInt("level");
    group.put("level", level + 1);
    current.addGroupChild(group);
    
    GroupDAO.INSTANCE.create(group);
    GroupDAO.INSTANCE.update(current);
    
    session().put("group_id", group.getId());
    
    return redirect(controllers.routes.Organization.body());
  }
}
