/**
 * 
 */
package interceptor;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.ats.component.usersmgt.group.Group;
import org.ats.component.usersmgt.user.User;
import org.ats.component.usersmgt.user.UserDAO;

import play.libs.F.Promise;
import play.mvc.Action.Simple;
import play.mvc.Http.Context;
import play.mvc.SimpleResult;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Aug 4, 2014
 */
public class AuthenticationInterceptor extends Simple {

  @Override
  public Promise<SimpleResult> call(Context ctx) throws Throwable {
    if (ctx.session().get("user_id") == null) return Promise.<SimpleResult>pure(redirect(controllers.routes.Application.signin()));
    else {
      User user = UserDAO.INSTANCE.findOne(ctx.session().get("user_id"));
      if (user == null || !user.isActive()) return Promise.<SimpleResult>pure(redirect(controllers.routes.Application.signout()));
    }
    //put group id to session if non-exist
    User currentUser = UserDAO.INSTANCE.findOne(ctx.session().get("user_id"));
    List<Group> groups = currentUser.getGroups();
    if (!(ctx.session().containsKey("group_id") && groups.isEmpty())) {
      Collections.sort(groups, new Comparator<Group>() {
        @Override
        public int compare(Group o1, Group o2) {
          return o1.getInt("level") - o2.getInt("level");
        }
      });
      ctx.session().put("group_id", groups.get(0).getId());
    }
    return delegate.call(ctx);
  }
}
