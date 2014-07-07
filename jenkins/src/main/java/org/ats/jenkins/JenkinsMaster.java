/**
 * 
 */
package org.ats.jenkins;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;
import org.ats.common.http.HttpClientFactory;
import org.ats.common.http.HttpClientUtil;

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
  private String[] systemNodes = { "master", "chef-workstation" };
  
  public JenkinsMaster(String masterHost, String scheme, int port) {
    this.masterHost = masterHost;
    this.scheme = scheme;
    this.port = port;
  }
  
  public List<String> listSlaves() throws IOException {
    List<String> slaves = new ArrayList<String>();
    DefaultHttpClient client = HttpClientFactory.getInstance();
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
    sb.append("://").append(masterHost).append(":").append(port).append("/").append(actionURL);
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
