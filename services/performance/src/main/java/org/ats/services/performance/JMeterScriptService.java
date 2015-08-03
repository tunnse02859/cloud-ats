/**
 * 
 */
package org.ats.services.performance;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.ats.common.PageList;
import org.ats.services.data.MongoDBService;
import org.ats.services.organization.base.AbstractMongoCRUD;
import org.ats.services.performance.JMeterSampler.Method;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mongodb.BasicDBList;
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
  
  @Override
  public JMeterScript transform(DBObject source) {
    BasicDBObject obj = (BasicDBObject) source;
    //    this.put("_id", source.get("_id"));
    String name = obj.getString("name");
    int loops = obj.getInt("loops");
    int number_threads = obj.getInt("number_threads");
    int ram_up = obj.getInt("ram_up");
    boolean scheduler = obj.getBoolean("scheduler");
    int duration = obj.getInt("duration");
    String projectId = obj.getString("project_id");
    BasicDBList listSampler = (BasicDBList) obj.get("samplers");
    List<JMeterSampler> samplers = new ArrayList<JMeterSampler>();
    for (Object sampler : listSampler) {
      Method method = Method.valueOf(((BasicDBObject) sampler).getString("method"));
      String samplerName = ((BasicDBObject) sampler).getString("name");
      String url = ((BasicDBObject) sampler).getString("url");
      String assertion_text = ((BasicDBObject) sampler).getString("assertion_text");
      Long constant_time = ((BasicDBObject) sampler).get("constant_time") == null ? null : ((BasicDBObject) sampler).getLong("constant_time");

      BasicDBList listArg = (BasicDBList) ((BasicDBObject) sampler).get("arguments");
      List<JMeterArgument> arguments = new ArrayList<JMeterArgument>();
      for (Object arg : listArg) {
        String paramName = ((BasicDBObject) arg).getString("paramName");
        String paramValue = ((BasicDBObject) arg).getString("paramValue");
        arguments.add(new JMeterArgument(paramName, paramValue));
      }
      samplers.add(new JMeterSampler(method, samplerName, url, assertion_text, constant_time, arguments));
    }
    
    
    JMeterScript script = new JMeterScript(name, loops, number_threads, ram_up, scheduler, duration, projectId, samplers);
    script.put("_id", source.get("_id"));
    return script;
  }

}
