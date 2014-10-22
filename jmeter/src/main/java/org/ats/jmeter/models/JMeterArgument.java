/**
 * 
 */
package org.ats.jmeter.models;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

import org.ats.jmeter.ParamBuilder;
import org.ats.jmeter.JMeterFactory.Template;
import org.rythmengine.Rythm;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Oct 22, 2014
 */
public class JMeterArgument implements Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  
  public JMeterArgument() {}
  
  /** .*/
  private String paramName;
  
  /** .*/
  private String paramValue;
  
  /** .*/
  private Map<Template, String> templates;
  
  public JMeterArgument(Map<Template, String> templates, String paramName, String paramValue) {
    this.paramName = paramName;
    this.paramValue = paramValue;
    this.templates = templates;
  }
  
  public Map<Template, String> getTemplates() {
    return Collections.unmodifiableMap(this.templates);
  }

  public String getParamName() {
    return paramName;
  }

  public void setParamName(String paramName) {
    this.paramName = paramName;
  }

  public String getParamValue() {
    return paramValue;
  }

  public void setParamValue(String paramValue) {
    this.paramValue = paramValue;
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof JMeterArgument) {
      JMeterArgument that = (JMeterArgument) obj;
      return this.toString().equals(that.toString());
    }
    return false;
  }
  
  @Override
  public String toString() {
    Map<String, Object> params = ParamBuilder.start().put("paramName", paramName).put("paramValue", paramValue).build();
    return Rythm.render(this.templates.get(Template.ARGUMENT), params);
  }
}
