/**
 * 
 */
package setup;

import play.libs.F.Promise;
import play.mvc.Action;
import play.mvc.Http.Context;
import play.mvc.SimpleResult;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Aug 13, 2014
 */
public class AuthorizationInterceptor extends Action<Authorization> {

  @Override
  public Promise<SimpleResult> call(Context ctx) throws Throwable {
    return delegate.call(ctx);
  }

}
