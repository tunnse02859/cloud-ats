/**
 * 
 */
package interceptor;

import org.ats.component.usersmgt.feature.Feature;
import org.ats.component.usersmgt.feature.FeatureDAO;
import org.ats.component.usersmgt.group.Group;
import org.ats.component.usersmgt.group.GroupDAO;
import org.ats.component.usersmgt.role.Permission;
import org.ats.component.usersmgt.role.Role;
import org.ats.component.usersmgt.user.User;
import org.ats.component.usersmgt.user.UserDAO;

import play.libs.F.Promise;
import play.mvc.Action;
import play.mvc.Http.Context;
import play.mvc.SimpleResult;

import com.mongodb.BasicDBObject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Aug 13, 2014
 */
public class AuthorizationInterceptor extends Action<Authorization> {

  @Override
  public Promise<SimpleResult> call(Context ctx) throws Throwable {
    User currentUser = UserDAO.INSTANCE.findOne(ctx.session().get("user_id"));
    if (currentUser == null) return Promise.<SimpleResult>pure(redirect("/"));
    
    if (!currentUser.getBoolean("joined")) {
      return Promise.<SimpleResult>pure(forbidden(
          views.html.forbidden.render()
      ));
    }
    
    Group currentGroup = GroupDAO.INSTANCE.findOne(ctx.session().get("group_id"));
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
          
          if (!feature.equals(per.getFeature())) continue;
          
          if (configuration.operation().isEmpty() && currentGroup.getRoles().contains(role)) {
              return delegate.call(ctx);
          }
          
          if (per.getOpertion().getName().equals(configuration.operation())) {
            if (feature.getBoolean("system") && feature.getName().equals("Organization"))
              return delegate.call(ctx);
            else if (currentGroup.getRoles().contains(role))
              return delegate.call(ctx);
          }
        }
      }
    }
    
    //
    return Promise.<SimpleResult>pure(forbidden(
        views.html.forbidden.render()
    ));
  }
}
