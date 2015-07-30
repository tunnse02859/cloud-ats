/**
 * 
 */
package controllers;

import java.io.File;
import java.util.Arrays;

import org.ats.services.OrganizationContext;
import org.ats.services.executor.ExecutorService;
import org.ats.services.executor.job.KeywordJob;
import org.ats.services.keyword.Case;
import org.ats.services.keyword.CaseFactory;
import org.ats.services.keyword.CaseReference;
import org.ats.services.keyword.CaseService;
import org.ats.services.keyword.KeywordProject;
import org.ats.services.keyword.KeywordProjectFactory;
import org.ats.services.keyword.KeywordProjectService;
import org.ats.services.keyword.Suite;
import org.ats.services.keyword.Suite.SuiteBuilder;
import org.ats.services.keyword.SuiteReference;
import org.ats.services.keyword.SuiteService;
import org.ats.services.organization.acl.Authenticated;
import org.ats.services.organization.entity.fatory.ReferenceFactory;

import play.mvc.Controller;
import play.mvc.Result;
import actions.CorsComposition;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Jul 28, 2015
 */
@CorsComposition.Cors
@Authenticated
public class KeywordController extends Controller {

  @Inject CaseService caseService;
  
  @Inject CaseFactory caseFactory;
  
  @Inject ReferenceFactory<CaseReference> caseRefFactory;
  
  @Inject SuiteService suiteService;
  
  @Inject KeywordProjectFactory keywordProjectFactory;
  
  @Inject OrganizationContext context;
  
  @Inject ReferenceFactory<SuiteReference> suiteRefFactory;
  
  @Inject KeywordProjectService keywordProjectService;
  
  @Inject ExecutorService executorService;

  public Result build() throws Exception {
    
    KeywordProject project = keywordProjectFactory.create(context, "Full Example");
    
    ObjectMapper m = new ObjectMapper();
    JsonNode rootNode = m.readTree(new File("conf/dump/full_example.json"));
    
    SuiteBuilder builder = new SuiteBuilder();
    builder.packageName("org.ats.generated")
      .suiteName("FullExample")
      .driverVar(SuiteBuilder.DEFAULT_DRIVER_VAR)
      .initDriver(SuiteBuilder.DEFAULT_INIT_DRIVER)
      .timeoutSeconds(SuiteBuilder.DEFAULT_TIMEOUT_SECONDS)
      .raw(null).projectId(project.getId());
    
    JsonNode stepsNode = rootNode.get("steps");
    Case caze = caseFactory.create(project.getId(), "test", null, "info");
    for (JsonNode json : stepsNode) {
      caze.addAction(json);
    }
    caseService.create(caze);
    builder.addCases(caseRefFactory.create(caze.getId()));

    Suite fullExampleSuite= builder.build();
    suiteService.create(fullExampleSuite);
    
    rootNode = m.readTree(new File("conf/dump/acceptAlert.json"));
    
    builder = new SuiteBuilder();
    builder.packageName("org.ats.generated")
      .suiteName("AcceptAlert")
      .driverVar(SuiteBuilder.DEFAULT_DRIVER_VAR)
      .initDriver(SuiteBuilder.DEFAULT_INIT_DRIVER)
      .timeoutSeconds(SuiteBuilder.DEFAULT_TIMEOUT_SECONDS)
      .raw(null).projectId(project.getId());
    
    stepsNode = rootNode.get("steps");
    caze = caseFactory.create(project.getId(), "test", null, "info");
    for (JsonNode json : stepsNode) {
      caze.addAction(json);
    }
    caseService.create(caze);
    builder.addCases(caseRefFactory.create(caze.getId()));
    
    Suite acceptAlertSuite = builder.build();
    suiteService.create(acceptAlertSuite);
    
    project.addSuite(suiteRefFactory.create(fullExampleSuite.getId()));
    project.addSuite(suiteRefFactory.create(acceptAlertSuite.getId()));
    
    keywordProjectService.create(project);
    
    KeywordJob job = executorService.execute(project, Arrays.asList(
        suiteRefFactory.create(fullExampleSuite.getId()),
        suiteRefFactory.create(acceptAlertSuite.getId())));
    
    return ok(job.getId());
  }
}
