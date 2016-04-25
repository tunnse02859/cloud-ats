/**
 * 
 */
package org.ats.services.performance;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.ats.common.PageList;
import org.ats.services.OrganizationContext;
import org.ats.services.data.MongoDBService;
import org.ats.services.organization.base.AbstractMongoCRUD;
import org.ats.services.performance.JMeterSampler.Method;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Jun 11, 2015
 */
@Singleton
public class JMeterScriptService extends AbstractMongoCRUD<JMeterScript> {

  /** .*/
  private final String COL_NAME = "performance-jmeter-script";
  
  @Inject OrganizationContext context;
  
  @Inject
  JMeterScriptService(MongoDBService mongo, Logger logger) {
    this.col = mongo.getDatabase().getCollection(COL_NAME);
    this.logger = logger;

    //create text index
    this.createTextIndex("name");
  }
  
  public PageList<JMeterScript> getJmeterScripts(String projectId) {
    return query(new BasicDBObject("project_id", projectId));
  }
  
  @SuppressWarnings("rawtypes")
  @Override
  public JMeterScript transform(DBObject source) {
    BasicDBObject obj = (BasicDBObject) source;
    String creator = source.get("creator") == null ? context.getUser().getEmail() : (String) source.get("creator");
    if (source.get("raw") != null && obj.getBoolean("raw")) {
      JMeterScript script = new JMeterScript(obj.getString("project_id"), obj.getString("name"), creator, obj.getString("raw_content"));
      script.put("_id", obj.getString("_id"));
      script.setLoops(obj.getInt("loops"));
      script.setNumberThreads(obj.getInt("number_threads"));
      script.setRamUp(obj.getInt("ram_up"));
      script.setNumberEngines(obj.getInt("number_engines"));
      return script;
    }
    
    String name = obj.getString("name");
    int loops = obj.getInt("loops");
    int number_threads = obj.getInt("number_threads");
    int ram_up = obj.getInt("ram_up");
    boolean scheduler = obj.getBoolean("scheduler");
    int duration = obj.getInt("duration");
    String projectId = obj.getString("project_id");
    
    List<JMeterSampler> samplers = new ArrayList<JMeterSampler>();
    
    ArrayList listSampler = (ArrayList) obj.get("samplers");
      
    ObjectMapper mapper = new ObjectMapper();
    for (Object bar : listSampler) {
      JsonNode data = null;
      try {
        if (bar instanceof Map) {
          JsonNode json = mapper.valueToTree(bar);
          data = mapper.readTree(json.toString());
        } else if (bar instanceof DBObject) {
          data = mapper.readTree(bar.toString());
        }
        
        Method method = Method.valueOf(data.get("method").asText());
        String samplerName = data.get("name").asText();
        String url = data.get("url").asText();
        String asserttion_text = null;
        if (data.has("assertion_text") && !"null".equals(data.get("assertion_text").asText())) {
           asserttion_text = data.get("assertion_text").asText();
        }
        Long constant_time = data.get("constant_time").asLong();
        
        JsonNode listArg = data.get("arguments");
        List<JMeterArgument> arguments =  new ArrayList<JMeterArgument>();
        if (listArg.size() > 0) {
          for (JsonNode json : listArg) {
            String paramName = json.get("paramName").asText();
            String paramValue = json.get("paramValue").asText();
            if (!"".equals(paramName) && !"".equals(paramValue)) {
              arguments.add(new JMeterArgument(paramName, paramValue));
            }
          }
        }
        samplers.add(new JMeterSampler(method, samplerName, url, asserttion_text, constant_time, arguments));
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    JMeterScript script = new JMeterScript(name, loops, number_threads, ram_up, scheduler, duration, projectId, creator, samplers);
    script.setNumberEngines(obj.get("number_engines") != null ? obj.getInt("number_engines") : 1);
    script.put("_id", source.get("_id"));
    return script;
  }

}
