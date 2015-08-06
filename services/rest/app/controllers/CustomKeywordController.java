/**
 * 
 */
package controllers;

import org.ats.services.keyword.CustomKeyword;
import org.ats.services.keyword.CustomKeywordFactory;
import org.ats.services.keyword.CustomKeywordService;
import org.ats.services.organization.acl.Authenticated;

import actions.CorsComposition;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;

import play.mvc.Controller;
import play.mvc.Result;

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
  
  public Result create(String projectId) {
    JsonNode data = request().body().asJson();
    String name = data.get("name").asText();
    
    CustomKeyword keyword = customFactory.create(projectId, name);
    for (JsonNode step : data.get("steps")) {
      keyword.addAction(step);
    }
    customService.create(keyword);
    return status(201);
  }
}
