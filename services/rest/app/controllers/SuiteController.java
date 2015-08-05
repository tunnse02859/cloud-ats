/**
 * 
 */
package controllers;

import org.ats.common.MapBuilder;
import org.ats.common.PageList;
import org.ats.services.keyword.CaseReference;
import org.ats.services.keyword.Suite;
import org.ats.services.keyword.Suite.SuiteBuilder;
import org.ats.services.keyword.SuiteService;
import org.ats.services.organization.acl.Authenticated;
import org.ats.services.organization.entity.fatory.ReferenceFactory;

import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import actions.CorsComposition;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.inject.Inject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Aug 4, 2015
 */
@CorsComposition.Cors
@Authenticated
public class SuiteController extends Controller {
  
  @Inject SuiteService suiteService;
  
  @Inject ReferenceFactory<CaseReference> caseRefFactory;
  
  public Result list(String projectId) {
    
    PageList<Suite> list = suiteService.getSuites(projectId);
    list.setSortable(new MapBuilder<String, Boolean>("created_date", false).build());
    
    ArrayNode array = Json.newObject().arrayNode();
    while(list.hasNext()) {
      for (Suite suite : list.next()) {
        array.add(Json.parse(suite.toString()));
      }
    }
    return ok(array);
  }

  public Result create(String projectId) {
    JsonNode data = request().body().asJson();
    
    String suiteName = data.get("name").asText();
    JsonNode cases = data.get("cases");
    
    SuiteBuilder builder = new SuiteBuilder();
    builder.packageName("org.ats.generated")
      .suiteName(suiteName)
      .driverVar(SuiteBuilder.DEFAULT_DRIVER_VAR)
      .initDriver(SuiteBuilder.DEFAULT_INIT_DRIVER)
      .timeoutSeconds(SuiteBuilder.DEFAULT_TIMEOUT_SECONDS)
      .raw(null).projectId(projectId);
    
    for (JsonNode testCase : cases) {
      builder.addCases(caseRefFactory.create(testCase.get("_id").asText()));
    }
    
    Suite suite = builder.build();
    suiteService.create(suite);
    
    return status(201, Json.parse(suite.toString()));
  }
}
