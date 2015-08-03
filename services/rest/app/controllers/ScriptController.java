/**
 * TrinhTV3@fsoft.com.vn
 */
package controllers;

import java.io.FileInputStream;
import java.util.List;

import org.ats.common.PageList;
import org.ats.common.StringUtil;
import org.ats.services.keyword.KeywordProjectService;
import org.ats.services.organization.acl.Authenticated;
import org.ats.services.performance.JMeterFactory;
import org.ats.services.performance.JMeterParser;
import org.ats.services.performance.JMeterSampler;
import org.ats.services.performance.JMeterScript;
import org.ats.services.performance.JMeterScriptService;
import org.ats.services.performance.PerformanceProject;
import org.ats.services.performance.PerformanceProjectService;

import actions.CorsComposition;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.inject.Inject;

import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;

/**
 * @author TrinhTV3
 *
 */
@CorsComposition.Cors
@Authenticated
public class ScriptController extends Controller {
  
  @Inject
  private JMeterScriptService service;
  
  @Inject
  private PerformanceProjectService projectService;
  
  @Inject
  private JMeterFactory jmeterFactory;
  
  public Result list(String projectId) {
     
    ArrayNode array = Json.newObject().arrayNode();
    PageList<JMeterScript> pages = service.getJmeterScripts(projectId);
    List<JMeterScript> list;
    while (pages.hasNext()) {
      list = pages.next();
      
      for (JMeterScript script : list) {
        array.add(Json.parse(script.toString()));
      }
    }
    
    return ok(array);
  }
  
  public Result createByFile(String projectId) {
    
    MultipartFormData body = request().body().asMultipartFormData();
    List<FilePart> listFiles = body.getFiles();
    
    // return badRequest if file amount is lower 1
    if (listFiles.size() <= 0) {
      return badRequest();
    }
    long i = service.getJmeterScripts(projectId).count();
     
    // create performance project model 
    FileInputStream fis;
    String content;
    JMeterParser parse;
    JMeterScript script;
    
    ArrayNode array = Json.newObject().arrayNode();
    // loop though files
    for (FilePart file : listFiles) {
      i ++;
      try {
        // read each file and get file content
        fis = new FileInputStream(file.getFile());
        content = StringUtil.readStream(fis);
        
        // create jmeter parser by file content and build jmeter script 
        parse = jmeterFactory.createJMeterParser(content, projectId);
        script = parse.parse();
        
        script.put("name", "script " + i);
        script.put("project_id", projectId);
        
        array.add(Json.parse(script.toString()));
        // save jmeter script into database
        service.create(script);
        
      } catch(Exception e) {
         throw new RuntimeException();
      }
    }
    
    return ok(array);
  }
  
  public Result createBySamplers(String projectId) {
    
    JsonNode data = request().body().asJson().get("scriptName");
    long i = service.getJmeterScripts(projectId).count();
    
    String scriptName = data.asText();
    JMeterSampler[] arraySamplers = new JMeterSampler[0];
    JMeterScript script = jmeterFactory.createJmeterScript("script "+ ( i + 1 ), 1, 1, 5, false, 0, projectId, arraySamplers);
    
    service.create(script);
    
    return ok(Json.parse(script.toString()));
  }
}
