/**
 * 
 */
package org.ats.jmeter.models;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.ats.common.http.HttpURL;
import org.ats.jmeter.JMeterFactory.Template;
import org.ats.jmeter.ParamBuilder;
import org.rythmengine.Rythm;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Oct 22, 2014
 */
public class JMeterSampler implements Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  
  /** 
   * Default contructor for serializable.
   *  
   *  */
  public JMeterSampler() {}
  
  /** .*/
  private Method method;
  
  /** .*/
  private String name;
  
  /** .*/
  private String url;
  
  /** .*/
  private String assertionText;
  
  /** .*/
  private long contantTime;
  
  /** .*/
  private List<JMeterArgument> arguments;
  
  /** .*/
  private Map<Template, String> templates;
  
  public JMeterSampler(Map<Template, String> templates, Method method, String name, String url, String assertionText, long contantTime, JMeterArgument ... arguments) {
    this.method = method;
    this.name = name;
    this.url = url;
    this.assertionText = assertionText;
    this.contantTime = contantTime;
    
    this.templates = templates;
    
    if (arguments != null && arguments.length != 0) {
      this.arguments = Arrays.asList(arguments);
    } else {
      this.arguments = new ArrayList<JMeterArgument>();
    }
  }
  
  public Map<Template, String> getTemplates() {
    return Collections.unmodifiableMap(this.templates);
  }
  
  public Method getMethod() {
    return method;
  }
  
  public void setMethod(Method method) {
    this.method = method;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getAssertionText() {
    return assertionText;
  }

  public void setAssertionText(String assertionText) {
    this.assertionText = assertionText;
  }

  public long getContantTime() {
    return contantTime;
  }

  public void setContantTime(long contantTime) {
    this.contantTime = contantTime;
  }

  public List<JMeterArgument> getArguments() {
    return Collections.unmodifiableList(arguments);
  }

  public List<JMeterArgument> addArgument(JMeterArgument argument) {
    this.arguments.add(argument);
    return this.arguments;
  }
  
  public String createArguments() {
    StringBuilder sb = new StringBuilder();
    for (JMeterArgument argument : arguments) {
      sb.append(argument.toString()).append('\n');
    }
    return Rythm.render(this.templates.get(Template.ARGUMENTS), sb.toString());
  }
  
  @Override
  public boolean equals(Object obj) {
    if(obj instanceof JMeterSampler) {
      JMeterSampler that = (JMeterSampler) obj;
      return this.toString().equals(that.toString());
    }
    return false;
  }
  
  @Override
  public String toString() {
    HttpURL httpUrl = null;
    try {
      httpUrl = new HttpURL(url);
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
    ParamBuilder builder = ParamBuilder.start()
        .put("name", name)
        .put("host", httpUrl.getHost())
        .put("port", httpUrl.getPort())
        .put("protocol", httpUrl.getProtocol())
        .put("path", httpUrl.getPath());
    
    if (!httpUrl.getQueryParameters().isEmpty()) {
      for (Map.Entry<String, String> entry : httpUrl.getQueryParameters().entrySet()) {
        arguments.add(new JMeterArgument(this.templates, entry.getKey(), entry.getValue()));
      }
    }
    String s = createArguments();
    builder.put("arguments", s);
    
    if (assertionText != null && !assertionText.trim().isEmpty()) builder.put("assertionText", Rythm.render(this.templates.get(Template.ASSERTION_TEXT), assertionText));
    
    if (contantTime > 0) builder.put("contantTime", Rythm.render(this.templates.get(Template.CONTANT_TIME), contantTime));
    
    switch (method) {
    case GET:
      return Rythm.render(this.templates.get(Template.SAMPLE_GET), builder.build());
    case POST:
    case PUT:
    case DELETE:
      builder.put("method", method);
      return Rythm.render(this.templates.get(Template.SAMPLE_POST), builder.build());
    default:
      return null;
    }
  }

  public static enum Method {
    POST, GET, PUT, DELETE
  }
}
