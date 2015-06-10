/**
 * 
 */
package actions;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import play.libs.F.Promise;
import play.mvc.Action;
import play.mvc.Http.Context;
import play.mvc.Http.Response;
import play.mvc.Result;
import play.mvc.With;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * May 11, 2015
 */
public class CorsComposition {
  
  @With(CorsAction.class)
  @Target({ ElementType.TYPE, ElementType.METHOD })
  @Retention(RetentionPolicy.RUNTIME)
  public @interface Cors {
    String value() default "*";
  }

  public static class CorsAction extends Action<Cors> {

    @Override
    public Promise<Result> call(Context ctx) throws Throwable {
      Response response = ctx.response();
      response.setHeader("Access-Control-Allow-Origin", "*");
 
      if (ctx.request().method().equals("OPTIONS")) {
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, PUT, DELETE");
        response.setHeader("Access-Control-Max-Age", "3600");
        response.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept, Authorization, X-Auth-Token, X-Space");
        response.setHeader("Access-Control-Allow-Credentials", "true");
 
        return delegate.call(ctx);
      }
 
      response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, Content-Type, X-Auth-Token");
      return delegate.call(ctx);
    }
    
  }
}
