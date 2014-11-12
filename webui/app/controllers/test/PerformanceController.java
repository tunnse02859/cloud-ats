/**
 * 
 */
package controllers.test;

import helpertest.JMeterScriptHelper;
import helpertest.JenkinsJobHelper;
import helpertest.TestProjectHelper;
import helpervm.VMHelper;
import interceptor.AuthenticationInterceptor;
import interceptor.Authorization;
import interceptor.WizardInterceptor;

import java.io.IOException;
import java.util.List;

import models.test.JenkinsJobModel;
import models.test.TestProjectModel;
import models.vm.VMModel;

import org.ats.jmeter.models.JMeterScript;

import play.api.templates.Html;
import play.mvc.Result;
import play.mvc.With;
import scala.collection.mutable.StringBuilder;
import views.html.test.body;
import views.html.test.index;
import views.html.test.report_perf;
import views.html.test.snapshot;

import com.mongodb.BasicDBObject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Oct 20, 2014
 */

@With({WizardInterceptor.class, AuthenticationInterceptor.class})
@Authorization(feature = "Performance", operation = "")
public class PerformanceController extends TestController {

  public static Result index() {
    return ok(index.render(TestProjectModel.PERFORMANCE, body.render(TestProjectModel.PERFORMANCE)));
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
  
  public static Result deleteProject(String projectId) throws IOException {
    TestController.delete(projectId);
    return redirect(routes.PerformanceController.index());
  }

  public static Result createProjectByUpload(boolean run) throws Exception {
    TestController.createProjectByUpload(run, TestProjectModel.PERFORMANCE);
    return redirect(controllers.test.routes.PerformanceController.index());
  }
}
