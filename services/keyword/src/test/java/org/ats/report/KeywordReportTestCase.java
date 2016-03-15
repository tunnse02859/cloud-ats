/**
 * TrinhTV3@fsoft.com.vn
 */
package org.ats.report;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
import org.ats.services.keyword.report.models.CaseReport;
import org.ats.services.keyword.report.models.CaseReportReference;
import org.ats.services.keyword.report.models.StepReport;
import org.ats.services.keyword.report.models.StepReportReference;
import org.ats.services.keyword.report.models.SuiteReport;
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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
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
  private ReferenceFactory<CaseReportReference> caseReportRefFactory;
  private ReferenceFactory<StepReportReference> stepReportRefFactory;
  
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
    this.keywordReportService = injector.getInstance(KeywordReportService.class);
    this.suiteService = injector.getInstance(SuiteService.class);
    this.suiteFactory = injector.getInstance(SuiteFactory.class);
    
    this.caseService = injector.getInstance(CaseService.class);
    this.caseFactory = injector.getInstance(CaseFactory.class);
    this.caseRefFactory = injector.getInstance(Key.get(new TypeLiteral<ReferenceFactory<CaseReference>>(){}));
    this.caseReportRefFactory = injector.getInstance(Key.get(new TypeLiteral<ReferenceFactory<CaseReportReference>>(){}));
    this.stepReportRefFactory = injector.getInstance(Key.get(new TypeLiteral<ReferenceFactory<StepReportReference>>(){}));
    
    
    //start event service
    this.eventService = injector.getInstance(EventService.class);
    this.eventService.setInjector(injector);
    this.eventService.start();
    
    initService();
  }
  
  @AfterClass
  public void shutdown() throws Exception {
   // this.mongoService.dropDatabase();
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
    //this.mongoService.dropDatabase();
  }
  
  @Test
  public void testLogParser() throws IOException {
    
    BufferedReader br = null;
    try {
      String currentLine;
      br = new BufferedReader(new java.io.FileReader("src/test/resources/log_structure.txt"));
      SuiteReport suiteReport = null;
      CaseReport caseReport = null;
      List<SuiteReport> suites = new ArrayList<SuiteReport>();
      List<CaseReport> cases = new ArrayList<CaseReport>();
      List<StepReport> steps = new ArrayList<StepReport>();
      List<CaseReportReference> listCaseReportRef = null;
      String id = null;
      List<StepReportReference> listStepReportRef = null;
      List<String> dataSource = null;
      while ((currentLine = br.readLine()) != null) {
        ObjectMapper mapper = new ObjectMapper();
        if (currentLine.contains("[Start][Suite]")) {
          listCaseReportRef = new ArrayList<CaseReportReference>();
          int start = currentLine.indexOf("{");
          int end = currentLine.lastIndexOf("}");
          String obj = currentLine.substring(start, end + 1);
          JsonNode json = mapper.readTree(obj);
          String suiteName = json.get("name").asText();
          String jobId = json.get("jobId").asText();
          suiteReport = new SuiteReport(jobId, suiteName, 0, 0, 0, listCaseReportRef);
        }
        if (currentLine.contains("[Start][Case]")) {
          dataSource = new ArrayList<String>();
          listStepReportRef = new ArrayList<StepReportReference>();
        }
        if (currentLine.contains("[End][Case]")) {
          int start = currentLine.indexOf("{");
          int end = currentLine.lastIndexOf("}");
          String obj = currentLine.substring(start, end + 1);
          JsonNode json = mapper.readTree(obj);
          id = json.get("id").toString();
          caseReport = new CaseReport(suiteReport.getId(), "test", json.get("name").asText(), json.get("id").asText(), listStepReportRef);
          caseReport.setDataSource(dataSource.toString());
          
          cases.add(caseReport);
          CaseReportReference ref = caseReportRefFactory.create(caseReport.getId());
          listCaseReportRef.add(ref);
          caseReport.setSteps(listStepReportRef);
        }
        
        if (currentLine.contains("[Start][Data]")) {
          int start = currentLine.indexOf("{");
          int end = currentLine.lastIndexOf("}");
          String obj = currentLine.substring(start, end + 1);
          dataSource.add(obj);
        }
        if (currentLine.contains("[Start][Step]")) {
          int start = currentLine.indexOf("{");
          int end = currentLine.lastIndexOf("}");
          String obj = currentLine.substring(start, end + 1);
          JsonNode json = mapper.readTree(obj);
          String name = json.get("name").toString();
          StepReport step = new StepReport(name);
          JsonNode params = json.get("params");
          BasicDBList list = new BasicDBList();
          for (JsonNode j : params) {
            BasicDBObject object = new BasicDBObject();
            String value = json.get(j.asText()).asText();
            object.put(j.asText(), value);
            list.add(object);
          }
          
          step.put("params", list);
          StepReportReference ref = stepReportRefFactory.create(step.getId());
          listStepReportRef.add(ref);
          steps.add(step);
        }
        
        if (currentLine.contains("[End][Data]")) {
          
        }
        if (currentLine.contains("[End][Suite]")) {
          suiteReport.setCases(listCaseReportRef);
          suites.add(suiteReport);
        }
      }
      
      keywordReportService.createSuitesReport(suites);
      keywordReportService.createCasesReport(cases);
      keywordReportService.createStepsReport(steps);
    } catch (IOException e) {
      e.printStackTrace();
    } 
  }
  
  
  
 

}
