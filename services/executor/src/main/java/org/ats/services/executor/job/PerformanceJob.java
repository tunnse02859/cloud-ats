/**
 * 
 */
package org.ats.services.executor.job;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ats.services.organization.entity.fatory.ReferenceFactory;
import org.ats.services.performance.JMeterScriptReference;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;


/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Jul 21, 2015
 */
@SuppressWarnings("serial")
public class PerformanceJob extends AbstractJob<PerformanceJob> {
  
  @Inject
  private ReferenceFactory<JMeterScriptReference> jmeterRefFactory;
  
  @Inject
  PerformanceJob(@Assisted("id") String id, 
      @Assisted("projectId") String projectId, 
      @Assisted("vmachineId") String vmachineId, 
      @Assisted("status") Status status,
      @Assisted("scripts") List<JMeterScriptReference> scripts) {
    
    super(id, projectId, vmachineId);
    
    setStatus(status);
    
    this.put("type", Type.Performance.toString());
    
    BasicDBList list = new BasicDBList();
    for (JMeterScriptReference ref : scripts) {
      list.add(ref.toJSon());
    }
    this.put("scripts", list);
  }
  
  public List<JMeterScriptReference> getScripts() {
    BasicDBList list = (BasicDBList) this.get("scripts");
    List<JMeterScriptReference> scripts = new ArrayList<JMeterScriptReference>();
    for (Object obj : list) {
      BasicDBObject dbObj = (BasicDBObject) obj;
      scripts.add(jmeterRefFactory.create(dbObj.getString("_id")));
    }
    return scripts;
  }

  @Override
  public Map<String, String> getRawDataOutput() {
    BasicDBList list = (BasicDBList) this.get("report");
    Map<String, String> reports = new HashMap<String, String>();
    for (Object obj : list) {
      BasicDBObject dbObj = (BasicDBObject) obj;
      reports.put(dbObj.getString("_id"), dbObj.getString("content"));
    }
    return reports;
  }

  @Override
  public Type getType() {
    return Type.Performance;
  }

}
