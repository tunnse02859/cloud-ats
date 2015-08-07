/**
 * 
 */
package controllers;

import org.ats.common.MapBuilder;
import org.ats.common.PageList;
import org.ats.services.keyword.CustomKeyword;
import org.ats.services.keyword.CustomKeywordFactory;
import org.ats.services.keyword.CustomKeywordService;
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
 * Aug 6, 2015
 */
@CorsComposition.Cors
@Authenticated
public class CustomKeywordController extends Controller {

  @Inject CustomKeywordService customService;
  
  @Inject CustomKeywordFactory customFactory;
  
  public Result list(String projectId) {
    PageList<CustomKeyword> list = customService.getCustomKeywords(projectId);
    list.setSortable(new MapBuilder<String, Boolean>("created_date", false).build());
    
    ArrayNode array = Json.newObject().arrayNode();
    while(list.hasNext()) {
      for (CustomKeyword caze : list.next()) {
        array.add(Json.parse(caze.toString()));
      }
    }
    return ok(array);
  }
  
  public Result create(String projectId) {
    JsonNode data = request().body().asJson();
    String name = data.get("name").asText();
    
    CustomKeyword keyword = customFactory.create(projectId, name);
    for (JsonNode step : data.get("steps")) {
      keyword.addAction(step);
    }
    customService.create(keyword);
    return status(201, Json.parse(keyword.toString()));
  }
  
  public Result update(String projectId) {
    JsonNode node = request().body().asJson();
    BasicDBObject obj = Json.fromJson(node, BasicDBObject.class);
    
    CustomKeyword keyword = customService.transform(obj);
    CustomKeyword oldKeyword = customService.get(keyword.getId());
    
    if (!projectId.equals(keyword.getProjectId()) 
        || !keyword.getId().equals(oldKeyword.getId())
        || oldKeyword == null) return status(400);
    
    if (keyword.equals(oldKeyword)) return status(204);
    
    customService.update(keyword);
    return status(200);
  }
  
  public Result delete(String projectId, String keywordId)  throws Exception {
    CustomKeyword keyword = customService.get(keywordId);
    if (keyword == null || !projectId.equals(keyword.getProjectId())) return status(404);
    
    customService.delete(keywordId);
    return status(200);
  }
}
