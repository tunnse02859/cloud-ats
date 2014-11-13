/**
 * 
 */
package models.test;

import helpertest.JenkinsJobHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;

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
  
  public List<JenkinsBuildResult> getResults() {
    if (this.get("results") == null) return Collections.emptyList();
    List<JenkinsBuildResult> list = new ArrayList<JenkinsBuildResult>();
    Iterator<Object> iterator = ((BasicDBList)this.get("results")).iterator();
    while(iterator.hasNext()) {
      Object obj = iterator.next();
      BasicDBObject source = (BasicDBObject) obj;
      list.add(new JenkinsBuildResult().from(source));
    }
    return list;
  }
  
  public void addBuildResult(JenkinsBuildResult result) {
    ArrayList<JenkinsBuildResult> results = (ArrayList<JenkinsBuildResult>) this.get("results");
    
    DBObject query = QueryBuilder.start("results").elemMatch(QueryBuilder.start("build_number").is(result.get("build_number")).get()).get();
    
    DBObject source = JenkinsJobHelper.getCollection().findOne(new BasicDBObject("_id", this.get("_id")), query);
    if(source.containsField("results")) {
      Iterator<Object> iterator = ((BasicDBList) source.get("results")).iterator();
      results.remove(new JenkinsBuildResult().from((DBObject) iterator.next())); 
    }
    
    results.add(result);
    this.put("results", results);
  }
  
  public static class JenkinsBuildResult extends BasicDBObject {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    public JenkinsBuildResult() {
      this(0, JenkinsJobStatus.Completed, 0);
    }

    public JenkinsBuildResult(int buildNumber, JenkinsJobStatus status, long buildTime) {
      this.put("build_number", buildNumber);
      this.put("status", status.toString());
      this.put("build_time", buildTime);
    }
    
    public JenkinsBuildResult from(DBObject source) {
      this.put("build_number", source.get("build_number"));
      this.put("status", source.get("status"));
      this.put("build_time", source.get("build_time"));
      return this;
    }
  }
}
