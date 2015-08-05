/**
 * 
 */
package controllers;

import org.ats.common.MapBuilder;
import org.ats.common.PageList;
import org.ats.services.keyword.Case;
import org.ats.services.keyword.CaseFactory;
import org.ats.services.keyword.CaseService;
import org.ats.services.organization.acl.Authenticated;

import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import actions.CorsComposition;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.inject.Inject;
import com.mongodb.BasicDBObject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Aug 1, 2015
 */
@CorsComposition.Cors
@Authenticated
public class CaseController extends Controller {

  @Inject CaseService caseService;
  
  @Inject CaseFactory caseFactory;
  
  public Result list(String projectId) {
    
    PageList<Case> list = caseService.getCases(projectId);
    list.setSortable(new MapBuilder<String, Boolean>("created_date", false).build());
    
    ArrayNode array = Json.newObject().arrayNode();
    while(list.hasNext()) {
      for (Case caze : list.next()) {
        array.add(Json.parse(caze.toString()));
      }
    }
    return ok(array);
  }
  
  public Result references(String projectId) {
    PageList<Case> list = caseService.getCases(projectId);
    ArrayNode array = Json.newObject().arrayNode();
    while(list.hasNext()) {
      for (Case caze : list.next()) {
        array.add(Json.newObject().put("_id", caze.getId()).put("name", caze.getName()));
      }
    }
    return ok(array);
  }
  
  public Result create(String projectId) {
    JsonNode node = request().body().asJson();
    String caseName = node.get("name").asText();
    Case caze = caseFactory.create(projectId, caseName, null);
    for(JsonNode action:node.get("steps")) {
      caze.addAction(action); 
    }
    caseService.create(caze);
    return status(201, Json.parse(caze.toString()));
  }
  
  public Result update(String projectId) throws Exception {
    JsonNode node = request().body().asJson();
    BasicDBObject obj = Json.fromJson(node, BasicDBObject.class);
    
    Case caze = caseService.transform(obj);
    Case oldCase = caseService.get(caze.getId());
    
    if (!projectId.equals(caze.getProjectId()) 
        || !caze.getId().equals(oldCase.getId())
        || oldCase == null) return status(400);
    
    if (caze.equals(oldCase)) return status(204);
    
    caseService.update(caze);
    
    return status(200);
  }
}
