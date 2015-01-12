/**
 * 
 */
package org.ats.jmeter.models;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import org.ats.common.http.HttpURL;
import org.ats.jmeter.JMeterFactory.Template;
import org.ats.jmeter.ParamBuilder;
import org.rythmengine.Rythm;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Oct 22, 2014
 */
public class JMeterSampler extends BasicDBObject {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  
  public JMeterSampler(Map<String, String> templates, Method method, String name, String url, String assertionText, long contantTime, JMeterArgument ... arguments) {
    this.put("method", method.toString());
    this.put("name", name);
    this.put("url", url);
    this.put("assertion_text", assertionText);
    this.put("contant_time", contantTime);
    this.put("templates", templates);
    this.put("arguments", arguments);
  }
  
  public JMeterSampler() {
    this(null, Method.GET, null, null, null, 0);
  }
  
  public JMeterSampler from(BasicDBObject obj) {
    this.put("method", obj.get("method"));
    this.put("name", obj.get("name"));
    this.put("url", obj.get("url"));
    this.put("assertion_text", obj.get("assertion_text"));
    this.put("contant_time", obj.get("contant_time"));
    this.put("templates", obj.get("templates"));
    this.put("arguments", obj.get("arguments"));
    return this;
  }
  
  public Map<String, String> getTemplates() {
    return Collections.unmodifiableMap((Map<String, String>)this.get("templates"));
  }
  
  public Method getMethod() {
    return Method.valueOf(this.getString("method"));
  }
  
  public void setMethod(Method method) {
    this.put("method", method.toString());
  }

  public String getName() {
    return this.getString("name");
  }

  public void setName(String name) {
    this.put("name", name);
  }

  public String getUrl() {
    return this.getString("url");
  }

  public void setUrl(String url) {
    this.put("url", url);
  }

  public String getAssertionText() {
    return this.getString("assertion_text");
  }

  public void setAssertionText(String assertionText) {
    this.put("assertion_text", assertionText);
  }

  public long getContantTime() {
    return this.getLong("contant_time");
  }

  public void setContantTime(long contantTime) {
    this.put("contant_time", contantTime);
  }

  public JMeterArgument[] getArguments() {
    Object obj = this.get("arguments");
    if (obj instanceof BasicDBList) {
      BasicDBList list = (BasicDBList) obj;
      JMeterArgument[] arguments = new JMeterArgument[list.size()];
      for(int i = 0; i < list.size(); i++) {
        arguments[i] = new JMeterArgument().from((BasicDBObject) list.get(i));
      }
      return arguments;
    } else if (obj instanceof JMeterArgument[]) {
      return (JMeterArgument[]) obj;
    }
    
    return null;
  }

  public JMeterSampler addArgument(JMeterArgument argument) {
    JMeterArgument[] current = getArguments();
    JMeterArgument[]  newArray = Arrays.copyOf(current, current.length + 1);
    newArray[current.length] = argument;
    this.put("arguments", newArray);
    return this;
  }
  
  public String createArguments() {
    StringBuilder sb = new StringBuilder();
    for (JMeterArgument argument : getArguments()) {
      sb.append(argument.toString()).append('\n');
    }
    return Rythm.render(getTemplates().get(Template.ARGUMENTS.toString()), sb.toString());
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj == this) return true;
    
    if(obj instanceof JMeterSampler) {
      JMeterSampler that = (JMeterSampler) obj;
      return  this.getName().equals(that.getName())
          && this.getUrl().equals(that.getUrl())
          && this.getTemplates().equals(that.getTemplates())
          && this.getMethod().equals(that.getMethod())
          && this.getContantTime() == that.getContantTime()
          && this.getAssertionText().equals(that.getAssertionText())
          && Arrays.equals(this.getArguments(), that.getArguments());
    }
    return false;
  }
  
  @Override
  public String toString() {
    HttpURL httpUrl = null;
    try {
      httpUrl = new HttpURL(getUrl());
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
    ParamBuilder builder = ParamBuilder.start()
        .put("name", getName())
        .put("host", httpUrl.getHost())
        .put("port", httpUrl.getPort())
        .put("protocol", httpUrl.getProtocol())
        .put("path", httpUrl.getPath());
    
    if (!httpUrl.getQueryParameters().isEmpty()) {
      for (Map.Entry<String, String> entry : httpUrl.getQueryParameters().entrySet()) {
        addArgument(new JMeterArgument(getTemplates(), entry.getKey(), entry.getValue()));
      }
    }
    String s = createArguments();
    builder.put("arguments", s);
    
    if (getAssertionText() != null && !getAssertionText().trim().isEmpty()) builder.put("assertionText", Rythm.render(getTemplates().get(Template.ASSERTION_TEXT.toString()), getAssertionText()));
    
    if (getContantTime() > 0) builder.put("contantTime", Rythm.render(getTemplates().get(Template.CONTANT_TIME.toString()), getContantTime()));
    
    switch (getMethod()) {
    case GET:
      return Rythm.render(getTemplates().get(Template.SAMPLE_GET.toString()), builder.build());
    case POST:
    case PUT:
    case DELETE:
      builder.put("method", getMethod());
      return Rythm.render(getTemplates().get(Template.SAMPLE_POST.toString()), builder.build());
    default:
      return null;
    }
  }

  public static enum Method {
    POST, GET, PUT, DELETE
  }
}
