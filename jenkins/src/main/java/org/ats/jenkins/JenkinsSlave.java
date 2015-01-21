/**
 * 
 */
package org.ats.jenkins;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
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
import org.ats.common.http.HttpClientFactory;
import org.ats.common.http.HttpClientUtil;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Apr 18, 2014
 */
public class JenkinsSlave {

  /** .*/
  private String slaveAddress;
  
  /** .*/
  private JenkinsMaster master;
  
  /** .*/
  private Map<String, String> environment;
  
  public JenkinsSlave(JenkinsMaster master, String slaveAddress) {
    this(master, slaveAddress, null);
  }
  
  public JenkinsSlave(JenkinsMaster master, String slaveAddress, Map<String, String> environment) {
    this.slaveAddress = slaveAddress;
    this.master = master;
    this.environment = environment;
  }
  
  private HttpEntity buildFormData() throws IOException {
    List<NameValuePair> list = new ArrayList<NameValuePair>();
    BufferedReader reader = new BufferedReader(new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream("jenkins-slave-template")));
    String line = null;
    while ((line = reader.readLine()) != null) {
      String[] arrays = line.split("\t");
      if (arrays.length == 2) {
        String name = arrays[0].trim();
        String value = arrays[1].trim();
        list.add(new BasicNameValuePair(name, value));
      }
    }
    list.add(new BasicNameValuePair("_.host", slaveAddress));
    list.add(new BasicNameValuePair("name", slaveAddress));
    
    if(environment != null) {
      for(Map.Entry<String, String> entry : environment.entrySet()) {
        list.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
      }
    }
    
    BufferedInputStream is = new BufferedInputStream(Thread.currentThread().getContextClassLoader().getResourceAsStream("jenkins-slave-json-template"));
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    byte[] buff = new byte[1024];
    for (int i = is.read(buff); i != -1; i = is.read(buff)) {
      bos.write(buff, 0, i);
    }
    String json = new String(bos.toByteArray());
    json = String.format(json, slaveAddress, slaveAddress);
    
    if (environment != null) {
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
  
  public boolean join() throws Exception {
    CloseableHttpClient client = HttpClientFactory.getInstance();
    HttpContext httpContext = new BasicHttpContext();
    
    HttpPost post = new HttpPost(master.buildURL("computer/doCreateItem"));
    post.setEntity(this.buildFormData());
    
    HttpResponse res = client.execute(post, httpContext);
    
    String body = HttpClientUtil.getContentBodyAsString(res);
    if (body.length() == 0) {
      //check status in 30 seconds
      long start = System.currentTimeMillis();
      while(true) {
        try {
          body = HttpClientUtil.fetch(client, master.buildURL(new StringBuilder("computer/").append(this.slaveAddress).append("/api/json").toString()));
          JSONObject json = new JSONObject(body);
          boolean offline = json.getBoolean("offline");
          System.out.println("Jenkins Slave offline status is: " + offline);
          if (!offline)
            return true;
          else if (System.currentTimeMillis() - start < 30 * 1000)
            continue;
          else 
            return false;
        } catch (Exception e) {
          e.printStackTrace();
          if (System.currentTimeMillis() - start > 10 * 1000) return false;
        } finally {
          Thread.sleep(3000);
        }
      }
    }
    
    //
    return false;
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

