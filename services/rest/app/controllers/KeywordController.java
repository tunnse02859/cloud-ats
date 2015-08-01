/**
 * 
 */
package controllers;

import org.ats.common.PageList;
import org.ats.services.OrganizationContext;
import org.ats.services.executor.ExecutorService;
import org.ats.services.keyword.CaseFactory;
import org.ats.services.keyword.CaseReference;
import org.ats.services.keyword.CaseService;
import org.ats.services.keyword.KeywordProject;
import org.ats.services.keyword.KeywordProjectFactory;
import org.ats.services.keyword.KeywordProjectService;
import org.ats.services.keyword.SuiteReference;
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
  
  public Result list() {
    PageList<KeywordProject> list = keywordProjectService.list();
    ArrayNode array = Json.newObject().arrayNode();
    
    while(list.hasNext()) {
      for (KeywordProject project : list.next()) {
        project.put("type", "keyword");
        project.put("totalSuites", suiteService.getSuites(project.getId()).count());
        project.put("totalCases", caseService.getCases(project.getId()).count());
        array.add(Json.parse(project.toString()));
      }
    }
    return ok(array);
  }
  
  public Result get(String projectId) {
    KeywordProject project = keywordProjectService.get(projectId);
    if (project == null) return status(404);
    
    project.put("type", "keyword");
    project.put("totalSuites", suiteService.getSuites(project.getId()).count());
    project.put("totalCases", caseService.getCases(project.getId()).count());
    return ok(Json.parse(project.toString()));
  }

  public Result create() {
    JsonNode json = request().body().asJson();
    String name = json.get("name").asText();
    
    KeywordProject project = keywordProjectFactory.create(context, name);
    keywordProjectService.create(project);

    return ok(Json.parse(project.toString()));
  }

}
