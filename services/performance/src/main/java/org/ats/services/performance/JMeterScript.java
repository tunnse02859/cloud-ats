/**
 * 
 */
package org.ats.services.performance;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.ats.common.StringUtil;
import org.rythmengine.Rythm;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Oct 22, 2014
 */
public class JMeterScript extends BasicDBObject {

  /** . */
  private static final long serialVersionUID = 1L;
  
  /** .*/
  private List<JMeterSampler> samplers = new ArrayList<JMeterSampler>();
  
  /**
   * The constructor for raw jmeter script upload
   * @param project_id the project id
   */
  JMeterScript(String project_id, String name, String raw_content) {
    this.put("_id", UUID.randomUUID().toString());
    this.put("project_id", project_id);
    this.put("name", name);
    this.put("raw", true);
    this.put("raw_content", raw_content);
  }

  JMeterScript(String testName, int loops, int numberThreads, int ramUp, boolean scheduler, int duration, String project_id, List<JMeterSampler>  samplers) {
    this.put("_id", UUID.randomUUID().toString());
    this.put("name", testName);
    this.put("loops", loops);
    this.put("number_threads", numberThreads);
    this.put("ram_up", ramUp);
    this.put("scheduler", scheduler);
    this.put("duration", duration);
    this.samplers.addAll(samplers);
    
    BasicDBList list = new BasicDBList();
    for (JMeterSampler sampler : samplers) {
      list.add(sampler);
    }
    
    this.put("samplers", list);
    this.put("project_id", project_id);
    this.put("raw", false);
  }

  public boolean isRaw() {
    return this.getBoolean("raw");
  }
  
  public String getId() {
    return this.getString("_id");
  }
  
  public String getProjectId () {
    return this.getString("project_id");
  }
  
  public void setProjectId(String projectId) {
    this.put("project_id", projectId);
  }
  
  public String getName() {
    return this.getString("name");
  }

  public void setName(String testName) {
    this.put("name", testName);
  }
  
  public int getLoops() {
    return this.getInt("loops");
  }

  public void setLoops(int loops) {
    this.put("loops", loops);
  }

  public int getNumberThreads() {
    return this.getInt("number_threads");
  }

  public void setNumberThreads(int numberThreads) {
    this.put("number_threads", numberThreads);
  }

  public int getRamUp() {
    return this.getInt("ram_up");
  }

  public void setRamUp(int ramUp) {
    this.put("ram_up", ramUp);
  }

  public boolean isScheduler() {
    return this.getBoolean("scheduler");
  }

  public void setScheduler(boolean scheduler) {
    this.put("scheduler", scheduler);
  }

  public int getDuration() {
    return this.getInt("duration");
  }

  public void setDuration(int duration) {
    this.put("duration", duration);
    
  }
  
  public void setNumberEngines(int engines) {
    this.put("number_engines", engines);
  }
  
  public int getNumberEngines() {
    return this.get("number_engines") != null ? this.getInt("number_engines") : 1;
  }
  
  public List<CSV> getCSVFiles() {
    
    Object obj = this.get("csv_files");
    if (obj == null) return Collections.emptyList();
    
    BasicDBList list = (obj == null ? new BasicDBList() : (BasicDBList) obj);
    
    List<CSV> listFiles = new ArrayList<CSV>();
    for (int i = 0; i < list.size(); i ++) {
      String id = ((BasicDBObject) list.get(i)).getString("_id");
      String name = ((BasicDBObject) list.get(i)).getString("name");
      CSV csv = new CSV(id, name);
      
      listFiles.add(csv);
    }
    
    return listFiles;
  }
  
  public void addCSVFiles(CSV... csvs) {
    Object obj = this.get("csv_files");
    
    BasicDBList list = obj == null ? new BasicDBList() : (BasicDBList) obj;
    
    for (CSV csv : csvs) {
      list.add(csv);
    }
    
    this.put("csv_files", list);
  }
  
  public void removeCSVFile(CSV csv) {
    
    Object obj = this.get("csv_files");
    if (obj == null) return;
    
    BasicDBList list = (BasicDBList) obj;
    list.remove(csv);
    
    this.put("csv_files", list);
  }
  
  public List<JMeterSampler> getSamplers() {
    return Collections.unmodifiableList(samplers);
  }

  public void addSampler(JMeterSampler... samplers) {
    for (JMeterSampler sampler : samplers) {
      this.samplers.add(sampler);
    }
    BasicDBList list = new BasicDBList();
    for (JMeterSampler sampler : this.samplers) {
      list.add(sampler);
    }
    this.put("samplers", list);
  }
  
  public String transform() throws IOException {
    String template = StringUtil.readStream(Thread.currentThread().getContextClassLoader().getResourceAsStream("jmeter.xml"));
    StringBuilder sb = new StringBuilder();
    for (JMeterSampler sampler : getSamplers()) {
      sb.append(sampler.transform()).append('\n');
    }
    
    ParamBuilder params = ParamBuilder.start()
      .put("name", getName())
      .put("loops", getLoops())
      .put("numberThreads", getNumberThreads())
      .put("ramUp", getRamUp())
      .put("scheduler", isScheduler())
      .put("duration", getDuration())
      .put("samplers", sb.toString());
    
    return Rythm.render(template, params.build());
  }
}
