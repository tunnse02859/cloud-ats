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
}
