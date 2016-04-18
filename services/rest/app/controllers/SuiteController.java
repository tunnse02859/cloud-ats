/**
 * 
 */
package controllers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.ats.common.PageList;
import org.ats.services.keyword.Case;
import org.ats.services.keyword.CaseReference;
import org.ats.services.keyword.Suite;
import org.ats.services.keyword.SuiteFactory;
import org.ats.services.keyword.SuiteService;
import org.ats.services.organization.acl.Authenticated;
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
  
  public Result list(String projectId) {
    
    MixProject mp = mpService.get(projectId);
    
    PageList<Suite> list = suiteService.getSuites(mp.getKeywordId());
    //list.setSortable(new MapBuilder<String, Boolean>("created_date", false).build());
    
    ArrayNode array = Json.newObject().arrayNode();
    while(list.hasNext()) {
      for (Suite suite : list.next()) {
        array.add(Json.parse(suite.toString()));
      }
    }
    return ok(array);
  }

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
    
    Suite suite = suiteFactory.create(projectId, suiteName, SuiteFactory.DEFAULT_INIT_DRIVER, list);
    suiteService.create(suite);
    suite.setMode(sequenceMode);
    suiteService.update(suite);
    
    return status(201, Json.parse(suite.toString()));
  }
  
  public Result update(String projectId) throws Exception {
    
    MixProject mp = mpService.get(projectId);
    
    JsonNode node = request().body().asJson();
    BasicDBObject obj = Json.fromJson(node, BasicDBObject.class);
    Suite suite = suiteService.transform(obj);
    Suite oldSuite = suiteService.get(suite.getId());
    if (!mp.getKeywordId().equals(suite.getProjectId()) 
        || !suite.getId().equals(oldSuite.getId())
        || oldSuite == null) return status(400);
    
    if (suite.equals(oldSuite)) return status(204);
    
    if (!suite.getName().equals(oldSuite.getName())) {
      
      PageList<Suite> listSuites = suiteService.getSuites(projectId);
      while(listSuites.hasNext()) {
        for(Suite suiteElement : listSuites.next()) {
          String oldSuiteName = suiteElement.getName();
          if (oldSuiteName.equals(suite.getName())) {
            return status(304);
          }
        }
      }
    }
    
    suiteService.update(suite);
    
    return status(200);
  }
  
  public Result delete(String projectId, String suiteId)  throws Exception {
    Suite suite = suiteService.get(suiteId);
    
    MixProject mp = mpService.get(projectId);
    if (suite == null || !mp.getKeywordId().equals(suite.getProjectId())) return status(404);
    
    suiteService.delete(suiteId);
    return status(200);
  }
  
  public Result cloneSuite(String projectId, String caseId) {
    
    String name = request().getQueryString("name");
    
    Suite suite = suiteService.get(caseId);
    suite.put("name", name);
    suite.put("_id", UUID.randomUUID().toString());
    suite.put("created_date", new Date());
    
    suiteService.create(suite);
    suite.put("created_date", suite.getDate("created_date").getTime());
    
    return ok(Json.parse(suite.toString()));
  }

  public Result get(String projectId, String suiteId) {
    
    ObjectNode object = Json.newObject();
    
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
    object.put("cases", Json.parse(array.toString()));
    object.put("_id", suiteId);
    object.put("name", suite.getName());
    object.put("init_driver", suite.getString("init_driver"));
    object.put("project_id", suite.getProjectId());
    
    return ok(object);
  }
}
