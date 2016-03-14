/**
 * TrinhTV3@fsoft.com.vn
 */
package org.ats.report;

import java.io.IOException;

import org.ats.services.DataDrivenModule;
import org.ats.services.KeywordServiceModule;
import org.ats.services.OrganizationContext;
import org.ats.services.OrganizationServiceModule;
import org.ats.services.data.DatabaseModule;
import org.ats.services.data.MongoDBService;
import org.ats.services.event.EventModule;
import org.ats.services.event.EventService;
import org.ats.services.keyword.CaseFactory;
import org.ats.services.keyword.CaseReference;
import org.ats.services.keyword.CaseService;
import org.ats.services.keyword.KeywordProjectFactory;
import org.ats.services.keyword.KeywordProjectService;
import org.ats.services.keyword.SuiteFactory;
import org.ats.services.keyword.SuiteService;
import org.ats.services.keyword.report.KeywordReportService;
import org.ats.services.organization.base.AuthenticationService;
import org.ats.services.organization.entity.Space;
import org.ats.services.organization.entity.Tenant;
import org.ats.services.organization.entity.User;
import org.ats.services.organization.entity.fatory.ReferenceFactory;
import org.ats.services.organization.event.AbstractEventTestCase;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;

/**
 * @author TrinhTV3
 *
 */
public class KeywordReportTestCase extends AbstractEventTestCase {
  
  @Inject KeywordReportService keywordReportService;
  
  private AuthenticationService<User> authService;
  private OrganizationContext context;
  
  private Tenant tenant;
  private Space space;
  private User user;
  
  private KeywordProjectService keywordProjectService;
  private KeywordProjectFactory keywordProjectFactory;
  private SuiteService suiteService;
  private SuiteFactory suiteFactory;
  private CaseFactory caseFactory;
  private CaseService caseService;
  private ReferenceFactory<CaseReference> caseRefFactory;
  

  @BeforeClass
  public void init() throws Exception {
    this.injector = Guice.createInjector(
        new DatabaseModule(), 
        new EventModule(),
        new OrganizationServiceModule(),
        new DataDrivenModule(),
        new KeywordServiceModule()
        );
    
    this.mongoService = injector.getInstance(MongoDBService.class);
    this.mongoService.dropDatabase();
    
    this.authService = injector.getInstance(Key.get(new TypeLiteral<AuthenticationService<User>>(){}));
    this.context = injector.getInstance(OrganizationContext.class);
    
    
    //keyword
    this.keywordProjectService = injector.getInstance(KeywordProjectService.class);
    this.keywordProjectFactory = injector.getInstance(KeywordProjectFactory.class);
    
    this.suiteService = injector.getInstance(SuiteService.class);
    this.suiteFactory = injector.getInstance(SuiteFactory.class);
    
    this.caseService = injector.getInstance(CaseService.class);
    this.caseFactory = injector.getInstance(CaseFactory.class);
    this.caseRefFactory = injector.getInstance(Key.get(new TypeLiteral<ReferenceFactory<CaseReference>>(){}));
    
    
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
    
    this.authService.logIn("haint@cloud-ats.net", "12345");
    this.spaceService.goTo(spaceRefFactory.create(this.space.getId()));
  }
  
  
  @AfterMethod
  public void tearDown() {
    this.authService.logOut();
    this.mongoService.dropDatabase();
  }
  
  @Test
  public void testLogParser() throws IOException {
    
  }
 

}
