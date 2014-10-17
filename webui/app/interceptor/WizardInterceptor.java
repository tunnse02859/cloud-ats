package interceptor;
import org.ats.component.usersmgt.user.UserDAO;

import play.libs.F.Promise;
import play.mvc.Action.Simple;
import play.mvc.Http.Context;
import play.mvc.SimpleResult;

import com.mongodb.BasicDBObject;

import controllers.Application;

/**
 * 
 */

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Aug 4, 2014
 */
public class WizardInterceptor extends Simple {

  @Override
  public Promise<SimpleResult> call(Context ctx) throws Throwable {
    if (UserDAO.getInstance(Application.dbName).find(new BasicDBObject("system", true)).isEmpty()
        && !ctx._requestHeader().path().equals("/wizard")) {
      
      return Promise.<SimpleResult>pure(redirect("/wizard"));
    } 
    
    if (!UserDAO.getInstance(Application.dbName).find(new BasicDBObject("system", true)).isEmpty() 
        && ctx._requestHeader().path().equals("/wizard")){
      
      return Promise.<SimpleResult>pure(redirect("/"));
    }
    
    return delegate.call(ctx);
  }

}
