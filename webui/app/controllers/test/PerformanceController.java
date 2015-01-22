/**
 * 
 */
package controllers.test;

import helpertest.*;
import helpervm.VMHelper;
import interceptor.AuthenticationInterceptor;
import interceptor.Authorization;
import interceptor.WizardInterceptor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import models.test.JenkinsJobModel;
import models.test.JenkinsJobStatus;
import models.test.TestProjectModel;
import models.vm.VMModel;

import org.ats.jenkins.JenkinsMaster;
import org.ats.jenkins.JenkinsMavenJob;
import org.ats.jmeter.models.JMeterSampler;
import org.ats.jmeter.models.JMeterScript;

import play.api.templates.Html;
import play.mvc.Result;
import play.mvc.With;
import play.libs.Json;
import scala.collection.mutable.StringBuilder;
import views.html.test.*;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Oct 20, 2014
 */

@With({WizardInterceptor.class, AuthenticationInterceptor.class})
@Authorization(feature = "Performance", operation = "")
public class PerformanceController extends TestController {

  public static Result index() throws Exception {
    int page = 1;
    if(request().getQueryString("page") != null) {
      page = Integer.parseInt(request().getQueryString("page"));
    }
    
    String name = "";
    if(request().getQueryString("name") != null) {
      name = request().getQueryString("name");
      return ok(index.render(TestProjectModel.PERFORMANCE,body.render(TestProjectModel.PERFORMANCE, TestController.countProjectByName(name),page,name)));
    } else {
      return ok(index.render(TestProjectModel.PERFORMANCE, body.render(TestProjectModel.PERFORMANCE, TestController.countProject(TestProjectModel.PERFORMANCE), page,null)));
    }
  }
  
  public static Result report(String snapshotId) throws Exception {
    JMeterScript snapshot = JMeterScriptHelper.getJMeterScriptById(snapshotId);
    TestProjectModel project = TestProjectHelper.getProjectById(snapshot.getString("project_id"));
    JenkinsJobModel job = JenkinsJobHelper.getJobs(new BasicDBObject("_id", snapshotId)).iterator().next();
    VMModel jenkins = VMHelper.getVMByID(job.getString("jenkins_id"));
    String report1 = "http://" + jenkins.getPublicIP() + ":8080/job/" + job.getId() + "/ws/target/jmeter/results/ResponseTimesOverTime.png" ;
    String report2 = "http://" + jenkins.getPublicIP() + ":8080/job/" + job.getId() + "/ws/target/jmeter/results/ThreadsStateOverTime.png" ;
    String report3 = "http://" + jenkins.getPublicIP() + ":8080/job/" + job.getId() + "/ws/target/jmeter/results/TransactionsPerSecond.png" ;
    
    String name = project.getName() + " / " + snapshot.getString("commit");
    return ok(report_perf.render(name, report1, report2, report3));
  }
  
  public static Html getSnapshotHtml(TestProjectModel project) {
    StringBuilder sb = new StringBuilder();
    List<JMeterScript> scripts = JMeterScriptHelper.getJMeterScript(project.getId());
    Collections.sort(scripts, new Comparator<JMeterScript>() {
     
      @Override
      public int compare(JMeterScript o1, JMeterScript o2) {
        return (o2.getInt("index") - o1.getInt("index"));
      }
    });
    
    for(JMeterScript script : scripts) {
      sb.append(snapshot.render(script));
    }
    return new Html(sb);
  }
  
  public static Result runProject(String projectId) throws Exception {
    TestProjectModel project = TestProjectHelper.getProjectById(projectId);
    JMeterScript snapshot = JMeterScriptHelper.getLastestCommit(project.getId());    
    TestController.run(project, snapshot.getString("_id"));
    return redirect(routes.PerformanceController.index());
  }
  
  public static Result runSnapshot(String snapshotId) throws Exception {
    JMeterScript snapshot = JMeterScriptHelper.getJMeterScriptById(snapshotId);
    TestProjectModel project = TestProjectHelper.getProjectById(snapshot.getString("project_id"));
    TestController.run(project, snapshot.getString("_id"));
    return redirect(routes.PerformanceController.index());
  }
  
  public static Result updateProject(String projectId) throws IOException {
    TestProjectModel project = TestProjectHelper.getProjectById(projectId);
    
    String type = project.getType();
    
    List<JMeterScript> jmeter = JMeterScriptHelper.getJMeterScript(projectId);
    JMeterScript jmeterModel= null;
    BasicDBObject query = new BasicDBObject();
    query.append("index", jmeter.size());
    DBCursor cursor = JMeterScriptHelper.getCollection().find(query);
    if(!cursor.hasNext()) {
      Collections.emptyList();
    }
    while( cursor.hasNext()){
      jmeterModel = new JMeterScript().from(cursor.next());
    }
    
    JMeterSampler[] samplers = jmeterModel.getSamplers();
    List<JMeterSampler> listSample = new ArrayList<JMeterSampler>();
    
    for (JMeterSampler sampler : samplers) {
     
      listSample.add(sampler);
    }
    
    return ok(index.render(type, views.html.test.updatewizard.render(project,jmeterModel,listSample)));
  }
  
  public static Result deleteProject(String projectId) throws IOException {
    TestController.delete(projectId);
    return redirect(routes.PerformanceController.index());
  }

  public static Result createProjectByUpload(boolean run) throws Exception {
    TestController.createProjectByUpload(run, TestProjectModel.PERFORMANCE);
    return redirect(controllers.test.routes.PerformanceController.index());
  }
  
  public static Result stopProject(String projectId) throws Exception {
    List<JenkinsJobModel> jobs = JenkinsJobHelper.getJobs(new BasicDBObject("status", JenkinsJobStatus.Running.toString()).append("project_id", projectId));
    final JenkinsJobModel jobModel = jobs.get(0);
    VMModel jenkins = VMHelper.getVMByID(jobModel.getString("jenkins_id"));
    JenkinsMaster jenkinsMaster = new JenkinsMaster(jenkins.getPublicIP(), "http", 8080);
    String snapsortId = jobModel.getId();
    JenkinsMavenJob maven = new JenkinsMavenJob(jenkinsMaster, snapsortId, null, null, null, null, null);
    maven.stop();
    return redirect(routes.PerformanceController.index());
  }
  
  public static Result stopSnapsort(String snapsortId) throws Exception {
    final JenkinsJobModel jobModel = JenkinsJobHelper.getJobById(snapsortId);
    VMModel jenkins = VMHelper.getVMByID(jobModel.getString("jenkins_id"));
    JenkinsMaster jenkinsMaster = new JenkinsMaster(jenkins.getPublicIP(), "http", 8080);
    JenkinsMavenJob maven = new JenkinsMavenJob(jenkinsMaster, snapsortId, null, null, null, null, null);
    maven.stop();
    return redirect(routes.PerformanceController.index());
  }
 public static Result filter() throws Exception {
    
    Map<String, String[]> parameters = request().queryString();
    Set<TestProjectModel> filter = new HashSet<TestProjectModel>();
    String name = parameters.get("name")[0];
    BasicDBObject query = new BasicDBObject();
    if(parameters.containsKey("name") && parameters.containsKey("creator")){
      
      if(!"".equalsIgnoreCase(name) ){
        query.put("$text", new BasicDBObject("$search", name));
      }
    }
    filter.addAll(TestProjectHelper.getProject(query));
    List<TestProjectModel> projects = new ArrayList<TestProjectModel>(filter);
    ArrayNode array = Json.newObject().arrayNode();
    ObjectNode json = null;
    for (TestProjectModel project : projects) {
      json= Json.newObject();
      json.put("id", project.getId());
      array.add(json);
    }
    return ok(array);
   
  }
}
