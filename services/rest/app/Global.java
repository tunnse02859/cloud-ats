import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;

import org.ats.service.BlobModule;
import org.ats.service.ReportModule;
import org.ats.services.DataDrivenModule;
import org.ats.services.ExecutorModule;
import org.ats.services.GeneratorModule;
import org.ats.services.KeywordServiceModule;
import org.ats.services.KeywordUploadServiceModule;
import org.ats.services.MixProjectModule;
import org.ats.services.OrganizationContext;
import org.ats.services.OrganizationServiceModule;
import org.ats.services.PerformanceServiceModule;
import org.ats.services.data.DatabaseModule;
import org.ats.services.event.EventModule;
import org.ats.services.event.EventService;
import org.ats.services.iaas.IaaSService;
import org.ats.services.iaas.IaaSServiceProvider;
import org.ats.services.iaas.VMachineServiceModule;
import org.ats.services.iaas.exception.CreateVMException;
import org.ats.services.iaas.exception.InitializeTenantException;
import org.ats.services.organization.FeatureService;
import org.ats.services.organization.RoleService;
import org.ats.services.organization.SpaceService;
import org.ats.services.organization.TenantService;
import org.ats.services.organization.UserService;
import org.ats.services.organization.acl.UnAuthenticatedException;
import org.ats.services.organization.acl.UnAuthorizationException;
import org.ats.services.organization.base.AuthenticationService;
import org.ats.services.organization.entity.Feature;
import org.ats.services.organization.entity.Feature.Action;
import org.ats.services.organization.entity.Role;
import org.ats.services.organization.entity.Tenant;
import org.ats.services.organization.entity.User;
import org.ats.services.organization.entity.fatory.FeatureFactory;
import org.ats.services.organization.entity.fatory.PermissionFactory;
import org.ats.services.organization.entity.fatory.ReferenceFactory;
import org.ats.services.organization.entity.fatory.RoleFactory;
import org.ats.services.organization.entity.fatory.TenantFactory;
import org.ats.services.organization.entity.fatory.UserFactory;
import org.ats.services.organization.entity.reference.RoleReference;
import org.ats.services.organization.entity.reference.SpaceReference;
import org.ats.services.organization.entity.reference.TenantReference;

import play.Application;
import play.GlobalSettings;
import play.Play;
import play.libs.F;
import play.libs.F.Promise;
import play.mvc.Http;
import play.mvc.Http.Request;
import play.mvc.Http.RequestHeader;
import play.mvc.Http.Response;
import play.mvc.Result;
import play.mvc.Results.Status;
import actors.EventTrackingActor;

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
    String eventConf = Play.application().configuration().getString(EventModule.EVENT_CONF);
    String vmConf = Play.application().configuration().getString(VMachineServiceModule.VM_CONF);
    
    String jenkinsCredential = Play.application().configuration().getString("jenkins.slave.credential");
    System.out.println("Using jenkins credential: " + jenkinsCredential);
    System.setProperty("jenkins.slave.credential", jenkinsCredential);
    
    try {
      injector = Guice.createInjector(
          new DatabaseModule(dbConf), 
          new EventModule(eventConf), 
          new OrganizationServiceModule(),
          new DataDrivenModule(),
          new PerformanceServiceModule(),
          new GeneratorModule(),
          new VMachineServiceModule(vmConf),
          new ExecutorModule(),
          new KeywordServiceModule(),
          new KeywordUploadServiceModule(),
          new BlobModule(),
          new ReportModule(),
          new MixProjectModule());

      //start event service
      EventService eventService = injector.getInstance(EventService.class);
      eventService.setInjector(injector);
      eventService.addActor(EventTrackingActor.class, "server-bus");
      eventService.start();
      
      //hardcode to initialize fsoft tenant
      TenantService tenantService = injector.getInstance(TenantService.class);
      Tenant fsoft = tenantService.get("fsoft");
      if (fsoft == null) {
        initializeTenant(injector, "fsoft");
      }
      
      initializePolicy(injector);
      
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    super.onStart(app);
  }
  
  @Override
  public void onStop(Application app) {
    EventService eventService = injector.getInstance(EventService.class);
    eventService.stop();
    super.onStop(app);
  }
  
  @Override
  public play.mvc.Action<?> onRequest(Request request, Method actionMethod) {
    
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
    
    
    if (user == null) {
      context.setUser(null);
      context.setSpace(null);
      context.setTenant(null);
      return super.onRequest(request, actionMethod);
    }

    context.setUser(user);
    context.setTenant(user.getTanent().get());

    if (space != null) {
      SpaceService spaceService = injector.getInstance(SpaceService.class);
      ReferenceFactory<SpaceReference> spaceRefFactory = injector.getInstance(Key.get(new TypeLiteral<ReferenceFactory<SpaceReference>>(){}));
      spaceService.goTo(spaceRefFactory.create(space));
    }

    return super.onRequest(request, actionMethod);
  }
  
  @Override
  public F.Promise<Result> onError(RequestHeader request, Throwable t) {
    Response response = Http.Context.current().response();
    response.setHeader("Access-Control-Allow-Origin", "*");
    
    if (t.getCause() instanceof UnAuthenticatedException) {
      return Promise.<Result>pure(new Status(play.core.j.JavaResults.Status(403)));
    } else if (t.getCause() instanceof UnAuthorizationException) {
      return Promise.<Result>pure(new Status(play.core.j.JavaResults.Status(401)));
    }
    return super.onError(request, t);
  }
  
  @Override
  public <T> T getControllerInstance(Class<T> aClass) throws Exception {
    return injector.getInstance(aClass);
  }
  
  private void initializeTenant(Injector injector, String tenantId) throws InitializeTenantException, CreateVMException {
    TenantService tenantService = injector.getInstance(TenantService.class);
    TenantFactory tenantFactory = injector.getInstance(TenantFactory.class);
    ReferenceFactory<TenantReference> tenantRefFactory = injector.getInstance(Key.get(new TypeLiteral<ReferenceFactory<TenantReference>>(){}));
    
    Tenant tenant = tenantFactory.create(tenantId);
    tenantService.create(tenant);
    
    IaaSServiceProvider iaasProvider = injector.getInstance(IaaSServiceProvider.class);
    IaaSService iaasService = iaasProvider.get();
    iaasService.initTenant(tenantRefFactory.create(tenant.getId()));
  }
  
  private void initializePolicy(Injector injector) throws IOException {
    BufferedReader reader = new BufferedReader(new FileReader("conf/policy.conf"));
    
    UserService userService = injector.getInstance(UserService.class);
    UserFactory userFactory = injector.getInstance(UserFactory.class);
    RoleService roleService = injector.getInstance(RoleService.class);
    RoleFactory roleFactory = injector.getInstance(RoleFactory.class);
    ReferenceFactory<RoleReference> roleRefFactory = injector.getInstance(Key.get(new TypeLiteral<ReferenceFactory<RoleReference>>(){}));
    ReferenceFactory<TenantReference> tenantRefFactory = injector.getInstance(Key.get(new TypeLiteral<ReferenceFactory<TenantReference>>(){}));
    PermissionFactory permFactory = injector.getInstance(PermissionFactory.class);
    FeatureService featureService = injector.getInstance(FeatureService.class);
    FeatureFactory featureFactory = injector.getInstance(FeatureFactory.class);
    
    User root = userService.get("root@cloudats.net");
    if (root == null) {
      root = userFactory.create("root@cloudats.net", "The", "God");
      root.setTenant(tenantRefFactory.create("fsoft"));
      root.setPassword("QWerty");
      Role role = roleFactory.create("root");
      role.addPermission(permFactory.create("*:*@fsoft:*"));
      roleService.create(role);
      root.addRole(roleRefFactory.create(role.getId()));
      userService.create(root);
    }
    
    String line = null;
    while ((line = reader.readLine()) != null) {
      String[] array = line.split(":");
      String f = array[0];
      String a = array[1];
      Feature feature = featureService.get(f);
      if (feature == null) {
        feature = featureFactory.create(f);
        feature.addAction(new Action(a));
        featureService.create(feature);
      } else {
        if (!feature.hasAction(new Action(a))) feature.addAction(new Action(a));
        featureService.update(feature);
      }
    }
    reader.close();
  }
}
