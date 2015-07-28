/**
 * TrinhTV3@fsoft.com.vn
 */
package controllers;

import java.util.List;

import org.ats.common.PageList;
import org.ats.services.keyword.CaseReference;
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
 * @author TrinhTV3
 *
 */

@CorsComposition.Cors
@Authenticated
public class KeywordProjectController extends Controller {

   
   @Inject
   private SuiteService suiteService;
   
   @Inject
   private KeywordProjectService keywordProjectService;
   
   @Inject
   private KeywordProjectFactory projectFactory;
   
   @Inject
   private ReferenceFactory<SuiteReference> suiteRefFactory;
   
   @Inject
   private ReferenceFactory<CaseReference> caseRefFactory;
   
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
           
          /* for (CaseReference caseRef : suite.getCases()) {
             Case caze =  caseRef.get();
             arrayCases.add(Json.parse(caze.toString()));
           }*/
           
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
     KeywordProject project = projectFactory.create(projectName);
     
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
       
       caseRef = caseRefFactory.create(testCase.get("_id").toString());
       
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
}
