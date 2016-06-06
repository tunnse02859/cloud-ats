/**
 * TrinhTV3@fsoft.com.vn
 */
package controllers;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.ats.common.PageList;
import org.ats.common.StringUtil;
import org.ats.service.blob.BlobService;
import org.ats.services.OrganizationContext;
import org.ats.services.organization.UserService;
import org.ats.services.organization.acl.Authenticated;
import org.ats.services.organization.acl.Authorized;
import org.ats.services.organization.entity.User;
import org.ats.services.performance.CSV;
import org.ats.services.performance.JMeterArgument;
import org.ats.services.performance.JMeterFactory;
import org.ats.services.performance.JMeterSampler;
import org.ats.services.performance.JMeterSampler.Method;
import org.ats.services.performance.JMeterScript;
import org.ats.services.performance.JMeterScriptService;
import org.ats.services.project.MixProject;
import org.ats.services.project.MixProjectService;

import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http.MultipartFormData;
import play.mvc.Result;
import actions.CorsComposition;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;

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
  private JMeterFactory jmeterFactory;
  
  @Inject 
  private BlobService fileService;
  
  @Inject MixProjectService mpService;
  
  @Inject UserService userService;
  
  @Inject OrganizationContext context;
  
  @Authorized(feature="project", action="view_performance")
  public Result list(String projectId) {
    
    MixProject mp = mpService.get(projectId);
    PageList<JMeterScript> pages = service.getJmeterScripts(mp.getPerformanceId());
    List<JMeterScript> list;
    
    BasicDBList array = new BasicDBList();
    while (pages.hasNext()) {
      list = pages.next();
      
      for (JMeterScript script : list) {
        
        String email = script.getCreator();
        User user = userService.get(email);
        BasicDBObject userObj = new BasicDBObject("email", email).append("first_name", user.getFirstName()).append("last_name", user.getLastName());
        script.put("creator", userObj);
        script.put("created_date", script.get("created_date") != null ? script.getDate("created_date").getTime() : new Date().getTime());
        array.add(script);
      }
    }
    
    mp.put("project_name", mp.getName());
    mp.put("scripts", array);
    return ok(Json.parse(mp.toString()));
  }
  
  @Authorized(feature="project", action="manage_performance")
  public Result createByFile(String projectId) {
	MixProject mp = mpService.get(projectId);
    MultipartFormData body = request().body().asMultipartFormData();
    MultipartFormData.FilePart file = body.getFile("file");
    String name = body.asFormUrlEncoded().get("name")[0];
    
    // return badRequest if file amount is lower 1
    if (file == null) {
      return badRequest();
    }
     
      try {
        JMeterScript script = jmeterFactory.createRawJmeterScript(mp.getPerformanceId(), name, context.getUser().getEmail(), StringUtil.readStream(new FileInputStream(file.getFile())));
        script.setNumberThreads(100);
        script.setNumberEngines(1);
        script.setRamUp(5);
        script.setLoops(1);
        service.create(script);
        return ok(Json.parse(script.toString()));
      } catch(Exception e) {
         throw new RuntimeException();
      }
  }
  
  @Authorized(feature="project", action="manage_performance")
  public Result createBySamplers(String projectId) {
	MixProject mp = mpService.get(projectId);
	
    JsonNode data = request().body().asJson();
    int i = 0;
    // configuration information for script
    int loops = data.get("loops").asInt();
    int ramup = data.get("ram_up").asInt();
    int duration = data.get("duration").asInt();
    int users = data.get("number_threads").asInt();
    int number_engines = data.get("number_engines").asInt();
    
    
    
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
        if ((!"".equals(jsonParam.get("paramName").asText())) && (!"".equals(jsonParam.get("paramValue").asText())) ) {
          arrayParams[j] = jmeterFactory.createArgument(jsonParam.get("paramName").asText(), jsonParam.get("paramValue").asText());
          j ++;
        }
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
    JMeterScript script = jmeterFactory.createJmeterScript(scriptName, loops, users, ramup, false, duration, mp.getPerformanceId(), context.getUser().getEmail(), arraySamplers);
    script.setNumberEngines(number_engines);
    
    service.create(script);
    
    return status(201, Json.parse(script.toString()));
  }
  
  public Result get(String projectId, String id) {
    
    MixProject project =mpService.get(projectId);
    
    JMeterScript script = service.get(id, "number_threads", "number_engines", "ram_up", "loops");
    if (script == null) return notFound();
    List<GridFSDBFile> files = fileService.find(new BasicDBObject("script_id", id));
    for (GridFSDBFile file : files) {
      script.addCSVFiles(new CSV(file.getId().toString(), file.getFilename()));
    }
    script.put("projectName", project.getName());
    return status(200, Json.parse(script.toString()));
  }
  
  public Result delete(String projectId, String id) {
     service.delete(id);
     
     return status(202);
  }
  
  @Authorized(feature="project", action="manage_performance")
  public Result update(String projectId) {
    
    JsonNode data = request().body().asJson();
    BasicDBObject obj = Json.fromJson(data, BasicDBObject.class);
    
    JMeterScript script = service.transform(obj);
    script.setNumberEngines(data.get("number_engines").asInt());
    script.setNumberThreads(data.get("number_threads").asInt());
    script.setRamUp(data.get("ram_up").asInt());
    script.setLoops(data.get("loops").asInt());
    
    JMeterScript oldScript = service.get(script.getId(), "number_threads", "number_engines", "ram_up", "loops");
    script.put("created_date", oldScript.getDate("created_date"));
    // handle csv files
    List<GridFSDBFile> listCSV = fileService.find(new BasicDBObject("script_id", script.getId()));
    int countFileChange = 0;
    
    for (GridFSDBFile file : listCSV) {
      if (file.getId().toString().contains("_temp")) {
        
        countFileChange ++;
        GridFSInputFile fileInput = fileService.create(file.getInputStream());
        String fileId = file.getId().toString().substring(0, file.getId().toString().length() - 5);
        fileService.deleteById(fileId.toString());
        
        fileInput.setId(fileId.toString());
        fileInput.put("script_id", script.getId());
        fileInput.put("filename", file.getFilename());
        fileService.save(fileInput);
      }
    }
    
    for (GridFSDBFile file : listCSV) {
      if (file.getId().toString().contains("_temp")) {
        fileService.deleteById(file.getId().toString());
      }
    }
    
    Set<String> listDeletedCSV = new HashSet<String>();
    Set<String> listOriginCSV = new HashSet<String>();
    if (data.get("csv_files") != null) {
      
      for (JsonNode json : data.get("csv_files")) {
        listDeletedCSV.add(json.get("_id").asText());
      }
      
      
      for (GridFSDBFile file : listCSV) {
        listOriginCSV.add(file.getId().toString());
      }    
      
      for (String s : listOriginCSV) {
        if (!listDeletedCSV.contains(s)) {
          fileService.deleteById(s);
        }
      }
    }
    
    if (!script.getId().equals(oldScript.getId()) || oldScript == null) {
      return status(400);
    }
    
    if (script.equals(oldScript) && (countFileChange == 0) && (listDeletedCSV.size() == listOriginCSV.size())) {
      return status(204);
    }
    
    service.update(script);
    return status(202);
  }
  
  @Authorized(feature="project", action="manage_performance")
  public Result cloneScript(String projectId, String scriptId) throws IOException {
    
    String name = request().getQueryString("name");
    
    JMeterScript script = service.get(scriptId);
    
    // start to create new script
    String id = UUID.randomUUID().toString();
    
    script.put("name", name);
    script.put("_id", id);
    script.put("created_date", new Date());
    
    service.create(script);
    
    // start to create new csv file for this cloned script
    List<GridFSDBFile> files = fileService.find(new BasicDBObject("script_id", scriptId));
    for (GridFSDBFile file : files) {
      String file_id = UUID.randomUUID().toString();
      
      GridFSInputFile newFile = fileService.create(file.getInputStream());
      newFile.put("_id", file_id);
      newFile.put("script_id", id);
      newFile.put("filename", file.getFilename());
      
      fileService.save(newFile);
      script.addCSVFiles(new CSV(newFile.getId().toString(), newFile.getFilename()));
    }
    
    script.put("created_date", script.getDate("created_date").getTime());
    
    String email = script.getCreator();
    User user = userService.get(email);
    BasicDBObject userObj = new BasicDBObject("email", email).append("first_name", user.getFirstName()).append("last_name", user.getLastName());
    script.put("creator", userObj);
    
    return ok(Json.parse(script.toString()));
  }
  
}
