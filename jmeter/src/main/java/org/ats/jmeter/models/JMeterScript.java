/**
 * 
 */
package org.ats.jmeter.models;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import org.ats.jmeter.JMeterFactory.Template;
import org.ats.jmeter.ParamBuilder;
import org.rythmengine.Rythm;

import com.mongodb.BasicDBObject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Oct 22, 2014
 */
public class JMeterScript extends BasicDBObject {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public JMeterScript(Map<String, String> templates, String testName, int loops, int numberThreads, int ramUp, boolean scheduler, int duration, JMeterSampler ... samplers) {
    this.put("name", testName);
    this.put("loops", loops);
    this.put("number_threads", numberThreads);
    this.put("ram_up", ramUp);
    this.put("scheduler", scheduler);
    this.put("duration", duration);
    this.put("templates", templates);
    this.put("samplers", samplers);
  }
  
  public Map<String, String> getTemplates() {
    return Collections.unmodifiableMap((Map<String, String>)this.get("templates"));
  }
  
  public void setName(String testName) {
    this.put("name", testName);
  }
  
  public String getName() {
    return this.getString("name");
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

  public JMeterSampler[] getSamplers() {
    return (JMeterSampler[]) this.get("samplers");
  }

  public JMeterScript addSampler(JMeterSampler sampler) {
    JMeterSampler[] current = getSamplers();
    JMeterSampler[]  newArray = Arrays.copyOf(current, current.length + 1);
    newArray[current.length] = sampler;
    this.put("samplers", newArray);
    return this;
  }
  
  @Override
  public boolean equals(Object obj) {
    if(obj == this) return true;
    
    if (obj instanceof JMeterScript) {
      JMeterScript that = (JMeterScript) obj;
      return this.getName().equals(that.getName())
          && this.getTemplates().equals(that.getTemplates())
          && this.getNumberThreads() == that.getNumberThreads()
          && this.isScheduler() == that.isScheduler()
          && this.getLoops() == that.getLoops()
          && this.getRamUp() == that.getRamUp()
          && this.getDuration() == that.getDuration()
          && Arrays.equals(this.getSamplers(), that.getSamplers());
    }
    return false;
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (JMeterSampler sampler : getSamplers()) {
      sb.append(sampler.toString()).append('\n');
    }
    
    ParamBuilder params = ParamBuilder.start()
      .put("name", getName())
      .put("loops", getLoops())
      .put("numberThreads", getNumberThreads())
      .put("ramUp", getRamUp())
      .put("scheduler", isScheduler())
      .put("duration", getDuration())
      .put("samplers", sb.toString());
    
    return Rythm.render(getTemplates().get(Template.JMETER.toString()), params.build());
  }
  
}
