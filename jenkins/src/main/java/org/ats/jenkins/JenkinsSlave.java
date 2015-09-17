/**
 * 
 */
package org.ats.jenkins;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.ats.common.MapBuilder;
import org.ats.common.StringUtil;
import org.ats.common.http.HttpClientFactory;
import org.ats.common.http.HttpClientUtil;
import org.json.JSONArray;
import org.json.JSONObject;
import org.rythmengine.Rythm;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Apr 18, 2014
 */
public class JenkinsSlave {

  /** .*/
  private String slaveAddress;
  
  /** .*/
  private String credentialId;
  
  /** .*/
  private JenkinsMaster master;
  
  /** .*/
  private Map<String, String> environment;
  
  /** .*/
  private String slaveTmpl, slaveJsonTmpl;
  
  public JenkinsSlave(JenkinsMaster master, String slaveAddress) throws IOException {
    this(master, slaveAddress, System.getProperty("jenkins.slave.credential"));
  }
  
  public JenkinsSlave(JenkinsMaster master, String slaveAddress, String credentialId) throws IOException {
    this(master, slaveAddress, new HashMap<String, String>(), credentialId);
  }
  
  public JenkinsSlave(JenkinsMaster master, String slaveAddress, Map<String, String> environment) throws IOException {
    this(master, slaveAddress, environment, System.getProperty("jenkins.slave.credential"));
  }
  
  public JenkinsSlave(JenkinsMaster master, String slaveAddress, Map<String, String> environment, String credentialId) throws IOException {
    this.slaveAddress = slaveAddress;
    this.master = master;
    this.environment = environment;
    this.credentialId = credentialId;
    
    String slaveTmpl = StringUtil.readStream(Thread.currentThread().getContextClassLoader().getResourceAsStream("jenkins-slave-template"));
    this.slaveTmpl = Rythm.render(slaveTmpl, new MapBuilder<String, String>("credential", credentialId).build());
    
    String slaveJsonTmpl = StringUtil.readStream(Thread.currentThread().getContextClassLoader().getResourceAsStream("jenkins-slave-json-template"));
    this.slaveJsonTmpl = Rythm.render(slaveJsonTmpl, new MapBuilder<String, String>("name", slaveAddress).append("credential", credentialId).build());
  }
  
  public String getCredential() {
    return credentialId;
  }
  
  public String getSlaveTemplate() {
    return slaveTmpl;
  }
  
  public String getSlaveJsonTemplate() {
    return slaveJsonTmpl;
  }
  
  private HttpEntity buildFormData() throws IOException {
    List<NameValuePair> list = new ArrayList<NameValuePair>();
    
    BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(slaveTmpl.getBytes())));
    String line = null;
    while ((line = reader.readLine()) != null) {
      String[] arrays = line.split(" ");
      if (arrays.length == 2) {
        String name = arrays[0].trim();
        String value = arrays[1].trim();
        list.add(new BasicNameValuePair(name, value));
      }
    }
    list.add(new BasicNameValuePair("_.host", slaveAddress));
    list.add(new BasicNameValuePair("name", slaveAddress));
    
    if(environment != null && !environment.isEmpty()) {
      for(Map.Entry<String, String> entry : environment.entrySet()) {
        list.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
      }
    }
    
    String json = slaveJsonTmpl;
    
    if (environment != null && !environment.isEmpty()) {
      JSONObject jsonObj = new JSONObject(json);
      JSONArray env = new JSONArray();
      for(Map.Entry<String, String> entry : environment.entrySet()) {
        env.put(new JSONObject().put("key", entry.getKey()).put("value", entry.getValue()));
      }
      jsonObj.getJSONObject("nodeProperties").put("hudson-slaves-EnvironmentVariablesNodeProperty", new JSONObject().put("env", env));
      json = jsonObj.toString();
    }
    
    list.add(new BasicNameValuePair("json", json));
    return new UrlEncodedFormEntity(list);
  }
  
  public boolean isOffline() throws IOException {
    CloseableHttpClient client = HttpClientFactory.getInstance();
    
    String body = HttpClientUtil.fetch(client, master.buildURL(new StringBuilder("computer/").append(this.slaveAddress).append("/api/json").toString()));
    JSONObject json = new JSONObject(body);
    return json.getBoolean("offline");
  }
  
  /**
   * The default timeout is 3 minutes
   * @return
   * @throws Exception
   */
  public boolean join() throws Exception {
    return this.join(3 * 60 * 1000);
  }
  
  public boolean join(long timeout) throws Exception {
    CloseableHttpClient client = HttpClientFactory.getInstance();
    HttpContext httpContext = new BasicHttpContext();
    
    HttpPost post = new HttpPost(master.buildURL("computer/doCreateItem"));
    post.setEntity(this.buildFormData());
    
    HttpResponse res = client.execute(post, httpContext);
    
    String body = HttpClientUtil.getContentBodyAsString(res);
    if (body.length() == 0) {
      long start = System.currentTimeMillis();
      return waitJenkinsSlaveJoinUntil(start, timeout, master.buildURL(new StringBuilder("computer/").append(this.slaveAddress).append("/api/json").toString()));
    }
    
    //
    return false;
  }
  
  private boolean waitJenkinsSlaveJoinUntil(long startTime, long timeout, String fetchUrl) throws IOException, InterruptedException {
    String body = HttpClientUtil.fetch(HttpClientFactory.getInstance(), fetchUrl);
    JSONObject json = new JSONObject(body);
    boolean offline = json.getBoolean("offline");
    System.out.println("Jenkins slave offline status is: " + offline);
    
    if (offline) {
      if (System.currentTimeMillis() - startTime > timeout) {
        return false;
      } else {
        Thread.sleep(15 * 1000);
        return waitJenkinsSlaveJoinUntil(startTime, timeout, fetchUrl);
      }
    }
    
    return !offline;
  }
  
  public boolean release() throws IOException {
    
    if (!master.listSlaves().contains(this.slaveAddress)) return true;
    
    String url = master.buildURL(new StringBuilder("computer/").append(slaveAddress).append("/doDelete").toString());
    CloseableHttpClient client = HttpClientFactory.getInstance();
    HttpContext httpContext = new BasicHttpContext();
    HttpPost post = new HttpPost(url);
    
    List<NameValuePair> params = new ArrayList<NameValuePair>();
    params.add(new BasicNameValuePair("Submit", "yes"));
    params.add(new BasicNameValuePair("json", "{}"));
    post.setEntity(new UrlEncodedFormEntity(params));
    
    HttpResponse res = client.execute(post, httpContext);
    String body = HttpClientUtil.getContentBodyAsString(res);
    return body.length() == 0;
  }
  
  public String getSlaveAddress() {
    return slaveAddress;
  }
  
  public Map<String, String> getEnvironment() {
    return environment == null ? Collections.<String, String>emptyMap() : Collections.unmodifiableMap(environment);
  }
}

