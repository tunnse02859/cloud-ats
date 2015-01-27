/**
 * 
 */
package helpertest;

import helpervm.VMHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import models.test.JenkinsJobModel;
import models.test.JenkinsJobStatus;
import models.vm.VMModel;

import org.ats.component.usersmgt.DataFactory;
import org.ats.jenkins.JenkinsMaster;
import org.ats.jenkins.JenkinsMavenJob;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import controllers.Application;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Oct 25, 2014
 */
public class JenkinsJobHelper {
  
  public static DB getDatabase() {
    return DataFactory.getDatabase(Application.dbName);
  }
  
  public static DBCollection getCollection() {
    DB db = getDatabase();
    DBCollection col = db.getCollection("test_job");
    return col;
  }
  
  public static void createJenkinsJob(JenkinsJobModel job) {
    DBCollection col = getCollection();
    col.insert(job);
  }
  
  public static void updateJenkinsJob(JenkinsJobModel job) {
    DBCollection col = getCollection();
    col.save(job);
  }
  
  public static void removeJenkinsJob(JenkinsJobModel job) {
    DBCollection col = getCollection();
    col.remove(job);
  }
  
  public static List<JenkinsJobModel> getJobs(DBObject query) {
    DBCollection col = getCollection();
    DBCursor cursor = col.find(query);
    if (!cursor.hasNext()) return Collections.emptyList();
    List<JenkinsJobModel> jobs = new ArrayList<JenkinsJobModel>();
    while(cursor.hasNext()) {
      jobs.add(new JenkinsJobModel().from(cursor.next()));
    }
    return jobs;
  }
  
  public static JenkinsJobModel getJobById(String jobId) {
    DBCollection col = getCollection();
    DBObject source = col.findOne(new BasicDBObject("_id", jobId));
    return source == null ? null : new JenkinsJobModel().from(source);
  }
  
  public static List<JenkinsJobModel> getRunningJobs() {
    BasicDBObject query = new BasicDBObject();
    query.put("status", JenkinsJobStatus.Running.toString());
    return getJobs(query);
  }
  
  public static List<JenkinsJobModel> getInitializingJobs() {
    BasicDBObject query = new BasicDBObject();
    query.put("status", JenkinsJobStatus.Initializing.toString());
    return getJobs(query);
  }
  
  @Deprecated
  public static void deleteBuildOfSnapshot(String snapshotId) throws IOException {
    List<JenkinsJobModel> jobs = getJobs(new BasicDBObject("_id", snapshotId));
    for (JenkinsJobModel job : jobs) {
      VMModel master = VMHelper.getVMByID(job.getString("jenkins_id"));
      String subfix = master.getName() + "/jenkins";
      JenkinsMavenJob jenkinsJob = new JenkinsMavenJob(new JenkinsMaster(master.getPublicIP(), "http", subfix, 8080), job.getId(), null, null, null, null, null);
      jenkinsJob.delete();
      removeJenkinsJob(job);
    }
  }
  
  public static void deleteBuildOfProject(String projectId) throws IOException {
    List<JenkinsJobModel> jobs = getJobs(new BasicDBObject("project_id", projectId));
    for (JenkinsJobModel job : jobs) {
      VMModel master = VMHelper.getVMByID(job.getString("jenkins_id"));
      String subfix = master.getName() + "/jenkins";
      JenkinsMavenJob jenkinsJob = new JenkinsMavenJob(new JenkinsMaster(master.getPublicIP(), "http", subfix, 8080), job.getId(), null, null, null, null, null);
      jenkinsJob.delete();
      removeJenkinsJob(job);
    }
  }
  
  public static int getCurrentBuildIndex(String projectId) {
    DBCollection col  = getCollection();
    DBCursor cursor = col.find(new BasicDBObject("project_id", projectId)).sort(new BasicDBObject("index", -1)).limit(1);
    if (!cursor.hasNext()) return 0;
    DBObject obj = cursor.next();
    return obj.get("index") != null ? (Integer) obj.get("index") : 0;
  }
}
