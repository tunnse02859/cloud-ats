/**
 * TrinhTV3@fsoft.com.vn
 */
package controllers;

import java.util.UUID;

import org.ats.common.PageList;
import org.ats.services.OrganizationContext;
import org.ats.services.keyword.KeywordProject;
import org.ats.services.keyword.KeywordProjectFactory;
import org.ats.services.keyword.KeywordProjectService;
import org.ats.services.organization.acl.Authenticated;
import org.ats.services.organization.entity.User;
import org.ats.services.performance.PerformanceProject;
import org.ats.services.performance.PerformanceProjectFactory;
import org.ats.services.performance.PerformanceProjectService;
import org.ats.services.project.MixProject;
import org.ats.services.project.MixProjectFactory;
import org.ats.services.project.MixProjectService;
import org.ats.services.upload.SeleniumUploadProject;
import org.ats.services.upload.SeleniumUploadProjectFactory;
import org.ats.services.upload.SeleniumUploadProjectService;

import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import actions.CorsComposition;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.inject.Inject;

/**
 * @author TrinhTV3
 *
 */

@CorsComposition.Cors
@Authenticated
public class MixProjectController extends Controller {
  
  @Inject MixProjectService mpService;
  
  @Inject PerformanceProjectService performanceService;
  
  @Inject KeywordProjectService keywordService;
  
  @Inject SeleniumUploadProjectService seleniumService;
  
  @Inject MixProjectFactory mixProjectFactory;
  
  @Inject PerformanceProjectFactory performanceFactory;
  
  @Inject KeywordProjectFactory keywordFactory;
  
  @Inject SeleniumUploadProjectFactory seleniumFactory;
  
  @Inject OrganizationContext context;
  
  public Result list() {
    
    ArrayNode array = Json.newObject().arrayNode();
    
    PageList<MixProject> list = mpService.list();
    while (list.hasNext()) {
      for (MixProject project : list.next()) {
        project.put("created_date", project.getDate("created_date").getTime());
        array.add(Json.parse(project.toString()));
      }
    }
    return ok(array);
    
  }
  
  public Result create() {
    
    JsonNode json = request().body().asJson();
    String creator = context.getUser().getEmail(); 
    String name = json.get("name").asText();
    
    PerformanceProject performance = performanceFactory.create(name);
    performanceService.create(performance);
    
    KeywordProject keyword = keywordFactory.create(context, name);
    keywordService.create(keyword);
    
    SeleniumUploadProject selenium = seleniumFactory.create(context, name);
    seleniumService.create(selenium);
    
    MixProject mp = mixProjectFactory.create(UUID.randomUUID().toString(), name, keyword.getId(), performance.getId(), selenium.getId(), creator);
    mpService.create(mp);
    
    return ok(Json.parse(mp.toString()));
  }
  
  public Result delete(String id, String name, String password) {
    
    MixProject mp = mpService.get(id);
    User user = context.getUser();
    
    if (!name.equals(mp.getName()) || !password.equals(user.getPassword())) {
      return status(403);
    }
    
    keywordService.delete(mp.getKeywordId());
    seleniumService.delete(mp.getSeleniumId());
    performanceService.delete(mp.getPerformanceId());
    mpService.delete(id);
    
    return ok();
  }
  
}
