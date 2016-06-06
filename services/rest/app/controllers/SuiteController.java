/**
 * 
 */
package controllers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.ats.common.MapBuilder;
import org.ats.common.PageList;
import org.ats.services.OrganizationContext;
import org.ats.services.keyword.Case;
import org.ats.services.keyword.CaseReference;
import org.ats.services.keyword.Suite;
import org.ats.services.keyword.SuiteFactory;
import org.ats.services.keyword.SuiteService;
import org.ats.services.organization.UserService;
import org.ats.services.organization.acl.Authenticated;
import org.ats.services.organization.acl.Authorized;
import org.ats.services.organization.entity.User;
import org.ats.services.organization.entity.fatory.ReferenceFactory;
import org.ats.services.project.MixProject;
import org.ats.services.project.MixProjectService;

import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import actions.CorsComposition;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Aug 4, 2015
 */
@CorsComposition.Cors
@Authenticated
public class SuiteController extends Controller {
  
  @Inject SuiteService suiteService;
  
  @Inject SuiteFactory suiteFactory;
  
  @Inject ReferenceFactory<CaseReference> caseRefFactory;
  
  @Inject MixProjectService mpService;
  
  @Inject OrganizationContext context;
  
  @Inject UserService userService;
  
  @Authorized(feature="project", action="view_functional")
  public Result list(String projectId) {
    MixProject mp = mpService.get(projectId);
    mp.put("created_date", mp.getDate("created_date").getTime());
    
    PageList<Suite> list = suiteService.getSuites(mp.getKeywordId());
    list.setSortable(new MapBuilder<String, Boolean>("created_date", false).build());
    
    BasicDBList array = new BasicDBList();
    while(list.hasNext()) {
      for (Suite suite : list.next()) {
        String email = suite.getCreator();
        User user = userService.get(email);
        BasicDBObject userObj = new BasicDBObject("email", email).append("first_name", user.getFirstName()).append("last_name", user.getLastName());
        suite.put("creator", userObj);
        suite.put("created_date", suite.getCreatedDate().getTime());
        array.add(suite);
      }
    }
    mp.put("suites", array);
    return ok(Json.parse(mp.toString()));
  }
  
  @Authorized(feature="project", action="manage_functional")
  public Result create(String projectId) {
    
    MixProject mp = mpService.get(projectId);
    projectId = mp.getKeywordId();
    
    JsonNode data = request().body().asJson();
    
    String suiteName = data.get("name").asText();
    boolean sequenceMode = data.get("sequence_mode").asBoolean();
    JsonNode cases = data.get("cases");
    
    PageList<Suite> listSuites = suiteService.getSuites(projectId);
    while(listSuites.hasNext()) {
      for(Suite suite : listSuites.next()) {
        if (suiteName.equals(suite.getName())) {
          return status(304);
        }
      }
    }
    
    List<CaseReference> list = new ArrayList<CaseReference>();
    for (JsonNode testCase : cases) {
      list.add(caseRefFactory.create(testCase.get("_id").asText()));
    }
    
    Suite suite = suiteFactory.create(projectId, suiteName, SuiteFactory.DEFAULT_INIT_DRIVER, list, context.getUser().getEmail());
    suiteService.create(suite);
    suite.setMode(sequenceMode);
    suiteService.update(suite);
    
    return status(201, Json.parse(suite.toString()));
  }
  
  @Authorized(feature="project", action="manage_functional")
  public Result update(String projectId) throws Exception {

    JsonNode node = request().body().asJson();
    Suite suite = suiteService.get(node.get("_id").asText());
    if (suite == null) return status(404);
    else {
      String name = node.get("name").asText();
      boolean mode = node.get("sequence_mode").asBoolean();
      if (suite.getCreator() == null) suite.put("creator", context.getUser().getEmail());
      
      //Update only suite info
      if (node.get("cases") == null) {
        if (suite.getName().equals(name) && suite.getMode() == mode) {
          return status(204);
        } else {
          suite.put("name", name);
          suite.put("sequence_mode", mode);
          suiteService.update(suite);
          
          return status(200);
        }
      }
      
//      suite.put("name", name);
//      suite.put("sequence_mode", mode);
      
      BasicDBList list = new BasicDBList();
      ArrayNode cases = (ArrayNode) node.get("cases");
      for (JsonNode caze : cases) {
        list.add(new BasicDBObject("_id", caze.get("_id").asText()));
      }
      suite.put("cases", list);
      suiteService.update(suite);
      
      return status(200);
    }
  }
  
  @Authorized(feature="project", action="manage_functional")
  public Result delete(String projectId, String suiteId)  throws Exception {
    Suite suite = suiteService.get(suiteId);
    
    MixProject mp = mpService.get(projectId);
    if (suite == null || !mp.getKeywordId().equals(suite.getProjectId())) return status(404);
    
    suiteService.delete(suiteId);
    return status(200);
  }
  
  @Authorized(feature="project", action="manage_functional")
  public Result cloneSuite(String projectId, String caseId) {
    
    String name = request().getQueryString("name");
    
    Suite suite = suiteService.get(caseId);
    suite.put("name", name);
    suite.put("_id", UUID.randomUUID().toString());
    suite.put("created_date", new Date());
    
    suiteService.create(suite);
    suite.put("created_date", suite.getDate("created_date").getTime());
    
    String email = suite.getCreator();
    User user = userService.get(email);
    BasicDBObject userObj = new BasicDBObject("email", email).append("first_name", user.getFirstName()).append("last_name", user.getLastName());
    suite.put("creator", userObj);
    
    return ok(Json.parse(suite.toString()));
  }

  public Result get(String projectId, String suiteId) {
    
    ObjectNode object = Json.newObject();
    
    MixProject project = mpService.get(projectId);
    Suite suite = suiteService.get(suiteId);
    List<CaseReference> caseRef = suite.getCases();
    ArrayNode array = Json.newObject().arrayNode();
    for (CaseReference ref : caseRef){
      ObjectNode obj = Json.newObject();
      Case caze = ref.get();
      
      obj.put("_id", caze.getId());
      obj.put("name", caze.getName());
      
      array.add(obj);
    }
    object.put("cases", array);
    object.put("_id", suiteId);
    object.put("name", suite.getName());
    object.put("sequence_mode", suite.getMode());
//    object.put("init_driver", suite.getString("init_driver"));
    object.put("project", Json.newObject().put("_id", projectId).put("name",  project.getName()));
    
    return ok(object);
  }
}
