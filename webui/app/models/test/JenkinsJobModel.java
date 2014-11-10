/**
 * 
 */
package models.test;

import java.util.ArrayList;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Oct 25, 2014
 */
public class JenkinsJobModel extends BasicDBObject {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  
  public JenkinsJobModel(int index, String id, String projectId, String vmId, String jenkinsId, String type) {
    this.put("_id", id);
    this.put("index", index);
    this.put("project_id", projectId);
    this.put("vm_id", vmId);
    this.put("jenkins_id", jenkinsId);
    this.put("status", JenkinsJobStatus.Initializing.toString());
    this.put("job_type", type == null ? null : type.toString());
    
    this.put("results", new ArrayList<JenkinsBuildResult>());
  }
  
  public JenkinsJobModel() {
    this(0, null, null, null, null, null);
  }
  
  public String getId() {
    return this.getString("_id");
  }
  
  public int getIndex() {
    return this.getInt("index");
  }
  
  public void setStatus(JenkinsJobStatus status) {
    this.put("status", status.toString());
  }
  
  public JenkinsJobStatus getStatus() {
    return JenkinsJobStatus.valueOf(this.getString("status"));
  }
  
  public JenkinsJobModel from(DBObject source) {
    this.put("_id", source.get("_id"));
    this.put("index", source.get("index"));
    this.put("project_id", source.get("project_id"));
    this.put("vm_id", source.get("vm_id"));
    this.put("jenkins_id", source.get("jenkins_id"));
    this.put("status", source.get("status"));
    this.put("job_type", source.get("job_type"));

    this.put("log", source.get("log"));
    this.put("results", source.get("results"));
    return this;
  }
  
  public void addBuildResult(JenkinsBuildResult result) {
    ArrayList<JenkinsBuildResult> results = (ArrayList<JenkinsBuildResult>) this.get("results");
    results.add(result);
    this.put("results", results);
  }
  
  public static class JenkinsBuildResult extends BasicDBObject {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public JenkinsBuildResult(int buildNumber, JenkinsJobStatus status, long buildTime) {
      this.put("build_number", buildNumber);
      this.put("status", status.toString());
      this.put("build_time", buildTime);
    }
  }
}
