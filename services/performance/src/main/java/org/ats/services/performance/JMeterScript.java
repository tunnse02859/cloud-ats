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

  JMeterScript(String testName, int loops, int numberThreads, int ramUp, boolean scheduler, int duration, List<JMeterSampler>  samplers) {
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
  
//  public JMeterScript from(DBObject source) {
//    this.put("_id", source.get("_id"));
//    this.put("name", source.get("name"));
//    this.put("loops", source.get("loops"));
//    this.put("number_threads", source.get("number_threads"));
//    this.put("ram_up", source.get("ram_up"));
//    this.put("scheduler", source.get("scheduler"));
//    this.put("duration", source.get("duration"));
//    this.put("templates", source.get("templates"));
//    this.put("samplers", source.get("samplers"));
//    
//    return this;
//  }
}
