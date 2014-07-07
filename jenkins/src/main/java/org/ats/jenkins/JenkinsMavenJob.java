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
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.ats.common.http.HttpClientFactory;
import org.ats.common.http.HttpClientUtil;
import org.json.JSONObject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Apr 26, 2014
 */
public class JenkinsMavenJob {
  
  /** .*/
  private String name;
  
  /** .*/
  private String assigned;
  
  /** .*/
  private String gitURL;
  
  /** .*/
  private String goals;
  
  /** .*/
  private String mavenOpts;
  
  /** .*/
  JenkinsMaster master;
  
  /**
   * 
   * @param name the job name
   * @param assigned the slave label
   * @param gitURL the http url of git project
   * @param goals empty string for not specified.
   * @param mavenOpts empty string for not specified.
   */
  public JenkinsMavenJob(JenkinsMaster master, String name, String assigned, String gitURL, String goals, String mavenOpts) {
    this.name = name;
    this.assigned = assigned;
    this.gitURL = gitURL;
    this.goals = goals;
    this.mavenOpts = mavenOpts;
    this.master = master;
  }
  
  public byte[] getConsoleOutput(int buildNumber, int start) throws IOException {
    String url = master.buildURL("job/" + name + "/" + buildNumber + "/logText/progressiveHtml");
    DefaultHttpClient client = HttpClientFactory.getInstance();
    HttpContext httpContext = new BasicHttpContext();
    HttpPost post = new HttpPost(url);
    List<NameValuePair> list = new ArrayList<NameValuePair>();
    list.add(new BasicNameValuePair("start", String.valueOf(start)));
    HttpResponse res = client.execute(post, httpContext);
    return HttpClientUtil.getContentBodyAsByteArray(res);
  }
  
  public boolean isBuilding(int buildNumber)  {
    String url = master.buildURL("job/" + name + "/" + buildNumber + "/api/json");
    DefaultHttpClient client = HttpClientFactory.getInstance();
    String response = null;
    try {
      response = HttpClientUtil.fetch(client, url);
      JSONObject json = new JSONObject(response);
      return json.getBoolean("building");
    } catch (Exception e) {
      System.out.println(response);
      return false;
    }
  }
  
  public String getStatus(int buildNumber) throws IOException {
    String url = master.buildURL("job/" + name + "/" + buildNumber + "/api/json");
    DefaultHttpClient client = HttpClientFactory.getInstance();
    String response = HttpClientUtil.fetch(client, url);
    JSONObject json = new JSONObject(response);
    return json.getString("result");
  }
  
  public boolean delete() throws IOException {
    String url = master.buildURL("job/" + name + "/doDelete");
    DefaultHttpClient client = HttpClientFactory.getInstance();
    HttpContext httpContext = new BasicHttpContext();
    HttpPost post = new HttpPost(url);
    HttpResponse res = client.execute(post, httpContext);
    String body = HttpClientUtil.getContentBodyAsString(res);
    return body.length() == 0;
  }
  
  /**
   * 
   * @return build number
   */
  public int submit() throws IOException {
    String url = master.buildURL("createItem");
    DefaultHttpClient client = HttpClientFactory.getInstance();
    HttpContext httpContext = new BasicHttpContext();
    HttpPost post = new HttpPost(url);

    StringBuilder sb = new StringBuilder();
    sb.append("{").append("\"name\":").append("\"").append(this.name).append("\"");
    sb.append(", \"mode\" : \"hudson.maven.MavenModuleSet\", \"Submit\" : \"OK\"}");
    
    List<NameValuePair> params = new ArrayList<NameValuePair>();
    params.add(new BasicNameValuePair("Submit", "OK"));
    params.add(new BasicNameValuePair("json", sb.toString()));
    params.add(new BasicNameValuePair("mode", "hudson.maven.MavenModuleSet"));
    params.add(new BasicNameValuePair("name", this.name));
    post.setEntity(new UrlEncodedFormEntity(params));
    
    HttpResponse res = client.execute(post, httpContext);
    String body = HttpClientUtil.getContentBodyAsString(res);
    if (body.length() == 0) {
      url = master.buildURL("job/" + this.name + "/configSubmit");
      post = new HttpPost(url);
      post.setEntity(this.buildFormData());
      res = client.execute(post, httpContext);
      body = HttpClientUtil.getContentBodyAsString(res);
      
      if (body.length() == 0) {
        return build();
      }
    }
    return -1;
  }
  
  public int build() throws IOException {
    DefaultHttpClient client = HttpClientFactory.getInstance();
    String url = master.buildURL("job/" + this.name + "/build?delay=0sec");
    String body = HttpClientUtil.fetch(client, url);
    
    if (body.length() == 0) {
      url = master.buildURL("job/" + this.name + "/api/json");
      body = HttpClientUtil.fetch(client, url);
      JSONObject json = new JSONObject(body);
      int nextBuildNumber = json.getInt("nextBuildNumber");
      return nextBuildNumber == 1 ? 1 : nextBuildNumber - 1;
    }
    
    return -1;
  }
  
  private HttpEntity buildFormData() throws IOException {
    List<NameValuePair> list = new ArrayList<NameValuePair>();
    BufferedReader reader = new BufferedReader(new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream("jenkins-job-template")));
    String line = null;
    while ((line = reader.readLine()) != null) {
      String[] arrays = line.split("\t");
      if (arrays.length == 2) {
        String key = arrays[0].trim();
        String value = arrays[1].trim();
        list.add(new BasicNameValuePair(key, value));
      } else {
        arrays = line.split(" ");
        if (arrays.length == 2) {
          String key = arrays[0].trim();
          String value = arrays[1].trim();
          list.add(new BasicNameValuePair(key, value));
        } else {
          System.err.println(line);
        }
      }
    }
    list.add(new BasicNameValuePair("_.assignedLabelString", assigned));
    list.add(new BasicNameValuePair("name", name));
    list.add(new BasicNameValuePair("_.url", gitURL));
    list.add(new BasicNameValuePair("goals", goals));
    list.add(new BasicNameValuePair("mavenOpts", mavenOpts));
    BufferedInputStream is = new BufferedInputStream(Thread.currentThread().getContextClassLoader().getResourceAsStream("jenkins-job-json-template"));
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    byte[] buff = new byte[1024];
    for (int i = is.read(buff); i != -1; i = is.read(buff)) {
      bos.write(buff, 0, i);
    }
    String json = new String(bos.toByteArray());
    json = String.format(json, name, assigned, gitURL, goals);
    list.add(new BasicNameValuePair("json", json));
    return new UrlEncodedFormEntity(list);
  }
  
  public String getName() {
    return name;
  }

  public String getAssigned() {
    return assigned;
  }

  public String getGitURL() {
    return gitURL;
  }
  
  public String getGoals() {
    return goals;
  }
  
  public String getMavenOpts() {
    return mavenOpts;
  }
}
