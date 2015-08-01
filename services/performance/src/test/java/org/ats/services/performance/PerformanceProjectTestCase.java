/**
 * 
 */
package org.ats.services.performance;

import java.util.List;

import org.ats.common.PageList;
import org.ats.services.OrganizationContext;
import org.ats.services.OrganizationServiceModule;
import org.ats.services.PerformanceServiceModule;
import org.ats.services.data.DatabaseModule;
import org.ats.services.data.MongoDBService;
import org.ats.services.event.EventModule;
import org.ats.services.event.EventService;
import org.ats.services.organization.base.AuthenticationService;
import org.ats.services.organization.entity.Space;
import org.ats.services.organization.entity.Tenant;
import org.ats.services.organization.entity.User;
import org.ats.services.organization.entity.fatory.ReferenceFactory;
import org.ats.services.organization.event.AbstractEventTestCase;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.inject.Guice;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;

/**
 * @author NamBV2
 *
 * Jun 17, 2015
 */
public class PerformanceProjectTestCase extends AbstractEventTestCase{
  
  private AuthenticationService<User> authService;
  
  private OrganizationContext context;
  
  private PerformanceProjectFactory factory;
  
  private PerformanceProjectService service;
  
  private ReferenceFactory<JMeterScriptReference> jmeterScriptRef;
  
  private JMeterScriptService  jmeterService;
  
  private Tenant tenant;

  private Space space;

  private User user;
  
  @BeforeClass
  public void init() throws Exception {
    this.injector = Guice.createInjector(
        new DatabaseModule(),
        new EventModule(),
        new PerformanceServiceModule(),
        new OrganizationServiceModule());
    this.factory = injector.getInstance(PerformanceProjectFactory.class);
    this.service = injector.getInstance(PerformanceProjectService.class);
    this.authService = injector.getInstance(Key.get(new TypeLiteral<AuthenticationService<User>>(){}));
    this.context = this.injector.getInstance(OrganizationContext.class);
    this.jmeterScriptRef = this.injector.getInstance(Key.get(new TypeLiteral<ReferenceFactory<JMeterScriptReference>>(){}));
    this.jmeterService = this.injector.getInstance(JMeterScriptService.class);
    this.mongoService = injector.getInstance(MongoDBService.class);
    this.mongoService.dropDatabase();
    
    //start event service
    this.eventService = injector.getInstance(EventService.class);
    this.eventService.setInjector(injector);
    this.eventService.start();
    
    initService();
  }
  
  @AfterClass
  public void shutdown() throws Exception {
    this.mongoService.dropDatabase();
  }

  @BeforeMethod
  public void setup() throws Exception {
    this.tenant = tenantFactory.create("Fsoft");
    this.tenantService.create(this.tenant);

    this.space = spaceFactory.create("FSU1.BU11");
    this.space.setTenant(tenantRefFactory.create(this.tenant.getId()));
    this.spaceService.create(this.space);

    this.user = userFactory.create("haint@cloud-ats.net", "Hai", "Nguyen");
    this.user.setTenant(tenantRefFactory.create(this.tenant.getId()));
    this.user.joinSpace(spaceRefFactory.create(this.space.getId()));
    this.user.setPassword("12345");
    this.userService.create(this.user);
  }
  
  @AfterMethod
  public void tearDown() {
    this.authService.logOut();
    this.mongoService.dropDatabase();
  }
  
  @Test
  public void testCRUD() throws Exception {
    
    PerformanceProject performanceProject = null;
    try {
      performanceProject = factory.create("Test Performance");
      Assert.fail();
    } catch (IllegalStateException e) {

    }
    this.authService.logIn("haint@cloud-ats.net", "12345");
    this.spaceService.goTo(spaceRefFactory.create(this.space.getId()));
    Assert.assertNotNull(this.context.getUser());
    Assert.assertNotNull(this.context.getTenant());
    Assert.assertNotNull(this.context.getSpace());
    
    try {
      performanceProject = factory.create("Test Performance");
    } catch (IllegalStateException e) {
      e.printStackTrace();
      Assert.fail();
    }
    
    Assert.assertNotNull(performanceProject);
    Assert.assertEquals(performanceProject.getSpace().getId(),this.space.getId());
    Assert.assertEquals(performanceProject.getCreator().getId(), "haint@cloud-ats.net");
    
    service.create(performanceProject);
    Assert.assertEquals(service.count(), 1);
  }
  
  @Test
  public void testScript() throws Exception {
    
    this.authService.logIn("haint@cloud-ats.net", "12345");
    this.spaceService.goTo(spaceRefFactory.create(this.space.getId()));
    
    PerformanceProject performanceProject = factory.create("Test Performance");
    service.create(performanceProject);
    
    JMeterFactory factory = new JMeterFactory();
    JMeterSampler loginPost = factory.createHttpPost("Login", "http://localhost:9000/signin", null, 0,
        factory.createArgument("email", "root@system.com"),
        factory.createArgument("password", "admin"));
    
    JMeterScript jmeter = factory.createJmeterScript(
        "Test Name",
        1, 100, 5, false, 0,performanceProject.getId(), 
        loginPost);
    JMeterScript newJMeter = factory.createJmeterScript(
        "New Script",
        1, 100, 5, false, 0, performanceProject.getId(), 
        loginPost);
    
    jmeterService.create(jmeter, newJMeter);
    
    PageList<JMeterScript> pages = jmeterService.getJmeterScripts(performanceProject.getId());
    
    Assert.assertEquals(2, pages.count());
    List<JMeterScript> list = pages.next();
    
    Assert.assertEquals("Test Name", list.get(0).getName());
    Assert.assertEquals(performanceProject.getId(), jmeter.getProjectId());
  }
}
