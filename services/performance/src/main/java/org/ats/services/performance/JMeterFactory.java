/**
 * 
 */
package org.ats.services.performance;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ats.common.StringUtil;
import org.ats.services.performance.JMeterSampler.Method;
import org.rythmengine.Rythm;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Oct 8, 2014
 */
public class JMeterFactory {

  public JMeterParser createJMeterParser(String source) throws Exception {
    return new JMeterParser(source);
  }
  
  public String createPom(String groupId, String artifactId) throws IOException {
    String template = StringUtil.readStream(Thread.currentThread().getContextClassLoader().getResourceAsStream("pom.xml"));
    if (groupId.indexOf(' ') != -1) groupId = groupId.replaceAll(" ", "-");
    if (artifactId.indexOf(' ') != -1) artifactId = artifactId.replaceAll(" ", "-");
    
    Map<String, Object> params = ParamBuilder.start().put("groupId", groupId).put("artifactId", artifactId).build();
    return Rythm.render(template, params);
  }
  
  public JMeterScript createJmeterScript(String testName, int loops, int numberThreads, int ramUp, boolean scheduler, int duration, JMeterSampler... samplers) {
    List<JMeterSampler> list = new ArrayList<JMeterSampler>();
    for (JMeterSampler sampler : samplers) {
      list.add(sampler);
    }
    return new JMeterScript(testName, loops, numberThreads, ramUp, scheduler, duration, list);
  }
  
  public String createArguments(JMeterArgument... arguments) throws IOException {
    String template = StringUtil.readStream(Thread.currentThread().getContextClassLoader().getResourceAsStream("arguments.xml"));
    StringBuilder sb = new StringBuilder();
    for (JMeterArgument argument : arguments) {
      sb.append(argument.transform()).append('\n');
    }
    return Rythm.render(template, sb.toString());
  }
  
  public JMeterArgument createArgument(String paramName, String paramValue) {
    return new JMeterArgument(paramName, paramValue);
  }
  
  public JMeterSampler createHttpGet(String name, String url, String assertionText, long contantTime, JMeterArgument... arguments) throws UnsupportedEncodingException {
    return createHttpRequest(Method.GET, name, url, assertionText, contantTime, arguments);
  }
  
  public JMeterSampler createHttpPost(String name, String url, String assertionText, long contantTime, JMeterArgument... arguments) throws UnsupportedEncodingException {
    return createHttpRequest(Method.POST, name, url, assertionText, contantTime, arguments);
  }
  
  public JMeterSampler createHttpRequest(Method method, String name, String url, String assertionText, long contantTime, JMeterArgument... arguments) throws UnsupportedEncodingException {
    List<JMeterArgument> list = new ArrayList<JMeterArgument>();
    for (JMeterArgument argument : arguments) {
      list.add(argument);
    }
    return new JMeterSampler(method, name, url, assertionText, contantTime, list);
  }
}
