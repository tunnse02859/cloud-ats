/**
 * 
 */
package controllers;

import interceptor.AuthenticationInterceptor;
import interceptor.Authorization;
import interceptor.WithSystem;
import interceptor.WizardInterceptor;

import java.util.LinkedList;
import java.util.regex.Pattern;

import org.ats.component.usersmgt.UserManagementException;
import org.ats.component.usersmgt.feature.Feature;
import org.ats.component.usersmgt.feature.FeatureDAO;
import org.ats.component.usersmgt.group.Group;
import org.ats.component.usersmgt.group.GroupDAO;
import org.ats.component.usersmgt.role.Permission;
import org.ats.component.usersmgt.role.Role;
import org.ats.component.usersmgt.role.RoleDAO;
import org.ats.component.usersmgt.user.User;
import org.ats.component.usersmgt.user.UserDAO;

import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.With;
import views.html.leftmenu;

import com.mongodb.BasicDBObject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Aug 22, 2014
 */
@With({WizardInterceptor.class, AuthenticationInterceptor.class})
public class FeatureAction extends Controller {
  
  public static Result updateFeatureList(String active) throws UserManagementException {
    return ok(leftmenu.render(active));
  }

  public static boolean hasPermissionOnFeature(Feature feature) throws UserManagementException {
    User currentUser = UserDAO.INSTANCE.findOne(session("user_id"));
    Group currentGroup = GroupDAO.INSTANCE.findOne(session("group_id"));
    
    if (currentGroup == null) return false;
    
    if (!currentGroup.getFeatures().contains(feature)) return false;
    
    if (feature.getBoolean("disable")) return false;
    
    //check for Organization feature
    if (feature.getBoolean("system") && feature.getName().equals("Organization")) {
      if (currentUser.getBoolean("system")) return true;
      LinkedList<Group> parents = currentGroup.buildParentTree();
      for (Group parent : parents) {
        BasicDBObject query = new BasicDBObject("name", "Administration");
        query.append("system", true);
        query.append("group_id", parent.getId());
        query.append("user_ids", Pattern.compile(currentUser.getId()));
        if (!RoleDAO.INSTANCE.find(query).isEmpty()) return true;
      }
    }
    
    //check current user has right permission to perform feature on current group
    for (Role role : currentGroup.getRoles()) {
      if (role.getUsers().contains(currentUser)) {
        for (Permission per : role.getPermissions()) {
          if (per.getFeature().equals(feature)) return true;
        }
      }
    }
    
    return false;
  }

  @WithSystem
  @Authorization(feature = "Organization", operation = "Administration")
  public static Result disableFeature(String f) throws UserManagementException {
    Feature feature = FeatureDAO.INSTANCE.findOne(f);
    if (feature != null && !feature.getBoolean("system") && !feature.getBoolean("disable")) {
      feature.put("disable", true);
      FeatureDAO.INSTANCE.update(feature);
    }
    return redirect(controllers.routes.Organization.index() + "?nav=feature");
  }
  
  @WithSystem
  @Authorization(feature = "Organization", operation = "Administration")
  public static Result enableFeature(String f) throws UserManagementException {
    Feature feature = FeatureDAO.INSTANCE.findOne(f);
    if (feature != null && !feature.getBoolean("system") && feature.getBoolean("disable")) {
      feature.put("disable", false);
      FeatureDAO.INSTANCE.update(feature);
    }
    return redirect(controllers.routes.Organization.index() + "?nav=feature");
  }
}
