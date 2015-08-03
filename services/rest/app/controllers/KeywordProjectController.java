package controllers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
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
/**
 * @author NamBV2
 *
 * Jun 29, 2015
 */

@CorsComposition.Cors
@Authenticated
public class KeywordProjectController extends Controller{
  
  @Inject 
  private KeywordProjectFactory keywordProjectFactory;
  
  @Inject 
  private DataDrivenService dataDrivenService;
  
  @Inject 
  private ReferenceFactory<DataDrivenReference> dataRef;
  
  @Inject 
  private CaseService caseService;
  
  @Inject 
  private CaseFactory caseFactory;
  
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
        caze = caseFactory.create("fake", caseName, dataRef.create(dataRefId));
      } else {
        caze = caseFactory.create("fake", caseName, null);
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
      .timeoutSeconds(SuiteBuilder.DEFAULT_TIMEOUT_SECONDS).projectId(keywordProject.getId());
      
      for(JsonNode node:caseInSuiteNode) {
        Case caseInSuite = listCase.get(node.get("name").asText());
        suiteBuilder.addCases(caseRefFactory.create(caseInSuite.getId()));
      }
      suite = suiteBuilder.build();
      suiteService.create(suite);
    }
    keywordProjectService.create(keywordProject);
      
    return ok(Json.parse(keywordProject.toString()));
  }
  
 
  public Result getTestsuites(String projectId) {
    
    KeywordProject project = keywordProjectService.get(projectId);
    
    PageList<Suite> listSuites = suiteService.getSuites(projectId);
    
    ArrayNode array = Json.newObject().arrayNode();
    ObjectNode object ;
     
    while (listSuites.hasNext()) {
      for (Suite suite : listSuites.next()) {
        object = Json.newObject();
        object.put("_id", suite.getId());
        object.put("name", suite.getString("suite_name"));
        array.add(object);
      }
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
        Map<String,Integer> mapAllSuite = new HashMap<String,Integer>();
        PageList<Suite> listSuite = suiteService.getSuites(project.getId());
        while(listSuite.hasNext()) {
          for (Suite suite : listSuite.next()) {
            ArrayNode arrayCases = Json.newObject().arrayNode();
            
            for (CaseReference caseRef : suite.getCases()) {
              Case caze =  caseRef.get();
              arrayCases.add(Json.parse(caze.toString()));
              Integer count = mapAllSuite.get(caze.getId());
              if(count == null){
                count = new Integer(0);
              }
              count = count +1 ;
              mapAllSuite.put(caze.getId(),count);            
            }
            
            suite.put("cases", arrayCases.toString());
            arraySuites.add(Json.parse(suite.toString()));
          }
        }
        
        object.put("totalCases", mapAllSuite.size());
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
    ObjectNode object = Json.newObject();
    object.put("totalTestCases", totalTestCasesInProject(id));
    object.put("_id", id);
    
    return ok(object);
  }
  
  public Result runKeywordProject() {
    JsonNode data = request().body().asJson();
    String id = data.get("projectId").asText();
    
    KeywordProject project = keywordProjectService.get(id);
    JsonNode suiteIds = data.get("suiteIds");
    
    SuiteReference suiteRef;
    
    for (JsonNode json : suiteIds) {
      
      String suiteId = json.get("_id").asText();
      System.out.println(suiteId);
      //suiteRef = suiteRefFactory.create(suiteId);
      //project.addSuite(suiteRef);
    }
    
    //keywordProjectService.update(project);
    return ok();
  }
  
  public Result updateTestSuite() {
    JsonNode json = request().body().asJson().get("data");
    
    String projectId = json.get("projectId").asText();
    String suiteId = json.get("suiteId").asText();
    
    JsonNode cases = json.get("cases");
    
    Suite suite = suiteService.get(suiteId);
    String name = suite.getString("suite_name");
    
    List<String> cazes = new ArrayList<String>();
    for (CaseReference cazeRef : suite.getCases()) {
      cazes.add(cazeRef.getId());
    }
    
    for (String cazeId : cazes) {
      suite.removeCase(caseRefFactory.create(cazeId));
    }
    
    SuiteBuilder builder = new SuiteBuilder();
    for (JsonNode caze : cases) {
       CaseReference cazeRef = caseRefFactory.create(caze.get("_id").asText());
       Case test = cazeRef.get();
       builder.addCases(cazeRef);
    }
    
    suite = builder.build();
    
    suite.put("_id", suiteId);
    suite.put("suite_name", name);
    suite.put("project_id", projectId);
    
    suiteService.update(suite);
    
    ObjectNode object = Json.newObject();
    object.put("totalTestCases", totalTestCasesInProject(projectId));
    
    return ok(object);
  }
  
  public Result createTestSuite() {
    
    JsonNode data = request().body().asJson().get("data");
    String projectId = data.get("_id").asText();
    
    KeywordProject project = keywordProjectService.get(projectId);
    
    
    String suiteName = data.get("suite_name").asText();
    
    JsonNode cases = data.get("cases");
    
    SuiteBuilder builder = new SuiteBuilder();
    
    builder.suiteName(suiteName);
    builder.projectId("fake");
    
    CaseReference caseRef;
    for (JsonNode testCase : cases) {
      
      caseRef = caseRefFactory.create(testCase.get("_id").asText());
      
      builder.addCases(caseRef);
    }
    
    Suite suite = builder.build();
    
    suiteService.create(suite);
    
    keywordProjectService.update(project);
    String suiteId = suite.getId();
    ObjectNode object = Json.newObject();
    object.put("suiteId", suiteId);
    object.put("totalTestCases", totalTestCasesInProject(projectId));
    return ok(object);
  }
  
  public int totalTestCasesInProject(String id) {
    
    KeywordProject project = keywordProjectService.get(id);
    
    Map<String, Integer> allMap = new HashMap<String, Integer>();
    
    PageList<Suite> listSuite = suiteService.getSuites(id);
    while (listSuite.hasNext()) {
      for (Suite suite : listSuite.next()) {
        for (CaseReference caseRef : suite.getCases()) {
          allMap.put(caseRef.getId(), 0);
        }
      }
    }
    
    return allMap.size();
  }
  
  public Result deleteTestSuite(String suiteId, String projectId) {
    
    
    suiteService.delete(suiteId);
    ObjectNode object = Json.newObject();
    object.put("totalTestCases", totalTestCasesInProject(projectId));
    return ok(object);
  }
  
  public Result getListTestCase(String projectId) {
    PageList<Case> list = caseService.getCases(projectId);
    list.setSortable(new MapBuilder<String, Boolean>("created_date", true).build());
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
    JsonNode projectIdNode = node.get("projectId");
    String projectId = projectIdNode.asText();
    ArrayNode array = Json.newObject().arrayNode();
    Case caze;
    String caseName = casesNode.get("name").asText();
    String idCaseExisted = "";
    boolean exisit = false;
    
    //check existing of testcase
    PageList<Case> list = caseService.getCases(projectId);
    while(list.hasNext()) {
      List<Case> listCases = list.next();
      for(Case item : listCases) {
        if(caseName.trim().equals(item.getName().trim())) {
          exisit = true;
          idCaseExisted = item.getId().toString();
        }
      }
    }
    
    if(exisit == false) {
      System.out.println("--not exist");
      caze = caseFactory.create(projectId,caseName, null);
      //Add action for test cases
      for(JsonNode action:casesNode.get("steps")) {
        caze.addAction(action); 
      }
      caseService.create(caze);
    } else {
      System.out.println("-- exist");
      caze = caseService.get(idCaseExisted);
      caze.clearActions();
      //Add action for test cases
      for(JsonNode action:casesNode.get("steps")) {
        caze.addAction(action); 
      }
      caseService.update(caze);
    }
    
    array.add(Json.parse(caze.toString()));
    
    
    return ok(array);
  }
  
  public Result getCustomKeywords(String tenant, String space, String projectID) {
    KeywordProject keywordProject = keywordProjectService.get(projectID);
    Collection<CustomKeyword> listCustomKeyword =  keywordProject.getCustomKeywords();
    ArrayNode array = Json.newObject().arrayNode();
    for(CustomKeyword item: listCustomKeyword) {
      array.add(Json.parse(item.toString()));
    }
    return ok(array);
  }
  
  public Result addCustomKeyword() {
    JsonNode node = request().body().asJson();
    JsonNode customKeyNode = node.get("customKeyword");
    JsonNode keywordProjectNode = node.get("projectId");
    CustomKeyword customKeyword ;
    KeywordProject keywordProject;
    ArrayNode array = Json.newObject().arrayNode();
    String nameCustomKeyword = customKeyNode.get("name").asText();
    String projectId = keywordProjectNode.asText();
    keywordProject = keywordProjectService.get(projectId);
    customKeyword = new CustomKeyword(nameCustomKeyword);
    
    //Add action for custom keyword
    for(JsonNode action:customKeyNode.get("steps")) {
      customKeyword.addAction(action); 
    }
    
    keywordProject.addCustomKeyword(customKeyword);
    keywordProjectService.update(keywordProject);
    array.add(Json.parse(customKeyword.toString()));
    
    return ok(array);
  }
  
  public Result removeCustomKeyword(String projectId,String customKeywordId) {
    KeywordProject keywordProject = keywordProjectService.get(projectId);
    keywordProject.removeCustomKeyword(customKeywordId);
    keywordProjectService.update(keywordProject);
    return ok();
  }
  
  public Result updateCustomKeyword() {
    JsonNode node = request().body().asJson();
    JsonNode nodeProject = node.get("projectId");
    JsonNode nodeCustomKeyword = node.get("customKeyword");
    KeywordProject keywordProject ;
    String projectId = nodeProject.asText();
    ArrayNode array = Json.newObject().arrayNode();
    keywordProject = keywordProjectService.get(projectId);

    JsonNode customKeyword = nodeCustomKeyword;
    String idCustomKeyword = customKeyword.get("_id").asText();
    CustomKeyword newCustomKeyword = new CustomKeyword(customKeyword.get("name").asText());
    
    for(JsonNode action:customKeyword.get("steps")) {
      newCustomKeyword.addAction(action); 
    }
    
    for(CustomKeyword item : keywordProject.getCustomKeywords()) {
      if(idCustomKeyword.equals(item.getId().toString())) {
        String _idCurrentProject = item.getId().toString();
        keywordProject.removeCustomKeyword(item.getName());
        newCustomKeyword.put("_id", _idCurrentProject);
        keywordProject.addCustomKeyword(newCustomKeyword);
        
        keywordProjectService.update(keywordProject);
      }
    }
    array.add(Json.parse(newCustomKeyword.toString()));
 
    return ok(array);
  }
  
  public Result updateCase() {
    JsonNode node = request().body().asJson();
    JsonNode nodeCase = node.get("cases");
    String caseId = nodeCase.get("_id").asText();
    String newNameCase = nodeCase.get("name").asText();
    Case caze = caseService.get(caseId);
    ArrayNode array = Json.newObject().arrayNode();
    caze.clearActions();
    for(JsonNode action:nodeCase.get("steps")) {
      caze.addAction(action);
    }
    caze.setName(newNameCase);
    caseService.update(caze);
    array.add(Json.parse(caze.toString()));
    return ok(array);
  }
  
  public Result removeCase(String caseId,String projectId) {
    PageList<Case> list = caseService.getCases(projectId);
    while(list.hasNext()) {
      List<Case> listCases = list.next();
      for(Case caze: listCases) {
        if(caseId.equals(caze.getId().toString())) {
          caseService.delete(caseId);
        }
      }
    }
    return ok();
  }
   
}
