/**
 * 
 */
package interceptor;

import org.ats.component.usersmgt.user.User;
import org.ats.component.usersmgt.user.UserDAO;

import play.libs.F.Promise;
import play.mvc.Action;
import play.mvc.Http.Context;
import play.mvc.SimpleResult;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Aug 15, 2014
 */
public class WithSystemInterceptor extends Action<WithSystem> {

  @Override
  public Promise<SimpleResult> call(Context ctx) throws Throwable {
    if (ctx.session().get("user_id") == null) return Promise.<SimpleResult>pure(redirect(controllers.routes.Application.signin()));
    else {
      User user = UserDAO.INSTANCE.findOne(ctx.session().get("user_id"));
      if (user == null || !user.isActive()) return Promise.<SimpleResult>pure(redirect(controllers.routes.Application.signout()));
      if (!user.getBoolean("system")) return Promise.<SimpleResult>pure(forbidden(
          views.html.forbidden.render()
      ));
    }
    
    return delegate.call(ctx);
  }

}
