/**
 * 
 */
package org.ats.jmeter.models;

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
public class JMeterArgument extends BasicDBObject {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  
  public JMeterArgument(Map<String, String> templates, String paramName, String paramValue) {
    this.put("paramName", paramName);
    this.put("paramValue", paramValue);
    this.put("templates", templates);
  }
  
  public Map<String, String> getTemplates() {
    return Collections.unmodifiableMap((Map<String, String>)this.get("templates"));
  }

  public String getParamName() {
    return this.getString("paramName");
  }

  public void setParamName(String paramName) {
    this.put("paramName", paramName);
  }

  public String getParamValue() {
    return this.getString("paramValue");
  }

  public void setParamValue(String paramValue) {
    this.put("paramValue", paramValue);
  }
  
  public boolean equals(Object obj) {
    if (obj == this) return true;
    
    if (obj instanceof JMeterArgument) {
      JMeterArgument that = (JMeterArgument) obj;
      return this.getParamName().equals(that.getParamName()) && this.getParamValue().equals(that.getParamValue()) && this.getTemplates().equals(that.getTemplates());
    }
    return false;
  }
  
  @Override
  public String toString() {
    Map<String, Object> params = ParamBuilder.start().put("paramName", getParamName()).put("paramValue", getParamValue()).build();
    return Rythm.render(getTemplates().get(Template.ARGUMENT.toString()), params);
  }
}
