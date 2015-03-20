import java.io.IOException;
import java.lang.reflect.Method;

import org.ats.services.OrganizationContext;
import org.ats.services.OrganizationServiceModule;
import org.ats.services.data.DatabaseModule;
import org.ats.services.organization.SpaceService;
import org.ats.services.organization.base.AuthenticationService;
import org.ats.services.organization.entity.User;
import org.ats.services.organization.entity.fatory.SpaceReferenceFactory;

import play.Application;
import play.GlobalSettings;
import play.Play;
import play.mvc.Action;
import play.mvc.Http.Request;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;

/**
 * 
 */

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Mar 18, 2015
 */
public class Global extends GlobalSettings {
  
  /** .*/
  private Injector injector;

  @Override
  public void onStart(Application app) {
    String dbConf = Play.application().configuration().getString(DatabaseModule.DB_CONF);
    try {
      injector = Guice.createInjector(new DatabaseModule(dbConf), new OrganizationServiceModule());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
  
  @Override
  public Action<?> onRequest(Request request, Method actionMethod) {
    
    OrganizationContext context = injector.getInstance(OrganizationContext.class);
    String token = request.getHeader(AuthenticationService.AUTH_TOKEN_HEADER);
    
    if (token == null) {
      context.setUser(null);
      context.setSpace(null);
      context.setTenant(null);
      return super.onRequest(request, actionMethod);
    }
    
    String space = request.getHeader(AuthenticationService.SPACE_HEADER);
    
    AuthenticationService<User> service = injector.getInstance(Key.get(new TypeLiteral<AuthenticationService<User>>(){}));

    User user = service.findByAuthToken(token);
    if (user != null) {
      context.setUser(user);
      context.setTenant(user.getTanent().get());
    }
    
    if (user != null && space != null) {
      SpaceService spaceService = injector.getInstance(SpaceService.class);
      SpaceReferenceFactory spaceRefFactory = injector.getInstance(SpaceReferenceFactory.class);
      spaceService.goTo(spaceRefFactory.create(space));
    }
    
    return super.onRequest(request, actionMethod);
  }
  
  @Override
  public <T> T getControllerInstance(Class<T> aClass) throws Exception {
    return injector.getInstance(aClass);
  }
}
