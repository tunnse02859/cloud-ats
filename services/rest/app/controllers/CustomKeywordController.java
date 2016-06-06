/**
 * 
 */
package controllers;

import org.ats.common.MapBuilder;
import org.ats.common.PageList;
import org.ats.services.OrganizationContext;
import org.ats.services.keyword.Case;
import org.ats.services.keyword.CustomKeyword;
import org.ats.services.keyword.CustomKeywordFactory;
import org.ats.services.keyword.CustomKeywordService;
import org.ats.services.organization.UserService;
import org.ats.services.organization.acl.Authenticated;
import org.ats.services.organization.acl.Authorized;
import org.ats.services.organization.entity.User;
import org.ats.services.project.MixProject;
import org.ats.services.project.MixProjectService;

import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import actions.CorsComposition;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.inject.Inject;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.util.JSON;

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
  
  @Inject MixProjectService mpService;
  
  @Inject UserService userService;
  
  @Inject OrganizationContext context;
  public Result list(String projectId) {
    
    MixProject mp = mpService.get(projectId);

    PageList<CustomKeyword> list = customService.getCustomKeywords(mp.getKeywordId());
    list.setSortable(new MapBuilder<String, Boolean>("created_date", false).build());
    
    BasicDBList array = new BasicDBList();
    while(list.hasNext()) {
      for (CustomKeyword custom : list.next()) {
        String email = custom.getCreator();
        User user = userService.get(email);
        BasicDBObject userObj = new BasicDBObject("email", email).append("first_name", user.getFirstName()).append("last_name", user.getLastName());
        custom.put("creator", userObj);
        custom.put("created_date", custom.getDate("created_date").getTime());
        array.add(custom);
      }
    }
    mp.put("customs", array);
    return ok(Json.parse(mp.toString()));
  }
  
  @Authorized(feature="project", action="manage_functional")
  public Result create(String projectId) {
    
    MixProject mp = mpService.get(projectId);
    
    JsonNode data = request().body().asJson();
    String name = data.get("name").asText();
    CustomKeyword keyword = customFactory.create(mp.getKeywordId(), name, context.getUser().getEmail());
    for (JsonNode step : data.get("steps")) {
      keyword.addAction(step);
    }
    customService.create(keyword);
    return status(201, Json.parse(keyword.toString()));
  }
  
  @Authorized(feature="project", action="manage_functional")
  public Result update(String projectId) {

    JsonNode node = request().body().asJson();
    CustomKeyword custom = customService.get(node.get("_id").asText());
    if (custom == null) return status(404);
    else {
      String name = node.get("name").asText();
      if (custom.getCreator() == null) custom.put("creator", context.getUser().getEmail());
      
      //Update only group keyword info
      if (node.get("steps") == null) {
        if (custom.getName().equals(name)) return status(204);
        else {
          custom.put("name", name);
          customService.update(custom);
          return status(200);
        }
      }
      
      ArrayNode steps = (ArrayNode) node.get("steps");
      BasicDBList list = new BasicDBList();
      for (JsonNode step : steps) {
        list.add(JSON.parse(step.toString()));
      }
      custom.put("steps",  list);
      customService.update(custom);
      
      return status(200);
    }
  }
  
  @Authorized(feature="project", action="manage_functional")
  public Result delete(String projectId, String keywordId)  throws Exception {
    
    MixProject mp = mpService.get(projectId);
    
    CustomKeyword keyword = customService.get(keywordId);
    if (keyword == null || !mp.getKeywordId().equals(keyword.getProjectId())) return status(404);
    
    customService.delete(keywordId);
    return status(200);
  }
  
  public Result get(String projectId, String customId) {
    
    CustomKeyword custom = customService.get(customId);
    MixProject project = mpService.get(projectId);
    custom.put("project", project.getName());
    return ok(Json.parse(custom.toString()));
  }
  
  @Authorized(feature="project", action="manage_functional")
  public Result rename(String projectId) {
    
    JsonNode json = request().body().asJson();
    String customId = json.get("_id").asText();
    String customName = json.get("name").asText();
    
    CustomKeyword custom = customService.get(customId);
    if (customName.equals(custom.getName())) return status(204);
    custom.setName(customName);
    
    customService.update(custom);
    
    return ok();
    
  }
  
}
