/**
 * 
 */
package org.ats.services.performance;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.ats.common.StringUtil;
import org.ats.common.http.HttpURL;
import org.rythmengine.Rythm;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Oct 22, 2014
 */
public class JMeterSampler extends BasicDBObject {

  /** .*/
  private static final long serialVersionUID = 1L;
  
  private List<JMeterArgument> arguments = new ArrayList<JMeterArgument>();
  
  JMeterSampler(Method method, String name, String url, String assertionText, Long constantTime, List<JMeterArgument> arguments) {
    this.put("method", method.toString());
    this.put("name", name);
    this.put("url", url);
    this.put("assertion_text", assertionText);
    this.put("constant_time", constantTime);
    
    this.arguments.addAll(arguments);
    BasicDBList list = new BasicDBList();
    for (JMeterArgument argument : this.arguments) {
      list.add(argument);
    }
    this.put("arguments", list);
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

  public long getConstantTime() {
    return this.getLong("constant_time");
  }

  public void setConstantTime(long contantTime) {
    this.put("constant_time", contantTime);
  }

  public List<JMeterArgument> getArguments() {
    return Collections.unmodifiableList(this.arguments);
  }

  public void addArgument(JMeterArgument... arguments) {
    for (JMeterArgument argument : arguments) {
      this.arguments.add(argument);
    }
    BasicDBList list = new BasicDBList();
    for (JMeterArgument argument : this.arguments) {
      list.add(argument);
    }
    this.put("arguments", list);
  }
  
  public String createArguments() throws IOException {
    String template = StringUtil.readStream(Thread.currentThread().getContextClassLoader().getResourceAsStream("arguments.xml"));
    StringBuilder sb = new StringBuilder();
    for (JMeterArgument argument : getArguments()) {
      sb.append(argument.transform()).append('\n');
    }
    return Rythm.render(template, sb.toString());
  }
  
  public String transform() throws IOException {
    HttpURL httpUrl = new HttpURL(getUrl());
    ParamBuilder builder = ParamBuilder.start()
        .put("name", getName())
        .put("host", httpUrl.getHost())
        .put("port", httpUrl.getPort())
        .put("protocol", httpUrl.getProtocol())
        .put("path", getMethod() == Method.GET ? httpUrl.getPath() : httpUrl.getFullPath());
    
    if (!httpUrl.getQueryParameters().isEmpty()) {
      for (Map.Entry<String, String> entry : httpUrl.getQueryParameters().entrySet()) {
        addArgument(new JMeterArgument(entry.getKey(), entry.getValue()));
      }
    }
    String s = createArguments();
    builder.put("arguments", s);
    
    if (getAssertionText() != null && !getAssertionText().trim().isEmpty()) {
      String template = StringUtil.readStream(Thread.currentThread().getContextClassLoader().getResourceAsStream("assertion-text.xml"));
      builder.put("assertionText", Rythm.render(template, getAssertionText()));
    }
    
    if (getConstantTime() > 0) {
      String template = StringUtil.readStream(Thread.currentThread().getContextClassLoader().getResourceAsStream("contant-time.xml"));
      builder.put("contantTime", Rythm.render(template, getConstantTime()));
    }
    
    String getTemplate = StringUtil.readStream(Thread.currentThread().getContextClassLoader().getResourceAsStream("sample-get.xml"));
    
    String postTemplate = StringUtil.readStream(Thread.currentThread().getContextClassLoader().getResourceAsStream("sample-post.xml"));
    
    switch (getMethod()) {
    case GET:
      return Rythm.render(getTemplate, builder.build());
    case POST:
    case PUT:
    case DELETE:
      builder.put("method", getMethod());
      return Rythm.render(postTemplate, builder.build());
    default:
      return null;
    }
  }

  public static enum Method {
    POST, GET, PUT, DELETE
  }
}
