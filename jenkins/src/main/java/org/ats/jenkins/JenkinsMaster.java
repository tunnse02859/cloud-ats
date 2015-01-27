/**
 * 
 */
package org.ats.jenkins;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.ats.common.http.HttpClientFactory;
import org.ats.common.http.HttpClientUtil;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Apr 21, 2014
 */
public class JenkinsMaster {

  /** .*/
  private String masterHost;
  
  /** .*/
  private String scheme;
  
  /** .*/
  private int port;
  
  /** .*/
  private String subfix;
  
  /** .*/
  private String[] systemNodes = { "master", "chef-workstation" };
  
  public JenkinsMaster(String masterHost, String scheme, String subfix, int port) {
    this.masterHost = masterHost;
    this.scheme = scheme;
    this.subfix = subfix;
    this.port = port;
  }
  
  public boolean isReady() throws IOException {
    CloseableHttpClient client = HttpClientFactory.getInstance();
    String url = buildURL("api/json");
    HttpResponse response = HttpClientUtil.execute(client, url);
    return response.getStatusLine().getStatusCode() == 200;
  }
  
  public boolean isReady(long timeout) throws IOException {
    long start = System.currentTimeMillis();
    while (true) {
      if (this.isReady()) return true;
      if ((System.currentTimeMillis() - start) < timeout) continue;
      return false;
    }
  }
  
  public List<String> listSlaves() throws IOException {
    List<String> slaves = new ArrayList<String>();
    CloseableHttpClient client = HttpClientFactory.getInstance();
    String json = HttpClientUtil.fetch(client, buildURL("computer/api/json"));
    JSONObject jsonObj = new JSONObject(json);
    JSONArray array = jsonObj.getJSONArray("computer");
    for (int i = 0; i < array.length(); i++) {
      JSONObject obj = array.getJSONObject(i);
      String displayName = obj.getString("displayName");
      boolean isSystemNode = false;
      for (String sysNode : systemNodes) {
        if (displayName.equals(sysNode)) {
          isSystemNode = true; 
        }
      }
      if (!isSystemNode) slaves.add(displayName);
    }
    return Collections.unmodifiableList(slaves);
  }
  
  public void deleteAllSlaves() throws IOException {
    for (String slave : listSlaves()) {
      new JenkinsSlave(this, slave).release();
    }
  }
  
  public String buildURL(String actionURL) {
    StringBuilder sb = new StringBuilder(scheme);
    sb.append("://").append(masterHost).append(":").append(port).append("/");
    if (subfix != null && !subfix.isEmpty()) sb.append(subfix).append("/");
    sb.append(actionURL);
    return sb.toString();
  }
  
  public String getMasterHost() {
    return masterHost;
  }
  
  public String scheme() {
    return scheme;
  }
  
  public int getPort() {
    return port;
  }
}
