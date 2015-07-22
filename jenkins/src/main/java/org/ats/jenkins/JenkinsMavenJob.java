/**
 * 
 */
package org.ats.jenkins;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
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
import org.ats.common.StringUtil;
import org.ats.common.http.HttpClientFactory;
import org.ats.common.http.HttpClientUtil;
import org.json.JSONObject;
import org.rythmengine.Rythm;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Apr 26, 2014
 */
public class JenkinsMavenJob {
  
  /** .*/
  private final String name, remote, pomLocation, goals;
  
  /** .*/
  private final JenkinsMaster master;
  
  private final String jobTmpl;
  
  private final String jobJsonTmpl;
  
  /**
   * 
   * @param name the job name
   * @param assigned the slave label
   * @param gitURL the http url of git project
   * @param goals empty string for not specified.
   * @param mavenOpts empty string for not specified.
   * @throws IOException 
   */
  public JenkinsMavenJob(JenkinsMaster master, String name, String remote, String pomLocation, String goals) throws IOException {
    this.name = name;
    this.remote = remote;
    this.pomLocation = pomLocation;
    this.goals = goals;
    this.master = master;
    
    Map<String, String> params = new HashMap<String, String>();
    params.put("name", this.name);
    params.put("remote", this.remote);
    params.put("pomLocation", this.pomLocation);
    params.put("goals", this.goals);
    
    String jobTmpl = StringUtil.readStream(Thread.currentThread().getContextClassLoader().getResourceAsStream("jenkins-job-template"));
    this.jobTmpl = Rythm.render(jobTmpl, params);
    
    String jobJsonTmpl = StringUtil.readStream(Thread.currentThread().getContextClassLoader().getResourceAsStream("jenkins-job-json-template"));
    this.jobJsonTmpl = Rythm.render(jobJsonTmpl, params);
  }
  
  public byte[] getConsoleOutput(int buildNumber, int start) throws IOException {
    String url = master.buildURL("job/" + encodeURIComponent(name) + "/" + buildNumber + "/logText/progressiveHtml");
    CloseableHttpClient client = HttpClientFactory.getInstance();
    HttpContext httpContext = new BasicHttpContext();
    HttpPost post = new HttpPost(url);
    List<NameValuePair> list = new ArrayList<NameValuePair>();
    list.add(new BasicNameValuePair("start", String.valueOf(start)));
    HttpResponse res = client.execute(post, httpContext);
    return HttpClientUtil.getContentBodyAsByteArray(res);
  }
  
  public boolean isBuilding(int buildNumber, long start, long timeout) throws Exception  {
    String url = master.buildURL("job/" + encodeURIComponent(name) + "/" + buildNumber + "/api/json");
    CloseableHttpClient client = HttpClientFactory.getInstance();
    HttpResponse response = null;
    try {
      response = HttpClientUtil.execute(client, url);
      JSONObject json = new JSONObject(HttpClientUtil.getContentBodyAsString(response));
      return json.getBoolean("building");
    } catch (Exception e) {
      if ((System.currentTimeMillis() - start) < timeout) {
        Thread.sleep(3000);
        return isBuilding(buildNumber, start, timeout);
      }
      throw new Exception("The request " + url + " has reponse code " + response.getStatusLine().getStatusCode());
    }
  }
  
  public String getStatus(int buildNumber) throws IOException {
    String url = master.buildURL("job/" + encodeURIComponent(name) + "/" + buildNumber + "/api/json");
    CloseableHttpClient client = HttpClientFactory.getInstance();
    String response = HttpClientUtil.fetch(client, url);
    JSONObject json = new JSONObject(response);
    return json.getString("result");
  }
  
  public boolean delete() throws IOException {
    String url = master.buildURL("job/" + encodeURIComponent(name) + "/doDelete");
    CloseableHttpClient client = HttpClientFactory.getInstance();
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
    CloseableHttpClient client = HttpClientFactory.getInstance();
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
      url = master.buildURL("job/" + encodeURIComponent(this.name) + "/configSubmit");
      post = new HttpPost(url);
      post.setEntity(this.buildFormData());
      res = client.execute(post, httpContext);
      body = HttpClientUtil.getContentBodyAsString(res);
      System.out.println(body);
      if (body.length() == 0) {
        return build();
      }
    }
    return -1;
  }
  
  public boolean update() throws Exception {
    CloseableHttpClient client = HttpClientFactory.getInstance();
    HttpContext httpContext = new BasicHttpContext();
    String url = master.buildURL("job/" + encodeURIComponent(this.name) + "/configSubmit");
    HttpPost post = new HttpPost(url);
    post.setEntity(this.buildFormData());
    HttpResponse res = client.execute(post, httpContext);
    return res.getStatusLine().getStatusCode() == 302;
  }
  
  public boolean stop() {
    try{
      CloseableHttpClient client = HttpClientFactory.getInstance();
      String url = master.buildURL("job/" + encodeURIComponent(this.name) + "/api/json");
      String body = HttpClientUtil.fetch(client, url);
      System.out.println(body);
      JSONObject json = new JSONObject(body);
      int currentbuildNumber = json.getInt("nextBuildNumber")-1;
      HttpContext httpContext = new BasicHttpContext();
     // String status = getStatus(currentbuildNumber);
      url = master.buildURL("job/" + encodeURIComponent(this.name) + "/"+currentbuildNumber+"/stop" );
      HttpPost post = new HttpPost(url);
      HttpResponse res = client.execute(post,httpContext);
      return res.getStatusLine().getStatusCode() == 302;
    }
    catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }
  
  public int build() {
    try {
      CloseableHttpClient client = HttpClientFactory.getInstance();
      String url = master.buildURL("job/" + encodeURIComponent(this.name) + "/api/json");
      String body = HttpClientUtil.fetch(client, url);
      JSONObject json = new JSONObject(body);
      int buildNumber = json.getInt("nextBuildNumber");

      url = master.buildURL("job/" + encodeURIComponent(this.name) + "/build?delay=0sec");
      body = HttpClientUtil.fetch(client, url);

      return body.length() == 0 ? buildNumber : -1;
    } catch (Exception e) {
      e.printStackTrace();
      return -1;
    }
  }
  
  private HttpEntity buildFormData() throws IOException {
    List<NameValuePair> list = new ArrayList<NameValuePair>();
    for (String line : jobTmpl.split("\n")) {
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
    list.add(new BasicNameValuePair("name", this.name));
    list.add(new BasicNameValuePair("remote", this.remote));
    list.add(new BasicNameValuePair("pomLocation", this.pomLocation));
    list.add(new BasicNameValuePair("goals", this.goals));
    list.add(new BasicNameValuePair("mavenOpts", ""));
    list.add(new BasicNameValuePair("json", this.jobJsonTmpl));

    return new UrlEncodedFormEntity(list);
  }
  
  private String encodeURIComponent(String s) {
    String result;

    try {
      result = URLEncoder.encode(s, "UTF-8").replaceAll("\\+", "%20")
          .replaceAll("\\%21", "!").replaceAll("\\%27", "'")
          .replaceAll("\\%28", "(").replaceAll("\\%29", ")")
          .replaceAll("\\%7E", "~");
    } catch (UnsupportedEncodingException e) {
      result = s;
    }

    return result;
  }
  
  public String getName() {
    return name;
  }
}
