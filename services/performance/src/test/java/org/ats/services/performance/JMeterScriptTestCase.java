/**
 * 
 */
package org.ats.services.performance;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.ats.service.BlobModule;
import org.ats.service.blob.FileService;
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
import org.ats.services.organization.event.AbstractEventTestCase;
import org.ats.services.performance.JMeterSampler.Method;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.inject.Guice;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;

/**
 * @author NamBV2
 *
 * Jun 17, 2015
 */
public class JMeterScriptTestCase extends AbstractEventTestCase {
  
private AuthenticationService<User> authService;
  
  private PerformanceProjectFactory factory;
  
  private PerformanceProjectService service;
  
  private JMeterScriptService  jmeterService;
  
  private FileService fileService;
  
  private Tenant tenant;

  private Space space;

  private User user;
  
  @BeforeClass
  public void init() throws Exception {
    this.injector = Guice.createInjector(
        new DatabaseModule(),
        new EventModule(),
        new PerformanceServiceModule(),
        new OrganizationServiceModule(),
        new BlobModule());
    this.factory = injector.getInstance(PerformanceProjectFactory.class);
    this.service = injector.getInstance(PerformanceProjectService.class);
    this.authService = injector.getInstance(Key.get(new TypeLiteral<AuthenticationService<User>>(){}));
    this.jmeterService = this.injector.getInstance(JMeterScriptService.class);
    this.mongoService = injector.getInstance(MongoDBService.class);
    this.mongoService.dropDatabase();
    this.fileService = injector.getInstance(FileService.class);
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
    
    String projectId = "tranvantrinhsdljgksdg235234534";
    JMeterFactory factory = new JMeterFactory();
    JMeterSampler signinRequest = factory.createHttpGet("Signin Page", "http://localhost:9000/signin", "this is assertion text", 1000);
    JMeterSampler loginPost = factory.createHttpPost("Login", "http://localhost:9000/signin", null, 0,
        factory.createArgument("email", "root@system.com"),
        factory.createArgument("password", "admin"));
    JMeterSampler oRequest = factory.createHttpGet("Organization Page", 
        "http://localhost:9000/portal/o?nav=group&group=40d4edcd-ff1b-483f-9b69-50aff29f49f6", null, 0);
    JMeterSampler signoutRequest = factory.createHttpGet("Signout", "http://localhost:9000/signout", null, 0);
    JMeterScript jmeter = factory.createJmeterScript(
        "Test Name",
        1, 100, 5, false, 0,projectId, 
        signinRequest, loginPost, oRequest, signoutRequest);
    
    jmeter.setName("Test Script");
    Assert.assertEquals(jmeter.getName(), "Test Script");
    
    Assert.assertEquals(jmeter.getSamplers().size(), 4);
    
    //Create new a JmeterSampler
    List<JMeterArgument> arguments = new ArrayList<JMeterArgument>();
    JMeterArgument param = factory
        .createArgument("password", "admin.password");
    arguments.add(param);
    JMeterSampler newSampler = new JMeterSampler(Method.POST, "Register",
        "http://localhost:8080/signup", null, 0L, arguments);
    jmeter.addSampler(newSampler);
    Assert.assertEquals(jmeter.getSamplers().size(), 5);
  }
  
  @Test
  public void testMixin() throws IOException {
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
    jmeter.setNumberEngines(4);
    jmeterService.create(jmeter);
    //get by mixins function
    
    JMeterScript newScript = jmeterService.get(jmeter.getId(), "number_engines");
    Assert.assertEquals(newScript.getNumberEngines(), 4);
    
    File file1 = new File("src/test/resources/b.csv");
    File file2 = new File("src/test/resources/test.csv");
    File file3 = new File("src/test/resources/test.csv");
    
    jmeter = jmeterService.get(jmeter.getId(), "csv_files");
    jmeter.addCSVFiles(new CSV(UUID.randomUUID().toString(), file1.getName()));
    jmeterService.update(jmeter);
    
    jmeter = jmeterService.get(jmeter.getId(), "csv_files");
    jmeter.addCSVFiles(new CSV(UUID.randomUUID().toString(), file2.getName()));
    jmeterService.update(jmeter);
    
    jmeter = jmeterService.get(jmeter.getId(), "csv_files");
    jmeter.addCSVFiles(new CSV(UUID.randomUUID().toString(), file3.getName()));
    jmeterService.update(jmeter);
    
    Assert.assertEquals(jmeterService.get(jmeter.getId(), "csv_files").getCSVFiles().size(), 3);
  }
}
