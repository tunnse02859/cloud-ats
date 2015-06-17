/**
 * 
 */
package org.ats.services.performance;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import org.ats.common.StringUtil;
import org.rythmengine.Rythm;

import com.mongodb.BasicDBObject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Oct 22, 2014
 */
public class JMeterArgument extends BasicDBObject {

  /** .*/
  private static final long serialVersionUID = 1L;
  
  JMeterArgument(String paramName, String paramValue) {
    this.put("paramName", paramName);
    this.put("paramValue", paramValue);
  }
  
  public JMeterArgument from(BasicDBObject obj) {
    this.put("paramName", obj.get("paramName"));
    this.put("paramValue", obj.get("paramValue"));
    return this;
  }
  
  @SuppressWarnings("unchecked")
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

  public String transform() throws IOException {
    String template = StringUtil.readStream(Thread.currentThread().getContextClassLoader().getResourceAsStream("argument.xml"));
    Map<String, Object> params = ParamBuilder.start().put("paramName", getParamName()).put("paramValue", getParamValue()).build();
    return Rythm.render(template, params);
  }
}
