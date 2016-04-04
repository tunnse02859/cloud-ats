package org.ats.service.report.testNg;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.ats.common.PageList;
import org.ats.service.ReportModule;
import org.ats.service.report.Report;
import org.ats.service.report.ReportService;
import org.ats.service.report.ReportService.Type;
import org.ats.services.DataDrivenModule;
import org.ats.services.ExecutorModule;
import org.ats.services.GeneratorModule;
import org.ats.services.KeywordServiceModule;
import org.ats.services.OrganizationContext;
import org.ats.services.OrganizationServiceModule;
import org.ats.services.PerformanceServiceModule;
import org.ats.services.data.DatabaseModule;
import org.ats.services.data.MongoDBService;
import org.ats.services.event.EventModule;
import org.ats.services.event.EventService;
import org.ats.services.executor.ExecutorService;
import org.ats.services.executor.job.AbstractJob;
import org.ats.services.executor.job.AbstractJob.Status;
import org.ats.services.executor.job.KeywordJob;
import org.ats.services.iaas.IaaSService;
import org.ats.services.iaas.IaaSServiceProvider;
import org.ats.services.iaas.VMachineServiceModule;
import org.ats.services.keyword.Case;
import org.ats.services.keyword.CaseFactory;
import org.ats.services.keyword.CaseReference;
import org.ats.services.keyword.CaseService;
import org.ats.services.keyword.KeywordProject;
import org.ats.services.keyword.KeywordProjectFactory;
import org.ats.services.keyword.KeywordProjectService;
import org.ats.services.keyword.Suite;
import org.ats.services.keyword.SuiteFactory;
import org.ats.services.keyword.SuiteReference;
import org.ats.services.keyword.SuiteService;
import org.ats.services.organization.base.AuthenticationService;
import org.ats.services.organization.entity.Tenant;
import org.ats.services.organization.entity.User;
import org.ats.services.organization.entity.fatory.ReferenceFactory;
import org.ats.services.organization.event.AbstractEventTestCase;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Guice;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.mongodb.BasicDBObject;

public class TestNgReport extends AbstractEventTestCase {

  private AuthenticationService<User> authService;
  private OrganizationContext context;

  private Tenant tenant;
  private User user;

  private KeywordProjectService keywordProjectService;
  private KeywordProjectFactory keywordProjectFactory;
  private SuiteService suiteService;
  private SuiteFactory suiteFactory;
  private ReferenceFactory<SuiteReference> suiteRefFactory;
  private CaseFactory caseFactory;
  private CaseService caseService;
  private ReferenceFactory<CaseReference> caseRefFactory;

  private ExecutorService executorService;

  private IaaSService openstackService;
  
  private IaaSServiceProvider iaasProvider;

  private ReportService reportService;

  @BeforeClass
  public void init() throws Exception {
    System.setProperty(EventModule.EVENT_CONF, "src/test/resources/event.conf");
    
    VMachineServiceModule vmModule = new VMachineServiceModule("src/test/resources/iaas.conf");
    vmModule.setProperty("org.ats.cloud.iaas", "org.ats.services.iaas.OpenStackService");

    this.injector = Guice.createInjector(
        new DatabaseModule(), 
        new EventModule(), 
        new OrganizationServiceModule(), 
        new DataDrivenModule(),
        new KeywordServiceModule(), 
        new PerformanceServiceModule(), 
        new GeneratorModule(), 
        vmModule,
        new ExecutorModule(), new ReportModule());

    this.mongoService = injector.getInstance(MongoDBService.class);
    this.mongoService.dropDatabase();

    this.authService = injector.getInstance(Key.get(new TypeLiteral<AuthenticationService<User>>() {
    }));
    this.context = injector.getInstance(OrganizationContext.class);

    // keyword
    this.keywordProjectService = injector.getInstance(KeywordProjectService.class);
    this.keywordProjectFactory = injector.getInstance(KeywordProjectFactory.class);

    this.suiteService = injector.getInstance(SuiteService.class);
    this.suiteFactory = injector.getInstance(SuiteFactory.class);
    this.suiteRefFactory = injector.getInstance(Key.get(new TypeLiteral<ReferenceFactory<SuiteReference>>() {
    }));

    this.caseService = injector.getInstance(CaseService.class);
    this.caseFactory = injector.getInstance(CaseFactory.class);
    this.caseRefFactory = injector.getInstance(Key.get(new TypeLiteral<ReferenceFactory<CaseReference>>() {
    }));

    this.executorService = injector.getInstance(ExecutorService.class);

    this.iaasProvider = injector.getInstance(IaaSServiceProvider.class);
    this.openstackService = iaasProvider.get();

    // start event service
    this.eventService = injector.getInstance(EventService.class);
    this.eventService.setInjector(injector);
    this.eventService.start();

    initService();

    this.tenant = tenantFactory.create("fsoft-testonly");
    this.tenantService.create(this.tenant);

    this.openstackService.addCredential("admin", "admin", "ADMIN_PASS");
    this.openstackService.initTenant(tenantRefFactory.create(this.tenant.getId()));

    this.openstackService.addCredential("fsoft-testonly");

    this.user = userFactory.create("haint@cloud-ats.net", "Hai", "Nguyen");
    this.user.setTenant(tenantRefFactory.create(this.tenant.getId()));
    this.user.setPassword("12345");
    this.userService.create(this.user);

    this.authService.logIn("haint@cloud-ats.net", "12345");

    this.reportService = injector.getInstance(ReportService.class);
  }

  @AfterClass
  public void shutdown() throws Exception {
    this.openstackService.destroyTenant(tenantRefFactory.create(this.tenant.getId()));
    this.mongoService.dropDatabase();
  }

  @Test
  public void testParseTestNgContent() throws Exception {

    KeywordProject project = keywordProjectFactory.create(context, "Full Example");

    ObjectMapper m = new ObjectMapper();
    JsonNode rootNode = m.readTree(new File("src/test/resources/full_example.json"));
    
    JsonNode stepsNode = rootNode.get("steps");
    List<CaseReference> cases = new ArrayList<CaseReference>();
    Case caze = caseFactory.create(project.getId(), "test", null);
    for (JsonNode json : stepsNode) {
      caze.addAction(json);
    }
    caseService.create(caze);
    cases.add(caseRefFactory.create(caze.getId()));

    Suite fullExampleSuite = suiteFactory.create(project.getId(), "FullExample", SuiteFactory.DEFAULT_INIT_DRIVER,cases);
    suiteService.create(fullExampleSuite);

    rootNode = m.readTree(new File("src/test/resources/acceptAlert.json"));
    stepsNode = rootNode.get("steps");
    cases.clear();
    caze = caseFactory.create(project.getId(), "test", null);
    for (JsonNode json : stepsNode) {
      caze.addAction(json);
    }
    caseService.create(caze);
    cases.add(caseRefFactory.create(caze.getId()));

    Suite acceptAlertSuite = suiteFactory.create(project.getId(), "AcceptAlert", SuiteFactory.DEFAULT_INIT_DRIVER, cases);
    suiteService.create(acceptAlertSuite);

    keywordProjectService.create(project);

    KeywordJob job = executorService.execute(project,
        Arrays.asList(suiteRefFactory.create(fullExampleSuite.getId()), suiteRefFactory.create(acceptAlertSuite.getId())), new BasicDBObject());

    Assert.assertEquals(job.getStatus(), Status.Queued);
    Assert.assertNull(job.getTestVMachineId());

    job = (KeywordJob) waitUntilJobFinish(job);

    job = (KeywordJob) executorService.get(job.getId());
    Assert.assertEquals(job.getStatus(), AbstractJob.Status.Completed);
    Assert.assertNotNull(job.getRawDataOutput());
    Assert.assertEquals(job.getRawDataOutput().size(), 1);
    Assert.assertTrue(job.getRawDataOutput().keySet().contains("report"));

    PageList<Report> list = reportService.getList(job.getId(), Type.FUNCTIONAL, null);    
    Assert.assertTrue(list.count() > 0);

  }

  private AbstractJob<?> waitUntilJobFinish(AbstractJob<?> job) throws InterruptedException {
    while (job.getStatus() != Status.Completed) {
      job = executorService.get(job.getId());
      Thread.sleep(3000);
    }
    return job;
  }

}
