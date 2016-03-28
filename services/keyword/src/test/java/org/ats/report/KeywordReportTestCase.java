/**
 * TrinhTV3@fsoft.com.vn
 */
package org.ats.report;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.ats.common.PageList;
import org.ats.services.DataDrivenModule;
import org.ats.services.KeywordServiceModule;
import org.ats.services.OrganizationServiceModule;
import org.ats.services.data.DatabaseModule;
import org.ats.services.data.MongoDBService;
import org.ats.services.event.EventModule;
import org.ats.services.event.EventService;
import org.ats.services.keyword.report.CaseReportService;
import org.ats.services.keyword.report.KeywordReportService;
import org.ats.services.keyword.report.StepReportService;
import org.ats.services.keyword.report.SuiteReportService;
import org.ats.services.keyword.report.models.CaseReport;
import org.ats.services.keyword.report.models.StepReport;
import org.ats.services.keyword.report.models.SuiteReport;
import org.ats.services.organization.base.AuthenticationService;
import org.ats.services.organization.entity.Space;
import org.ats.services.organization.entity.Tenant;
import org.ats.services.organization.entity.User;
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
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

/**
 * @author TrinhTV3
 *
 */
public class KeywordReportTestCase extends AbstractEventTestCase {
  
  private KeywordReportService keywordReportService;
  
  private AuthenticationService<User> authService;
  
  private Tenant tenant;
  private Space space;
  private User user;
  
  private SuiteReportService suiteReportService;
  
  private CaseReportService caseReportService;
  
  private StepReportService stepReportService;
  
  
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
    //keyword
    this.keywordReportService = injector.getInstance(KeywordReportService.class);
    this.suiteReportService = injector.getInstance(SuiteReportService.class);
    this.caseReportService = injector.getInstance(CaseReportService.class);
    this.stepReportService = injector.getInstance(StepReportService.class);
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
  
  /**
   * One suite, 3 case, 4 steps
   * 1 case fail, 1 steps fail
   * @throws IOException
   */
  
  @Test
  public void testLogParser() throws IOException {
    
    keywordReportService.processLog(new FileInputStream("src/test/resources/log_structure.txt"));
    
    PageList<SuiteReport> suites = suiteReportService.query(new BasicDBObject("name", "suiteName"));
    
    SuiteReport report = suites.next().get(0);
    
    Assert.assertEquals(report.getName(), "suiteName");
    Assert.assertEquals(report.getCases().size(), 3);
    
    PageList<CaseReport> caseReports = caseReportService.query(new BasicDBObject("suite_report_id", report.getId()));
    Assert.assertTrue(caseReports.count() == 3);
    Assert.assertEquals(caseReportService.query(new BasicDBObject("isPass", true)).count(), 2);
    
    Assert.assertTrue(stepReportService.count() == 4);
    PageList<StepReport> failSteps = stepReportService.query(new BasicDBObject("isPass", false));
    Assert.assertEquals(failSteps.count(), 1);
    
    PageList<StepReport> trueSteps = stepReportService.query(new BasicDBObject("isPass", true));
    Assert.assertEquals(trueSteps.count(), 3);
    
  }
  
  /**
   * Run 2 suite 
   * 3 cases, 9 steps in suite1 (1 case use 2 datasets)
   * 2 cases, 6 steps in suite2 (1 case use 2 datasets)
   * No case fail, no step fail
   * @throws FileNotFoundException 
   */
  @Test
  public void testLogParserWith2Suite() throws Exception {
    keywordReportService.processLog(new FileInputStream("src/test/resources/log_structure_2_suite"));
    
    Assert.assertEquals(suiteReportService.count(), 2);
    Assert.assertEquals(caseReportService.count(), 7);
    Assert.assertEquals(stepReportService.count(), 15);
    
    SuiteReport suite = suiteReportService.query(new BasicDBObject("name", "suiteName")).next().get(0);
    Assert.assertEquals(((BasicDBList) suite.get("cases")).size(), 4);
    Assert.assertEquals(suite.getTotalCase(), 3);
    PageList<CaseReport> cases = caseReportService.query(new BasicDBObject("isPass", true));
    Assert.assertEquals(cases.count(), 7);
    
    PageList<StepReport> steps = stepReportService.query(new BasicDBObject("isPass", true));
    Assert.assertEquals(steps.count(), 15);
    
  }
  
  @Test
  public void testLogParserFinal() throws IOException {
    keywordReportService.processLog(new FileInputStream("src/test/resources/log_structure_final_test"));
    PageList<SuiteReport> suites = suiteReportService.list();
    Assert.assertEquals(suites.count(), 4);
    SuiteReport suite = suiteReportService.query(new BasicDBObject("name", "Invitation")).next().get(0);
    Assert.assertEquals(suite.getTotalCase(), 24);
    Assert.assertEquals(suite.getCases().size(), 25);
    Assert.assertEquals(suite.getTotalPass(), 22);
  }
  
  @Test
  public void testLogParserFinalTest() throws IOException {
    keywordReportService.processLog(new FileInputStream("src/test/resources/test"));
    PageList<SuiteReport> suites = suiteReportService.list();
    Assert.assertEquals(suites.count(), 1);
  }
}
