/**
 * 
 */
package org.ats.jmeter.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.ats.jmeter.JMeterFactory.Template;
import org.ats.jmeter.ParamBuilder;
import org.rythmengine.Rythm;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Oct 22, 2014
 */
public class JMeterScript implements Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public JMeterScript() {}
  
  /** .*/
  private String testName;
  
  /** .*/
  private int loops;
  
  /** .*/
  private int numberThreads;
  
  /** .*/
  private int ramUp;
  
  /** .*/
  private boolean scheduler;
  
  /** .*/
  private int duration;
  
  /** .*/
  private List<JMeterSampler> samplers;
  
  private Map<Template, String> templates;
  
  public JMeterScript(Map<Template, String> templates, String testName, int loops, int numberThreads, int ramUp, boolean scheduler, int duration, JMeterSampler ... samplers) {
    this.testName = testName;
    this.loops = loops;
    this.numberThreads = numberThreads;
    this.ramUp = ramUp;
    this.scheduler = scheduler;
    this.duration = duration;
    
    this.templates = templates;
    
    if (samplers != null && samplers.length != 0) {
      this.samplers = Arrays.asList(samplers);
    } else {
      this.samplers = new ArrayList<JMeterSampler>();
    }
  }
  
  public Map<Template, String> getTemplates() {
    return Collections.unmodifiableMap(this.templates);
  }
  
  public void setName(String testName) {
    this.testName = testName;
  }
  
  public String getName() {
    return this.testName;
  }

  public int getLoops() {
    return loops;
  }

  public void setLoops(int loops) {
    this.loops = loops;
  }

  public int getNumberThreads() {
    return numberThreads;
  }

  public void setNumberThreads(int numberThreads) {
    this.numberThreads = numberThreads;
  }

  public int getRamUp() {
    return ramUp;
  }

  public void setRamUp(int ramUp) {
    this.ramUp = ramUp;
  }

  public boolean isScheduler() {
    return scheduler;
  }

  public void setScheduler(boolean scheduler) {
    this.scheduler = scheduler;
  }

  public int getDuration() {
    return duration;
  }

  public void setDuration(int duration) {
    this.duration = duration;
  }

  public List<JMeterSampler> getSamplers() {
    return Collections.unmodifiableList(samplers);
  }

  public List<JMeterSampler> addSampler(JMeterSampler sampler) {
    this.samplers.add(sampler);
    return this.samplers;
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof JMeterScript) {
      JMeterScript that = (JMeterScript) obj;
      return this.toString().equals(that.toString());
    }
    return false;
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (JMeterSampler sampler : samplers) {
      sb.append(sampler.toString()).append('\n');
    }
    
    ParamBuilder params = ParamBuilder.start()
      .put("name", testName)
      .put("loops", loops)
      .put("numberThreads", numberThreads)
      .put("ramUp", ramUp)
      .put("scheduler", scheduler)
      .put("duration", duration)
      .put("samplers", sb.toString());
    
    return Rythm.render(this.templates.get(Template.JMETER), params.build());
  }
  
}
