/**
 * 
 */
package controllers;

import java.util.Date;
import java.util.UUID;

import org.ats.common.MapBuilder;
import org.ats.common.PageList;
import org.ats.services.OrganizationContext;
import org.ats.services.datadriven.DataDriven;
import org.ats.services.keyword.Case;
import org.ats.services.keyword.CaseFactory;
import org.ats.services.keyword.CaseService;
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
 * Aug 1, 2015
 */
@CorsComposition.Cors
@Authenticated
public class CaseController extends Controller {

  @Inject CaseService caseService;
  
  @Inject CaseFactory caseFactory;
  
  @Inject MixProjectService mpService;
  
  @Inject OrganizationContext context;
  
  @Inject UserService userService;
  
  @Authorized(feature="project", action="view_functional")
  public Result list(String projectId) {
    
    MixProject mp = mpService.get(projectId);

    PageList<Case> list = caseService.getCases(mp.getKeywordId());
    list.setSortable(new MapBuilder<String, Boolean>("created_date", false).build());
    
    BasicDBList array = new BasicDBList();
    while(list.hasNext()) {
      for (Case caze : list.next()) {
        String email = caze.getCreator();
        User user = userService.get(email);
        BasicDBObject userObj = new BasicDBObject("email", email).append("first_name", user.getFirstName()).append("last_name", user.getLastName());
        caze.put("creator", userObj);
        caze.put("created_date", caze.getCreatedDate().getTime());
        array.add(caze);
      }
    }
    mp.put("cases", array);
    return ok(Json.parse(mp.toString()));
  }
  
  public Result references(String projectId) {
    
    MixProject mp = mpService.get(projectId);
    PageList<Case> list = caseService.getCases(mp.getKeywordId());
    ArrayNode array = Json.newObject().arrayNode();
    while(list.hasNext()) {
      for (Case caze : list.next()) {
        array.add(Json.newObject().put("_id", caze.getId()).put("name", caze.getName()));
      }
    }
    return ok(array);
  }
  
  @Authorized(feature="project", action="manage_functional")
  public Result create(String projectId) {
    
    MixProject mp = mpService.get(projectId);
    
    JsonNode node = request().body().asJson();
    String caseName = node.get("name").asText();
    Case caze = caseFactory.create(mp.getKeywordId(), caseName, null, context.getUser().getEmail());
    for(JsonNode action:node.get("steps")) {
      caze.addAction(action); 
    }
    caseService.create(caze);
    return status(201, Json.parse(caze.toString()));
  }
  
  @Authorized(feature="project", action="manage_functional")
  public Result update(String projectId) throws Exception {
    
    JsonNode node = request().body().asJson();
    Case caze = caseService.get(node.get("_id").asText());
    if (caze == null) return status(404);
    else {
      String name = node.get("name").asText();
      if (caze.getCreator() == null) caze.put("creator", context.getUser().getEmail());
      
      //Update data driven
      if (node.get("data_driven") != null) {
        caze.put("data_driven",  new BasicDBObject("_id", node.get("data_driven").asText()));
      } else {
        caze.remove("data_driven");
      }
      
      //Update only case info
      if (node.get("steps") == null) {
        if (caze.getName().equals(name)) return status(204);
        else {
          caze.put("name", name);
          caseService.update(caze);
          return status(200);
        }
      }
      
      ArrayNode steps = (ArrayNode) node.get("steps");
      BasicDBList list = new BasicDBList();
      for (JsonNode step : steps) {
        list.add(JSON.parse(step.toString()));
      }
      caze.put("steps",  list);
      caseService.update(caze);
      
      return status(200);
    }
    
//    BasicDBObject obj = Json.fromJson(node, BasicDBObject.class);
//    
//    Case caze = caseService.transform(obj);
//    Case oldCase = caseService.get(caze.getId());
//    
//    if (!projectId.equals(caze.getProjectId()) 
//        || !caze.getId().equals(oldCase.getId())
//        || oldCase == null) return status(400);
//    
//    if (caze.equals(oldCase)) return status(204);
//    
//    caseService.update(caze);
    
//    return status(200);
  }
  
  @Authorized(feature="project", action="manage_functional")
  public Result delete(String projectId, String caseId)  throws Exception {
    
    MixProject mp = mpService.get(projectId);
    
    Case caze = caseService.get(caseId);
    if (caze == null || !mp.getKeywordId().equals(caze.getProjectId())) return status(404);
    
    caseService.delete(caseId);
    return status(200);
  }
  
  @Authorized(feature="project", action="manage_functional")
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
  
  public Result get(String projectId, String caseId) {
    
    Case caze = caseService.get(caseId);
    if (caze.getDataDriven() != null) {
      DataDriven data = caze.getDataDriven().get();
      if (data != null)
        caze.put("data_driven", new BasicDBObject("_id", data.getId()).append("name", data.getName()));
    }
    MixProject project = mpService.get(projectId);
    caze.put("project", project.getName());
    return ok(Json.parse(caze.toString()));
  }
  
  @Authorized(feature="project", action="manage_functional")
  public Result rename(String projectId) {
    
    JsonNode json = request().body().asJson();
    String caseId = json.get("_id").asText();
    String caseName = json.get("name").asText();
    Case caze = caseService.get(caseId);
    
    if (caseName.equals(caze.getName())) return status(204);
    caze.setName(caseName);
    caseService.update(caze);
    return ok();
    
  }
}
