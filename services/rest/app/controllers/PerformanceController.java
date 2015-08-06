/**
 * TrinhTV3@fsoft.com.vn
 */
package controllers;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.ats.common.PageList;
import org.ats.common.StringUtil;
import org.ats.services.OrganizationContext;
import org.ats.services.keyword.KeywordProject;
import org.ats.services.organization.acl.Authenticated;
import org.ats.services.organization.entity.fatory.ReferenceFactory;
import org.ats.services.performance.JMeterArgument;
import org.ats.services.performance.JMeterFactory;
import org.ats.services.performance.JMeterParser;
import org.ats.services.performance.JMeterSampler;
import org.ats.services.performance.JMeterSampler.Method;
import org.ats.services.performance.JMeterScript;
import org.ats.services.performance.JMeterScriptReference;
import org.ats.services.performance.JMeterScriptService;
import org.ats.services.performance.PerformanceProject;
import org.ats.services.performance.PerformanceProjectFactory;
import org.ats.services.performance.PerformanceProjectService;

import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
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
public class PerformanceController extends Controller {
  
  @Inject
  private JMeterFactory jmeterFactory;
  
  @Inject
  private JMeterScriptService jmeterService;
  
  @Inject
  private PerformanceProjectFactory projectFactory;
  
  @Inject
  private PerformanceProjectService projectService;
  
  @Inject
  private ReferenceFactory<JMeterScriptReference> jmeterReferenceFactory;
  
  @Inject OrganizationContext context;
  /**
   * 
   * @return
   */
  public Result listPerformanceProject() {
    
    PageList<PerformanceProject> list = projectService.list();
    
    ArrayNode array = Json.newObject().arrayNode();
    
    while (list.hasNext()) {
      
      List<PerformanceProject> listPer = list.next();
      ObjectNode object;
      for (PerformanceProject per : listPer) {
         object = Json.newObject();
         object.put("_id", per.getString("_id"));
         array.add(object);
      }
      
    }
    System.out.println(array);
    return ok(array);
  }
  
  
  /**
   * Create test performance project by using wizard
   * @return
   * @throws UnsupportedEncodingException
   */
  public Result createPerformanceTestWizard () throws UnsupportedEncodingException {
    
    JsonNode data = request().body().asJson();
    
    String projectName = data.get("project_name").toString();
    int loops = data.get("loops").asInt();
    int ramup = data.get("ramup").asInt();
    int duration = data.get("duration").asInt();
    int users = data.get("users").asInt();
    
    JsonNode nodeSamplers = data.get("samplers");
    JMeterSampler[] arraySamplers = new JMeterSampler[nodeSamplers.size()];
    
    // declare variables
    String sampler_url;
    String sampler_assertionTime = null;
    long sampler_constantTime;
    String sampler_method;
    String sampler_name;
    JsonNode nodeParams;
    JMeterArgument[] arrayParams;
    // loops through all samplers
    int i = 0;
    for (JsonNode jsonSampler : nodeSamplers) {
      
      sampler_url = jsonSampler.get("sampler_url").toString();
      
      if (jsonSampler.get("sampler_assertionTime") != null && "".equals(jsonSampler.get("sampler_assertionTime").toString())) {
        sampler_assertionTime = jsonSampler.get("sampler_assertionTime").toString();
      }
      
      sampler_constantTime = jsonSampler.get("sampler_constantTime").asLong();
      sampler_method = jsonSampler.get("sampler_method").asText();
      sampler_name = jsonSampler.get("sampler_name").toString();
      
      nodeParams = jsonSampler.get("params");
      arrayParams = new JMeterArgument[nodeParams.size()];
      
      // loops through all parameters in each sampler
      int j = 0;
      for (JsonNode jsonParam: nodeParams) {
        
        //create parameter object
        arrayParams[j] = jmeterFactory.createArgument(jsonParam.get("name").toString(), jsonParam.get("value").toString());
        j ++;
      }
      
      //create sampler object
      arraySamplers[i] = jmeterFactory.createHttpRequest(Method.valueOf(sampler_method), sampler_name, sampler_url, sampler_assertionTime, sampler_constantTime, arrayParams);
      
      i ++;
      
    }

    PerformanceProject project = projectFactory.create(projectName);
    projectService.create(project);
    
    //create jmeter script
    JMeterScript script = jmeterFactory.createJmeterScript("Script 1", loops, users, ramup, false, duration, project.getId(), arraySamplers);
    jmeterService.create(script); // save jmeter script into database
    
    return status(200);
  }
  
  /**
   * Update performance test script by wizard
   * @return
   * @throws UnsupportedEncodingException 
   */
  public Result updatePerformanceTestWizard() throws UnsupportedEncodingException {
    
    JsonNode data = request().body().asJson();
        
    String scriptId = data.get("script_id").asText();
    
    int loops = data.get("loops").asInt();
    System.out.println("AAAloop:" + loops);
    int ramup = data.get("ramup").asInt();
    int duration = data.get("duration").asInt();
    int users = data.get("users").asInt();
    
    JsonNode nodeSamplers = data.get("samplers");
    JMeterSampler[] arraySamplers = new JMeterSampler[nodeSamplers.size()];
    
    // declare variables
    String sampler_url;
    String sampler_assertionTime = null;
    long sampler_constantTime;
    String sampler_method;
    String sampler_name;
    JsonNode nodeParams;
    JMeterArgument[] arrayParams;
    // loops through all samplers
    int i = 0;
    for (JsonNode jsonSampler : nodeSamplers) {
      
      sampler_url = jsonSampler.get("sampler_url").toString();
      
      if (jsonSampler.get("sampler_assertionTime") != null && "".equals(jsonSampler.get("sampler_assertionTime").toString())) {
        sampler_assertionTime = jsonSampler.get("sampler_assertionTime").toString();
      }
      
      sampler_constantTime = jsonSampler.get("sampler_constantTime").asLong();
      sampler_method = jsonSampler.get("sampler_method").asText();
      sampler_name = jsonSampler.get("sampler_name").toString();
      
      nodeParams = jsonSampler.get("params");
      arrayParams = new JMeterArgument[nodeParams.size()];
      
      // loops through all parameters in each sampler
      int j = 0;
      for (JsonNode jsonParam: nodeParams) {
        
        //create parameter object
        arrayParams[j] = jmeterFactory.createArgument(jsonParam.get("name").toString(), jsonParam.get("value").toString());
        j ++;
      }
      
      //create sampler object
      arraySamplers[i] = jmeterFactory.createHttpRequest(Method.valueOf(sampler_method), sampler_name, sampler_url, sampler_assertionTime, sampler_constantTime, arrayParams);
      
      i ++;
      
    }
    
    JMeterScript script = jmeterService.get(scriptId);
    script.setLoops(loops);
    script.setDuration(duration);
    script.setNumberThreads(users);
    script.setRamUp(ramup);
    script.addSampler(arraySamplers);
    
    jmeterService.update(script);
    
    return status(200);
  }
  
  /**
   * Create performance by upload jmx file
   * @return
   * @throws FileNotFoundException
   * @throws IOException
   */
  public Result createPerformanceTestByFile() throws FileNotFoundException, IOException {
    
    MultipartFormData body = request().body().asMultipartFormData();
    List<FilePart> listFiles = body.getFiles();
    
    String scriptName = request().getQueryString("script_name");
    String projectId = request().getQueryString("project_id");
    
    PerformanceProject project = projectService.get(projectId);
    // return badRequest if file amount is lower 1
    if (listFiles.size() <= 0) {
      return badRequest();
    }
    long i = jmeterService.getJmeterScripts(projectId).count();
     
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
        parse = jmeterFactory.createJMeterParser(content, project.getId());
        script = parse.parse();
        
        script.put("name", "script" + i);
        script.put("project_id", project.getId());
        
        array.add(Json.parse(script.toString()));
        // save jmeter script into database
        jmeterService.create(script);
        
      } catch(Exception e) {
         throw new RuntimeException();
      }
    }
    
    return ok(array);
  }
  
  public Result list() {
    PageList<PerformanceProject> list = projectService.list();
    ArrayNode array = Json.newObject().arrayNode();
    
    while(list.hasNext()) {
      for (PerformanceProject project : list.next()) {
        project.put("type", "performance");
        project.put("totalScripts", jmeterService.getJmeterScripts(project.getId()).count());
        array.add(Json.parse(project.toString()));
      }
    }
    
    return ok(array);
  }
  
  public Result create() {
    
    JsonNode json = request().body().asJson();
    String name = json.get("name").asText();
    
    PerformanceProject project = projectFactory.create(name);
    projectService.create(project);
    return ok(project.getId());
  }
  
  public Result get(String projectId) {
    
    PerformanceProject project = projectService.get(projectId);
    if (project == null) return status(404);
    
    PageList<JMeterScript> pages = jmeterService.getJmeterScripts(projectId);
    ArrayNode array = Json.newObject().arrayNode();
    List<JMeterScript> list;
    while (pages.hasNext()) {
      
      list = pages.next();
      for (JMeterScript script : list) {
        
        array.add(Json.parse(script.toString()));
      }
    }
    project.put("type", "performance");
    project.put("totalScripts", jmeterService.getJmeterScripts(projectId).count());
    return ok(Json.parse(project.toString()));
  }
  
  public Result run(String projectId) {
    
    System.out.println(projectId);
    
    JsonNode data = request().body().asJson();
    for (JsonNode json : data) {
      System.out.println(json.asText());
    }
    
    return ok();
  }
  
}
