/**
 * TrinhTV3@fsoft.com.vn
 */
package controllers;

import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ats.common.PageList;
import org.ats.common.StringUtil;
import org.ats.service.blob.BlobService;
import org.ats.services.organization.acl.Authenticated;
import org.ats.services.performance.CSV;
import org.ats.services.performance.JMeterArgument;
import org.ats.services.performance.JMeterFactory;
import org.ats.services.performance.JMeterSampler;
import org.ats.services.performance.JMeterSampler.Method;
import org.ats.services.performance.JMeterScript;
import org.ats.services.performance.JMeterScriptService;

import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http.MultipartFormData;
import play.mvc.Result;
import actions.CorsComposition;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.inject.Inject;
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
  
  public Result list(String projectId) {
     
    ArrayNode array = Json.newObject().arrayNode();
    PageList<JMeterScript> pages = service.getJmeterScripts(projectId);
    List<JMeterScript> list;
    while (pages.hasNext()) {
      list = pages.next();
      
      for (JMeterScript script : list) {
        array.add(Json.parse(service.get(script.getId(), "number_engines", "number_threads").toString()));
      }
    }
    
    return ok(array);
  }
  
  public Result createByFile(String projectId) {
    
    MultipartFormData body = request().body().asMultipartFormData();
    MultipartFormData.FilePart file = body.getFile("file");
    String name = body.asFormUrlEncoded().get("name")[0];
    
    // return badRequest if file amount is lower 1
    if (file == null) {
      return badRequest();
    }
     
      try {
        JMeterScript script = jmeterFactory.createRawJmeterScript(projectId, name, StringUtil.readStream(new FileInputStream(file.getFile())));
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
  
  public Result createBySamplers(String projectId) {
    
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
    JMeterScript script = jmeterFactory.createJmeterScript(scriptName, loops, users, ramup, false, duration, projectId, arraySamplers);
    script.setNumberEngines(number_engines);
    
    service.create(script);
    
    return ok(Json.parse(script.toString()));
  }
  
  public Result get(String projectId, String id) {
    
    JMeterScript script = service.get(id, "number_threads", "number_engines", "ram_up", "loops");
    if (script == null) return notFound();
    List<GridFSDBFile> files = fileService.find(new BasicDBObject("script_id", id));
    for (GridFSDBFile file : files) {
      script.addCSVFiles(new CSV(file.getId().toString(), file.getFilename()));
    }
    return status(200, Json.parse(script.toString()));
  }
  
  public Result delete(String projectId, String id) {
     service.delete(id);
     
     return status(202);
  }
  
  public Result update(String projectId) {
    
    JsonNode data = request().body().asJson();
    
    BasicDBObject obj = Json.fromJson(data, BasicDBObject.class);
    
    JMeterScript script = service.transform(obj);
    script.setNumberEngines(data.get("number_engines").asInt());
    script.setNumberThreads(data.get("number_threads").asInt());
    script.setRamUp(data.get("ram_up").asInt());
    script.setLoops(data.get("loops").asInt());
    
    JMeterScript oldScript = service.get(script.getId(), "number_threads", "number_engines", "ram_up", "loops");
    
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
  
}
