/**
 * 
 */
package org.ats.services.executor;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.ats.services.DataDrivenModule;
import org.ats.services.ExecutorModule;
import org.ats.services.GeneratorModule;
import org.ats.services.KeywordServiceModule;
import org.ats.services.KeywordUploadServiceModule;
import org.ats.services.OrganizationContext;
import org.ats.services.OrganizationServiceModule;
import org.ats.services.PerformanceServiceModule;
import org.ats.services.data.DatabaseModule;
import org.ats.services.data.MongoDBService;
import org.ats.services.event.EventModule;
import org.ats.services.event.EventService;
import org.ats.services.executor.job.AbstractJob;
import org.ats.services.executor.job.AbstractJob.Status;
import org.ats.services.executor.job.KeywordJob;
import org.ats.services.executor.job.KeywordUploadJob;
import org.ats.services.executor.job.PerformanceJob;
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
import org.ats.services.performance.JMeterFactory;
import org.ats.services.performance.JMeterSampler;
import org.ats.services.performance.JMeterScript;
import org.ats.services.performance.JMeterScriptReference;
import org.ats.services.performance.JMeterScriptService;
import org.ats.services.performance.PerformanceProject;
import org.ats.services.performance.PerformanceProjectFactory;
import org.ats.services.performance.PerformanceProjectService;
import org.ats.services.upload.KeywordUploadProject;
import org.ats.services.upload.KeywordUploadProjectFactory;
import org.ats.services.upload.KeywordUploadProjectService;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Guice;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Jul 22, 2015
 */
public class ExecutorServiceTestCase extends AbstractEventTestCase {
  
  private AuthenticationService<User> authService;
  private OrganizationContext context;
  
  private Tenant tenant;
  private User user;
  
  private PerformanceProjectFactory perfFactory;
  private PerformanceProjectService perfService;
  private ReferenceFactory<JMeterScriptReference> jmeterScriptRef;
  private JMeterScriptService  jmeterService;
  
  private KeywordProjectService keywordProjectService;
  private KeywordProjectFactory keywordProjectFactory;
  private SuiteService suiteService;
  private SuiteFactory suiteFactory;
  private ReferenceFactory<SuiteReference> suiteRefFactory;
  private CaseFactory caseFactory;
  private CaseService caseService;
  private ReferenceFactory<CaseReference> caseRefFactory;
  
  private ExecutorService executorService;
  
  private KeywordUploadProjectService uploadProjectService;
  private KeywordUploadProjectFactory uploadProjectFactory;
  
  private ExecutorUploadService executorUploadService;
  
  private IaaSService openstackService;
  
  private IaaSServiceProvider iaasProvider;
  
  @BeforeClass
  public void init() throws Exception {
    System.setProperty("jenkins.slave.credential", "965a0c50-868c-48b1-8f3e-b0179bf40666");
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
        new KeywordUploadServiceModule(),
        new GeneratorModule(),
        vmModule,
        new ExecutorModule());
    
    this.mongoService = injector.getInstance(MongoDBService.class);
    this.mongoService.dropDatabase();
    
    this.authService = injector.getInstance(Key.get(new TypeLiteral<AuthenticationService<User>>(){}));
    this.context = injector.getInstance(OrganizationContext.class);
    
    //performance
    this.perfFactory = injector.getInstance(PerformanceProjectFactory.class);
    this.perfService = injector.getInstance(PerformanceProjectService.class);
    this.jmeterScriptRef = this.injector.getInstance(Key.get(new TypeLiteral<ReferenceFactory<JMeterScriptReference>>(){}));
    this.jmeterService = this.injector.getInstance(JMeterScriptService.class);
    
    //keyword
    this.keywordProjectService = injector.getInstance(KeywordProjectService.class);
    this.keywordProjectFactory = injector.getInstance(KeywordProjectFactory.class);
    
    this.suiteService = injector.getInstance(SuiteService.class);
    this.suiteFactory = injector.getInstance(SuiteFactory.class);
    this.suiteRefFactory = injector.getInstance(Key.get(new TypeLiteral<ReferenceFactory<SuiteReference>>(){}));
    
    this.caseService = injector.getInstance(CaseService.class);
    this.caseFactory = injector.getInstance(CaseFactory.class);
    this.caseRefFactory = injector.getInstance(Key.get(new TypeLiteral<ReferenceFactory<CaseReference>>(){}));
    
    //keyword upload project
    this.uploadProjectService = injector.getInstance(KeywordUploadProjectService.class);
    this.uploadProjectFactory = injector.getInstance(KeywordUploadProjectFactory.class);
    
    this.iaasProvider = injector.getInstance(IaaSServiceProvider.class);
    this.openstackService = iaasProvider.get();
    
    this.executorService = injector.getInstance(ExecutorService.class);
   
    //start event service
    this.eventService = injector.getInstance(EventService.class);
    this.eventService.setInjector(injector);
    this.eventService.start();
    
    initService();
    
    this.tenant = tenantFactory.create("fsoft-testonly");
    this.tenantService.create(this.tenant);

    this.openstackService.addCredential("admin", "admin",  "ADMIN_PASS");
    this.openstackService.initTenant(tenantRefFactory.create(this.tenant.getId()));

    this.openstackService.addCredential("fsoft-testonly");
    
    this.user = userFactory.create("haint@cloud-ats.net", "Hai", "Nguyen");
    this.user.setTenant(tenantRefFactory.create(this.tenant.getId()));
    this.user.setPassword("12345");
    this.userService.create(this.user);
    
    this.authService.logIn("haint@cloud-ats.net", "12345");
  }
  
  @AfterClass
  public void shutdown() throws Exception {
    this.openstackService.destroyTenant(tenantRefFactory.create(this.tenant.getId()));
    this.mongoService.dropDatabase();
  }
  
  @Test
  public void testExecutePerformanceProject() throws Exception {
    PerformanceProject project = perfFactory.create("Test Performance");
    
    JMeterFactory factory = new JMeterFactory();
    JMeterSampler loginPost = factory.createHttpPost("Login codeproject post", 
        "https://www.codeproject.com/script/Membership/LogOn.aspx?rp=%2f%3floginkey%3dfalse",
        "kakalot", 0,
        factory.createArgument("FormName", "MenuBarForm"),
        factory.createArgument("Email", "kakalot8x08@gmail.com"),
        factory.createArgument("Password", "tititi"));
    
    JMeterSampler gotoArticle = factory.createHttpGet("Go to top article", 
        "http://www.codeproject.com/script/Articles/TopArticles.aspx?ta_so=5",
        null, 0);
    
    JMeterSampler google = factory.createHttpGet("Go to Google", 
        "https://www.google.com",
        null, 0);
    
    JMeterScript loginScript = factory.createJmeterScript(
        "LoginCodeProject",
        1, 20, 5, false, 0, project.getId(), 
        loginPost);
    
    jmeterService.create(loginScript);
    
    JMeterScript gotoArticleScript = factory.createJmeterScript(
        "GoToArticle", 
        1, 20, 5, false, 0, project.getId(), 
        gotoArticle, google);
    
    jmeterService.create(gotoArticleScript);
    
    perfService.create(project);
    
    PerformanceJob job = executorService.execute(project, Arrays.asList(
        jmeterScriptRef.create(loginScript.getId()),
        jmeterScriptRef.create(gotoArticleScript.getId())));
    
    Assert.assertEquals(job.getStatus(), Status.Queued);
    Assert.assertNull(job.getTestVMachineId());
    
    job = (PerformanceJob) waitUntilJobFinish(job);
    
    job = (PerformanceJob) executorService.get(job.getId());

    project = perfService.get(project.getId());
    
    Assert.assertEquals(job.getStatus(), AbstractJob.Status.Completed);
    Assert.assertNotNull(job.getRawDataOutput());
    Assert.assertEquals(job.getRawDataOutput().size(), 2);
    Assert.assertTrue(job.getRawDataOutput().keySet().contains(loginScript.getId()));
    Assert.assertTrue(job.getRawDataOutput().keySet().contains(gotoArticleScript.getId()));
    Assert.assertEquals(project.getStatus(), PerformanceProject.Status.READY);
  }

  @Test
  public void testExecuteKeywordProject() throws Exception {
    
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

    Suite fullExampleSuite= suiteFactory.create(project.getId(), "FullExample", SuiteFactory.DEFAULT_INIT_DRIVER, cases);
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
    
    KeywordJob job = executorService.execute(project, Arrays.asList(
        suiteRefFactory.create(fullExampleSuite.getId()),
        suiteRefFactory.create(acceptAlertSuite.getId())));
    
    Assert.assertEquals(job.getStatus(), Status.Queued);
    Assert.assertNull(job.getTestVMachineId());
    
    job = (KeywordJob) waitUntilJobFinish(job);
    
    project = keywordProjectService.get(project.getId());
    
    job = (KeywordJob) executorService.get(job.getId());
    Assert.assertEquals(job.getStatus(), AbstractJob.Status.Completed);
    Assert.assertNotNull(job.getRawDataOutput());
    Assert.assertEquals(job.getRawDataOutput().size(), 1);
    Assert.assertTrue(job.getRawDataOutput().keySet().contains("report"));
    Assert.assertEquals(project.getStatus(), KeywordProject.Status.READY);
  }
  
  @Test
  public void testExecutorKeywordUpload() throws Exception {
    KeywordUploadProject uploadProject = uploadProjectFactory.create(context, "Upload Project");
    
    FileInputStream fis = null;
    File uploadFile = new File("/executor/src/test/resources/TestGithubUpload.zip");
    byte[] bFile = new byte[(int) uploadFile.length()];
    
    fis = new FileInputStream(uploadFile);
    fis.read(bFile);
    fis.close();
    
    uploadProject.setRawData(bFile);
    uploadProjectService.update(uploadProject);
    
    KeywordUploadJob uploadJob = executorUploadService.execute(uploadProject);
    Assert.assertEquals(uploadJob.getStatus(), AbstractJob.Status.Queued);
    Assert.assertNotNull(uploadJob.getTestVMachineId());
    
    uploadJob = (KeywordUploadJob) waitUntilUploadJobFinish(uploadJob);
    
    uploadProject = uploadProjectService.get(uploadProject.getId());
    uploadJob = (KeywordUploadJob) executorUploadService.get(uploadJob.getId());
    Assert.assertEquals(uploadJob.getStatus(), AbstractJob.Status.Completed);
    Assert.assertNotNull(uploadJob.getRawData());
    Assert.assertEquals(uploadProject.getStatus(), KeywordProject.Status.READY);
    
  }
  
  private AbstractJob<?> waitUntilJobFinish(AbstractJob<?> job) throws InterruptedException {
    while (job.getStatus() != Status.Completed) {
      job = executorService.get(job.getId());
      Thread.sleep(3000);
    }
    return job;
  }
  
  private AbstractJob<?> waitUntilUploadJobFinish(AbstractJob<?> uploadJob) throws InterruptedException {
    while (uploadJob.getStatus() != Status.Completed) {
      uploadJob = executorUploadService.get(uploadJob.getId());
      Thread.sleep(3000);
    }
    return uploadJob;
  }

}
