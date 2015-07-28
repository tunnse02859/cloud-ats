package controllers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.ats.common.MapBuilder;
import org.ats.common.PageList;
import org.ats.services.OrganizationContext;
import org.ats.services.datadriven.DataDrivenReference;
import org.ats.services.datadriven.DataDrivenService;
import org.ats.services.keyword.Case;
import org.ats.services.keyword.CaseFactory;
import org.ats.services.keyword.CaseReference;
import org.ats.services.keyword.CaseService;
import org.ats.services.keyword.CustomKeyword;
import org.ats.services.keyword.KeywordProject;
import org.ats.services.keyword.KeywordProjectFactory;
import org.ats.services.keyword.KeywordProjectService;
import org.ats.services.keyword.Suite;
import org.ats.services.keyword.Suite.SuiteBuilder;
import org.ats.services.keyword.SuiteReference;
import org.ats.services.keyword.SuiteService;
import org.ats.services.organization.acl.Authenticated;
import org.ats.services.organization.entity.fatory.ReferenceFactory;

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
 * @author NamBV2
 *
 * Jun 29, 2015
 */

@CorsComposition.Cors
@Authenticated
public class KeywordProjectController extends Controller{
  
  @Inject KeywordProjectFactory keywordProjectFactory;
  
  @Inject DataDrivenService dataDrivenService;
  
  @Inject ReferenceFactory<DataDrivenReference> dataRef;
  
  @Inject CaseService caseService;
  
  @Inject CaseFactory caseFactory;
  
  @Inject
  private SuiteService suiteService;
  
  @Inject
  private KeywordProjectService keywordProjectService;
  
  @Inject
  private ReferenceFactory<SuiteReference> suiteRefFactory;
  
  @Inject
  private ReferenceFactory<CaseReference> caseRefFactory;
  
  @Inject
  private OrganizationContext context;
  
  private Suite suite;
  
  public Result newData() {
    
    JsonNode json = request().body().asJson();
    
    //define variable for cases
    JsonNode casesNode = json.get("cases");
    String nameKeywordProject = json.get("wizardData").get("project_name").asText();
    KeywordProject keywordProject = keywordProjectFactory.create(context, nameKeywordProject);
    Map<String, Case> listCase = new HashMap<String, Case>();
 
    for(int i = 0; i < casesNode.size(); i++) {
      Case caze;
      String caseName = casesNode.get(i).get("name").asText();
      if(casesNode.get(i).get("driven") != null ) {
        String dataRefId = casesNode.get(i).get("driven").get("id").asText();
        caze = caseFactory.create(caseName, dataRef.create(dataRefId),null);
      } else {
        caze = caseFactory.create(caseName, null,null);
      }
      
      //Add action for test cases
      for(JsonNode action:casesNode.get(i).get("steps")) {
        caze.addAction(action); 
      }
      listCase.put(caze.get("name").toString(),caze);
      caseService.create(caze);
    }
    
    for(int j = 0; j < json.get("suites").size(); j++) {
      
      //define variable for suites
      JsonNode suitesNode = json.get("suites").get(j);
      String nameSuite = suitesNode.get("name").asText();
      JsonNode caseInSuiteNode = suitesNode.get("cases");
      
      SuiteBuilder suiteBuilder = new SuiteBuilder();
      suiteBuilder.packageName("org.ats.generated")
      .suiteName(nameSuite)
      .driverVar(SuiteBuilder.DEFAULT_DRIVER_VAR)
      .initDriver(SuiteBuilder.DEFAULT_INIT_DRIVER)
      .timeoutSeconds(SuiteBuilder.DEFAULT_TIMEOUT_SECONDS);
      
      for(JsonNode node:caseInSuiteNode) {
        Case caseInSuite = listCase.get(node.get("name").asText());
        suiteBuilder.addCases(caseRefFactory.create(caseInSuite.getId()));
      }
      suite = suiteBuilder.build();
      suiteService.create(suite);
      keywordProject.addSuite(suiteRefFactory.create(suite.getId()));
    }
    keywordProjectService.create(keywordProject);
      
    return ok(Json.parse(keywordProject.toString()));
  }
  
 
  public Result getTestsuites(String projectId) {
    
    KeywordProject project = keywordProjectService.get(projectId);
    
    List<org.ats.services.keyword.SuiteReference> listSuites = project.getSuites();
    
    ArrayNode array = Json.newObject().arrayNode();
    ObjectNode object ;
      
    for (org.ats.services.keyword.SuiteReference suiteRef : listSuites) {
      
      Suite suite = suiteRef.get();
      object = Json.newObject();
      object.put("_id", suiteRef.getId());
      object.put("name", suite.getString("suite_name"));
      array.add(object);
    }
    return ok(array);
  }
  
  public Result getListProject() {
    
    PageList<KeywordProject> pages = keywordProjectService.list();
    
    List<KeywordProject> list;
    ArrayNode array = Json.newObject().arrayNode();
    ObjectNode object;
    while (pages.hasNext()) {
      list = pages.next();
      
      for (KeywordProject project : list) {
        
        object = Json.newObject();
        
        object.put("projectId", project.getId());
        object.put("projectName", project.getString("name"));
        
        ArrayNode arraySuites = Json.newObject().arrayNode();
        
        int totalCases = 0;
        for (SuiteReference suiteRef : project.getSuites()) {
          
          Suite suite = suiteRef.get();
          ArrayNode arrayCases = Json.newObject().arrayNode();
          
          for (CaseReference caseRef : suite.getCases()) {
            Case caze =  caseRef.get();
            arrayCases.add(Json.parse(caze.toString()));
          }
          
          totalCases += arrayCases.size();
          suite.put("cases", arrayCases);
          arraySuites.add(Json.parse(suite.toString()));
          
        }
        object.put("totalCases", totalCases);
        object.put("suites", arraySuites);
        array.add(object);
      }
      
    }
    return ok(array);
  }
  
  public Result createKeywordProject() {
    
    JsonNode data = request().body().asJson();
    
    String projectName = data.get("projectName").asText();
    KeywordProject project = keywordProjectFactory.create(context, projectName);
    
    keywordProjectService.create(project);
    
    String id = project.getId();
    
    return ok(id);
  }
  
  public Result runKeywordProject() {
    JsonNode data = request().body().asJson();
    String id = data.get("projectId").asText();
    
    KeywordProject project = keywordProjectService.get(id);
    JsonNode suiteIds = data.get("suiteIds");
    
    SuiteReference suiteRef;
    
    for (JsonNode json : suiteIds) {
      
      String suiteId = json.get("_id").asText();
      suiteRef = suiteRefFactory.create(suiteId);
      project.addSuite(suiteRef);
    }
    
    keywordProjectService.update(project);
    return ok();
  }
  
  public Result createTestSuite() {
    
    JsonNode data = request().body().asJson().get("data");
    
    String suiteName = data.get("suite_name").asText();
    
    JsonNode cases = data.get("cases");
    
    SuiteBuilder builder = new SuiteBuilder();
    
    builder.suiteName(suiteName);
    CaseReference caseRef;
    for (JsonNode testCase : cases) {
      
      caseRef = caseRefFactory.create(testCase.get("_id").asText());
      
      builder.addCases(caseRef);
    }
    
    Suite suite = builder.build();
    
    suiteService.create(suite);
    
    String suiteId = suite.getId();
    
    return ok(suiteId);
  }
  
  public Result deleteTestSuite(String id) {
    
    suiteService.delete(id);
    return ok();
  }
  public Result getListKeywordProject(String tenant, String space) {
    
    BasicDBObject query = new BasicDBObject("tenant", new BasicDBObject("_id", tenant));
    query.append("space", "null".equals(space) ? null : new BasicDBObject("_id", space));
    
    PageList<KeywordProject> list = keywordProjectService.query(query);
    list.setSortable(new MapBuilder<String, Boolean>("created_date", true).build());
    
    ArrayNode array = Json.newObject().arrayNode();
    while(list.hasNext()) {
      List<KeywordProject> listProject = list.next();
      for(KeywordProject item : listProject) {
        array.add(Json.parse(item.toString()));
      }
    }
    System.out.println(array);
    return ok(array);
    
  }
  
  public Result getListTestCase() {
    PageList<Case> list = caseService.list();
    ArrayNode array = Json.newObject().arrayNode();
    while(list.hasNext()) {
      List<Case> listCases = list.next();
      for(Case caze : listCases) {
        array.add(Json.parse(caze.toString()));
      }
    }
    return ok(array);
  }
  
  public Result newTestCase() {
    JsonNode node = request().body().asJson();
    JsonNode casesNode = node.get("cases");
    for(int i = 0; i < casesNode.size(); i++) {
      Case caze;
      String caseName = casesNode.get(i).get("name").asText();
      String info = casesNode.get(i).get("info").asText();
      if("".equals(info)) {
        caze = caseFactory.create(caseName, null,null); 
      } else {
        caze = caseFactory.create(caseName, null,info);
      }
      
      //Add action for test cases
      for(JsonNode action:casesNode.get(i).get("steps")) {
        caze.addAction(action); 
      }
      caseService.create(caze);
    }
    
    return ok();
  }
  
  public Result getCustomKeywords(String tenant, String space, String projectId) {
    BasicDBObject query = new BasicDBObject("tenant", new BasicDBObject("_id", tenant));
    query.append("space", "null".equals(space) ? null : new BasicDBObject("_id", space));
    query.append("_id",projectId);
    
    PageList<KeywordProject> list = keywordProjectService.query(query);
    list.setSortable(new MapBuilder<String, Boolean>("created_date", true).build());
    ArrayNode array = Json.newObject().arrayNode();
    List<KeywordProject> listKeywordProject = list.next();
    Collection<CustomKeyword> listCustom = listKeywordProject.get(0).getCustomKeywords();
    Iterator<CustomKeyword> iterator = listCustom.iterator();
    while(iterator.hasNext()) {
      array.add(Json.parse(iterator.next().toString()));
    }
    return ok(array);
  }
  public Result addCustomKeyword() {
    JsonNode node = request().body().asJson();
    JsonNode customKeyNode = node.get("customKeyword");
    JsonNode keywordProjectNode = node.get("keywordProject");
    CustomKeyword customKeyword ;
    KeywordProject keywordProject;
    for(int i = 0; i < customKeyNode.size(); i++) {
      String nameCustomKeyword = customKeyNode.get(i).get("name").asText();
      String projectId = keywordProjectNode.get("projectId").asText();
      customKeyword = new CustomKeyword(nameCustomKeyword);
      //Add action for custom keyword
      for(JsonNode action:customKeyNode.get(i).get("steps")) {
        customKeyword.addAction(action); 
      }
      keywordProject = keywordProjectService.get(projectId);
      keywordProject.addCustomKeyword(customKeyword);
      keywordProjectService.update(keywordProject);
    }
    return ok();
  }
  
  public Result removeCustomKeyword(String projectId,String customKeywordName) {
    KeywordProject keywordProject = keywordProjectService.get(projectId);
    keywordProject.removeCustomKeyword(customKeywordName);
    keywordProjectService.update(keywordProject);
    return ok();
  }
  
  public Result update() {
    JsonNode node = request().body().asJson();
    JsonNode nodeProject = node.get("projectId");
    JsonNode nodeCustomKeyword = node.get("customKeyword");
    KeywordProject keywordProject ;
    String projectId = nodeProject.get("projectId").asText();
    keywordProject = keywordProjectService.get(projectId);
    for(int i = 0; i < nodeCustomKeyword.size(); i++) {
      JsonNode customKeyword = nodeCustomKeyword.get(i);
      String nameCustomKeyword = customKeyword.get("name").asText();
      String idCustomKeyword = customKeyword.get("_id").asText();  
      for(CustomKeyword item : keywordProject.getCustomKeywords()) {
        if(idCustomKeyword.equals(item.getId().toString())) {
          List<JsonNode> listActions = item.getActions();
          item.setName(nameCustomKeyword);
          System.out.println(listActions.get(0).toString()+"***");
          //listActions.clear();
          
          /*for(int j = 0; j <item.getActions().size(); j++) {
            System.out.println(item.getActions().get(j)+"&&&&");
          }*/
          for(JsonNode action:nodeCustomKeyword.get(i).get("steps")) {
            item.addAction(action);
            System.out.println(action.toString()+"-+_+_+");
          }
        }
      }
      keywordProjectService.update(keywordProject);
      
    }
    return ok();
  }
   
}
