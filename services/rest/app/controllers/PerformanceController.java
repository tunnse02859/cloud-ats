/**
 * TrinhTV3@fsoft.com.vn
 */
package controllers;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.ats.common.StringUtil;
import org.ats.services.organization.acl.Authenticated;
import org.ats.services.performance.JMeterArgument;
import org.ats.services.performance.JMeterFactory;
import org.ats.services.performance.JMeterParser;
import org.ats.services.performance.JMeterSampler;
import org.ats.services.performance.JMeterSampler.Method;
import org.ats.services.performance.JMeterScript;
import org.ats.services.performance.JMeterScriptService;

import play.mvc.Controller;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.Result;
import actions.CorsComposition;

import com.fasterxml.jackson.databind.JsonNode;
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
    
    // loops through all samplers
    int i = 0;
    for (JsonNode jsonSampler : nodeSamplers) {
      
      String sampler_url = jsonSampler.get("sampler_url").toString();
      String sampler_assertionTime = jsonSampler.get("sampler_assertionTime").toString();
      long sampler_constantTime = jsonSampler.get("sampler_constantTime").asLong();
      String sampler_method = jsonSampler.get("sampler_method").asText();
      String sampler_name = jsonSampler.get("sampler_name").toString();
      
      JsonNode nodeParams = jsonSampler.get("params");
      JMeterArgument[] arrayParams = new JMeterArgument[nodeParams.size()];
      
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
    
    //create jmeter script
    JMeterScript script = jmeterFactory.createJmeterScript(projectName, loops, users, ramup, false, duration, arraySamplers);
    
    jmeterService.create(script); // save jmeter script into database
    
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
    FilePart uploadedFile = body.getFile("uploadedFile");
    
    String testName = request().getQueryString("project_name");
    
    if(uploadedFile != null) {
      
      try {
        // read file
        FileInputStream fis = new FileInputStream(uploadedFile.getFile());
       
        // get file content
        String content = StringUtil.readStream(fis);
        
        // create jmeter script
        JMeterParser parse = jmeterFactory.createJMeterParser(content);
        JMeterScript script = parse.parse();
        
        script.put("name", testName);
        // save into database
        jmeterService.create(script);
      } catch (Exception e) {
        throw new RuntimeException();
      }
     
     return ok();
    }
    return badRequest();
  }
}
