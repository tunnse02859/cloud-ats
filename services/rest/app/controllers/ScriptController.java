/**
 * TrinhTV3@fsoft.com.vn
 */
package controllers;

import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.ats.common.PageList;
import org.ats.common.StringUtil;
import org.ats.services.organization.acl.Authenticated;
import org.ats.services.performance.JMeterArgument;
import org.ats.services.performance.JMeterFactory;
import org.ats.services.performance.JMeterParser;
import org.ats.services.performance.JMeterSampler;
import org.ats.services.performance.JMeterSampler.Method;
import org.ats.services.performance.JMeterScript;
import org.ats.services.performance.JMeterScriptService;
import org.ats.services.performance.PerformanceProjectService;

import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.Result;
import actions.CorsComposition;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.inject.Inject;
import com.mongodb.BasicDBObject;

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
    
    JsonNode data = request().body().asJson();
    int i = 0;
    // configuration information for script
    int loops = data.get("loops").asInt();
    int ramup = data.get("ram_up").asInt();
    int duration = data.get("duration").asInt();
    int users = data.get("number_threads").asInt();
    
    JsonNode nodeSamplers = data.get("samplers");
    String scriptName = data.get("name").asText();
    
    JMeterSampler[] arraySamplers = new JMeterSampler[nodeSamplers.size()];
    
    // declare variables
    String sampler_url = null;
    String sampler_assertionTime = null;
    long sampler_constantTime = 0;
    String sampler_method = null;
    String sampler_name = null;
    JsonNode nodeParams = null;
    JMeterArgument[] arrayParams;
    // loops through all samplers
    for (JsonNode jsonSampler : nodeSamplers) {
      if (jsonSampler.has("url")) {
        sampler_url = jsonSampler.get("url").asText();
      }
      
      if (jsonSampler.has("assertion_text")) {
        sampler_assertionTime = jsonSampler.get("assertion_text").asText();
      }
      if (jsonSampler.has("constant_time")) {
        sampler_constantTime = jsonSampler.get("constant_time").asLong();
      }
      
      sampler_method = jsonSampler.get("method").asText();
      
      sampler_name = jsonSampler.get("name").asText();
      
      if (jsonSampler.has("arguments")) {
        nodeParams = jsonSampler.get("arguments");
      }
      arrayParams = new JMeterArgument[nodeParams.size()];
      
      // loops through all parameters in each sampler
      int j = 0;
      for (JsonNode jsonParam: nodeParams) {
        
        //create parameter object
        arrayParams[j] = jmeterFactory.createArgument(jsonParam.get("paramName").asText(), jsonParam.get("paramValue").asText());
        j ++;
      }
      
      //create sampler object
      try {
        arraySamplers[i] = jmeterFactory.createHttpRequest(Method.valueOf(sampler_method.toUpperCase()), sampler_name, sampler_url, sampler_assertionTime, sampler_constantTime, arrayParams);
      } catch (UnsupportedEncodingException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      
      i ++;
      
    }
    JMeterScript script = jmeterFactory.createJmeterScript(scriptName, loops, users, ramup, false, duration, projectId, arraySamplers);
    
    service.create(script);
    
    return ok(Json.parse(script.toString()));
  }
  
  public Result get(String id) {
    
    JMeterScript script = service.get(id);
    
    return status(200, Json.parse(script.toString()));
  }
  
  public Result delete(String id) {
     service.delete(id);
     
     return status(202);
  }
  
  public Result update() {
    
    JsonNode data = request().body().asJson();
    
    BasicDBObject obj = Json.fromJson(data, BasicDBObject.class);
    
    JMeterScript script = service.transform(obj);
    
    JMeterScript oldScript = service.get(script.getId());
    
    if (!script.getId().equals(oldScript.getId()) || oldScript == null) {
      return status(400);
    }
    
    if (script.equals(oldScript)) {
      return status(204);
    }
    return status(202);
  }
}
