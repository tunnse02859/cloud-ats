/**
 * 
 */
package interceptor;

import org.ats.component.usersmgt.feature.Feature;
import org.ats.component.usersmgt.feature.FeatureDAO;
import org.ats.component.usersmgt.group.Group;
import org.ats.component.usersmgt.role.Permission;
import org.ats.component.usersmgt.role.Role;
import org.ats.component.usersmgt.user.User;
import org.ats.component.usersmgt.user.UserDAO;

import play.libs.F.Promise;
import play.mvc.Action;
import play.mvc.Http.Context;
import play.mvc.SimpleResult;

import com.mongodb.BasicDBObject;

import controllers.Organization;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Aug 13, 2014
 */
public class AuthorizationInterceptor extends Action<Authorization> {

  @Override
  public Promise<SimpleResult> call(Context ctx) throws Throwable {
    User currentUser = UserDAO.INSTANCE.findOne(ctx.session().get("user_id"));
    if (!currentUser.getBoolean("joined")) {
      return Promise.<SimpleResult>pure(forbidden(
          views.html.forbidden.render()
      ));
    }
    
    Group currentGroup = Organization.setCurrentGroup(ctx.session().get("group_id"));
    if (currentGroup == null)
      return Promise.<SimpleResult>pure(forbidden(
          views.html.forbidden.render()
      ));
      
    Feature feature = FeatureDAO.INSTANCE.find(new BasicDBObject("name", configuration.feature())).iterator().next();
    
    if (feature == null || feature.getBoolean("disable")) 
      return Promise.<SimpleResult>pure(forbidden(
          views.html.forbidden.render()
      ));
      
    if (currentGroup.getFeatures().contains(feature)) {
      for (Role role : currentUser.getRoles()) {
        for (Permission per : role.getPermissions()) {
          if (feature.equals(per.getFeature()) && per.getOpertion().getName().equals(configuration.operation()))
            return delegate.call(ctx);
        }
      }
    }
    
    //
    return Promise.<SimpleResult>pure(forbidden(
        views.html.forbidden.render()
    ));
  }
}
