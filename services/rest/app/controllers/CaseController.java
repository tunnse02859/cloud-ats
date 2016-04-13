/**
 * 
 */
package controllers;

import java.util.Date;
import java.util.UUID;

import org.ats.common.MapBuilder;
import org.ats.common.PageList;
import org.ats.services.keyword.Case;
import org.ats.services.keyword.CaseFactory;
import org.ats.services.keyword.CaseService;
import org.ats.services.organization.acl.Authenticated;
import org.ats.services.project.MixProject;
import org.ats.services.project.MixProjectService;

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
  
  @Inject MixProjectService mpService;
  
  public Result list(String projectId) {
    
    MixProject mp = mpService.get(projectId);
    
    PageList<Case> list = caseService.getCases(mp.getKeywordId());
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
  
  public Result delete(String projectId, String caseId)  throws Exception {
    
    MixProject mp = mpService.get(projectId);
    
    Case caze = caseService.get(caseId);
    if (caze == null || !mp.getKeywordId().equals(caze.getProjectId())) return status(404);
    
    caseService.delete(caseId);
    return status(200);
  }
  
  public Result cloneCase(String projectId, String caseId) {
    
    String name = request().getQueryString("name");
    
    Case caze = caseService.get(caseId);
    caze.put("name", name);
    caze.put("_id", UUID.randomUUID().toString());
    caze.put("created_date", new Date());
    
    caseService.create(caze);
    caze.put("created_date", caze.getDate("created_date").getTime());
    
    return ok(Json.parse(caze.toString()));
  }
  
}
