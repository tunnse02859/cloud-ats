/**
 * TrinhTV3@fsoft.com.vn
 */
package org.ats.services.project;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.ats.common.PageList;
import org.ats.common.StringUtil;
import org.ats.services.DataDrivenModule;
import org.ats.services.KeywordServiceModule;
import org.ats.services.KeywordUploadServiceModule;
import org.ats.services.MixProjectModule;
import org.ats.services.OrganizationContext;
import org.ats.services.OrganizationServiceModule;
import org.ats.services.PerformanceServiceModule;
import org.ats.services.data.DatabaseModule;
import org.ats.services.data.MongoDBService;
import org.ats.services.event.EventModule;
import org.ats.services.keyword.Case;
import org.ats.services.keyword.CaseFactory;
import org.ats.services.keyword.CaseReference;
import org.ats.services.keyword.CaseService;
import org.ats.services.keyword.CustomKeyword;
import org.ats.services.keyword.CustomKeywordFactory;
import org.ats.services.keyword.CustomKeywordService;
import org.ats.services.keyword.KeywordProject;
import org.ats.services.keyword.KeywordProjectFactory;
import org.ats.services.keyword.KeywordProjectService;
import org.ats.services.keyword.Suite;
import org.ats.services.keyword.SuiteFactory;
import org.ats.services.keyword.SuiteService;
import org.ats.services.organization.base.AuthenticationService;
import org.ats.services.organization.entity.Space;
import org.ats.services.organization.entity.Tenant;
import org.ats.services.organization.entity.User;
import org.ats.services.organization.entity.fatory.ReferenceFactory;
import org.ats.services.organization.event.AbstractEventTestCase;
import org.ats.services.performance.JMeterFactory;
import org.ats.services.performance.JMeterSampler;
import org.ats.services.performance.JMeterScript;
import org.ats.services.performance.JMeterScriptService;
import org.ats.services.performance.PerformanceProject;
import org.ats.services.performance.PerformanceProjectFactory;
import org.ats.services.performance.PerformanceProjectService;
import org.ats.services.upload.SeleniumUploadProject;
import org.ats.services.upload.SeleniumUploadProjectFactory;
import org.ats.services.upload.SeleniumUploadProjectService;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Guice;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.mongodb.BasicDBObject;

/**
 * @author TrinhTV3
 *
 */
public class CloneDataTestCase extends AbstractEventTestCase {
  
  private MongoDBService mongoService;
  
  private PerformanceProjectService performanceProjectService;
  
  private KeywordProjectService keywordProjectService;
  
  private SeleniumUploadProjectService seleniumService;
  
  private MixProjectService mpService;
  
  private MixProjectFactory mpFactory;
  
  private SuiteService suiteService;
  
  private CaseService caseService;
  
  private PerformanceProjectFactory performanceFactory;
  
  private KeywordProjectFactory keywordFactory;
  
  private SuiteFactory suiteFactory;
  
  private CaseFactory caseFactory;
  
  private JMeterScriptService jmeterService;
  
  private JMeterFactory jmeterFactory;
  
  private OrganizationContext context;
  
  private SeleniumUploadProjectFactory seleniumFactory;
  
  private ReferenceFactory<CaseReference> caseRefFactory;
  
  private CustomKeywordFactory customFactory;
  
  private CustomKeywordService customService;
  
  private Tenant tenant;

  private Space space;

  private User user;
  
  private AuthenticationService<User> authService;
  
  @BeforeClass
  public void init() throws Exception {
    String host = "localhost";
    String name = "db-test";
    int port = 27017;
    
    this.injector = Guice.createInjector(new DatabaseModule(host, port, name), 
        new KeywordServiceModule(), new PerformanceServiceModule(), 
        new KeywordUploadServiceModule(), new MixProjectModule(), 
        new EventModule(), new OrganizationServiceModule(), new DataDrivenModule());
    
    this.mongoService = injector.getInstance(MongoDBService.class);
    this.performanceProjectService = injector.getInstance(PerformanceProjectService.class);
    this.keywordProjectService = injector.getInstance(KeywordProjectService.class);
    this.seleniumService = injector.getInstance(SeleniumUploadProjectService.class);
    this.mpService = injector.getInstance(MixProjectService.class);
    this.mpFactory = injector.getInstance(MixProjectFactory.class);
    this.suiteService = injector.getInstance(SuiteService.class);
    this.caseService = injector.getInstance(CaseService.class);
    this.performanceFactory = injector.getInstance(PerformanceProjectFactory.class);
    this.keywordFactory = injector.getInstance(KeywordProjectFactory.class);
    this.suiteFactory = injector.getInstance(SuiteFactory.class);
    this.caseFactory = injector.getInstance(CaseFactory.class);
    this.jmeterFactory = injector.getInstance(JMeterFactory.class);
    this.jmeterService = injector.getInstance(JMeterScriptService.class);
    this.seleniumFactory = injector.getInstance(SeleniumUploadProjectFactory.class);
    this.caseRefFactory = injector.getInstance(Key.get(new TypeLiteral<ReferenceFactory<CaseReference>>(){}));
    this.customFactory = injector.getInstance(CustomKeywordFactory.class);
    this.customService = injector.getInstance(CustomKeywordService.class);
    this.authService = injector.getInstance(Key.get(new TypeLiteral<AuthenticationService<User>>(){}));
    this.context = injector.getInstance(OrganizationContext.class);
    initService();
  }
  
  @BeforeMethod
  public void setup() throws Exception {
    
    this.mongoService.dropDatabase();
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
  
  @Test 
  public void cloneProject() throws FileNotFoundException, IOException {
    this.authService.logIn("haint@cloud-ats.net", "12345");
    this.spaceService.goTo(spaceRefFactory.create(this.space.getId()));
    //create performance project
    PerformanceProject performance = performanceFactory.create("performance", "");
    performanceProjectService.create(performance);
    
    //create keyword project
    KeywordProject keyword = keywordFactory.create(context, "keyword", "");
    keywordProjectService.create(keyword);
    
    //create selenium project
    SeleniumUploadProject selenium = seleniumFactory.create(context, "selenium", "");
    seleniumService.create(selenium);
    
    //create mix project
    MixProject mp = mpFactory.create(UUID.randomUUID().toString(), "mix_project", keyword.getId(), performance.getId(), selenium.getId(), context.getUser().getEmail());
    mpService.create(mp);
    
    //create upload jmx script file
    String jmeterContent = StringUtil.readStream(new FileInputStream("src/test/resources/test.jmx"));
    JMeterScript script = jmeterFactory.createRawJmeterScript(performance.getId(), "Test Script", "haint@cloudats.net", jmeterContent);
    script.setLoops(2);
    script.setNumberEngines(2);
    script.setRamUp(5);
    script.setNumberThreads(200);
    jmeterService.create(script);
    
    //create jmeter script by wizard
    JMeterSampler loginPost = jmeterFactory.createHttpPost("Login", "http://localhost:9000/signin", null, 0,
        jmeterFactory.createArgument("email", "root@system.com"),
        jmeterFactory.createArgument("password", "admin"));
    
    JMeterScript jmeter = jmeterFactory.createJmeterScript(
        "Test Name",
        1, 100, 5, false, 0,performance.getId(), "haint@cloudats.net",
        loginPost);
    jmeter.setNumberEngines(4);
    JMeterScript newJMeter = jmeterFactory.createJmeterScript(
        "New Script",
        1, 100, 5, false, 0, performance.getId(), "haint@cloudats.net",
        loginPost);
    
    newJMeter.setNumberEngines(5);
    jmeterService.create(jmeter, newJMeter);
    
    //create test case 1 for keyword project
    ObjectMapper m = new ObjectMapper();
    JsonNode rootNode = m.readTree(new File("src/test/resources/full_example.json"));
    
    JsonNode stepsNode = rootNode.get("steps");
    List<CaseReference> cases = new ArrayList<CaseReference>();
    
    Case caze = caseFactory.create(keyword.getId(), "testcase", null, "haint@cloudats.net");
    
    for (JsonNode json : stepsNode) {
      caze.addAction(json);
    }
    caseService.create(caze);
    cases.add(caseRefFactory.create(caze.getId()));
    
    //test case 2
    caze = caseFactory.create(keyword.getId(), "testcase2", null, "haint@cloudats.net");
    caseService.create(caze);
    cases.add(caseRefFactory.create(caze.getId()));
    
    Suite suite = suiteFactory.create(keyword.getId(), "FullExamle", SuiteFactory.DEFAULT_INIT_DRIVER, cases, context.getUser().getEmail());
    suiteService.create(suite);
    
    //custom keyword
    CustomKeyword customKeyword = customFactory.create(keyword.getId(), "custom_keyword");
    for (JsonNode json : stepsNode) {
      customKeyword.addAction(json);
    }
    customService.create(customKeyword);
    
    //clone project
    MixProject clone_project = mpService.cloneData(mp.getId(), "mix_project_clone");

    //assert result
    PerformanceProject newPers = performanceProjectService.get(clone_project.getPerformanceId());
    Assert.assertEquals(newPers.getName(), "mix_project_clone");
    
    PageList<JMeterScript> scripts = jmeterService.query(new BasicDBObject("project_id", clone_project.getPerformanceId()));
    Assert.assertEquals(scripts.count(), 3);
    
    KeywordProject newKey = keywordProjectService.get(clone_project.getKeywordId());
    Assert.assertEquals(newKey.getString("name"), "mix_project_clone");
    
    SeleniumUploadProject newSelenium = seleniumService.get(clone_project.getSeleniumId());
    Assert.assertEquals(newSelenium.getString("name"), "mix_project_clone");
    
    PageList<Suite> suites = suiteService.query(new BasicDBObject("project_id", clone_project.getKeywordId()));
    Assert.assertEquals(suites.count(), 1);
    
    PageList<Case> listCase = caseService.query(new BasicDBObject("project_id", clone_project.getKeywordId()));
    Assert.assertEquals(listCase.count(), 2);
    
    PageList<CustomKeyword> listCustomsKey = customService.query(new BasicDBObject("project_id", clone_project.getKeywordId()));
    Assert.assertEquals(listCustomsKey.count(), 1);
    
    
    
  }
}
