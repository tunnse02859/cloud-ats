/**
 * 
 */
package controllers.test;

import helpertest.JenkinsJobHelper;
import helpertest.TestProjectHelper;
import helpervm.VMHelper;
import interceptor.AuthenticationInterceptor;
import interceptor.Authorization;
import interceptor.WizardInterceptor;

import java.io.IOException;

import models.test.JenkinsJobModel;
import models.test.TestProjectModel;
import models.vm.VMModel;
import play.mvc.Result;
import play.mvc.With;
import views.html.test.body;
import views.html.test.index;
import views.html.test.report_func;

import com.mongodb.BasicDBObject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Oct 27, 2014
 */
@With({WizardInterceptor.class, AuthenticationInterceptor.class})
@Authorization(feature = "Functional", operation = "")
public class FunctionalController extends TestController {

  public static Result index() {
    return ok(index.render(TestProjectModel.FUNCTIONAL, body.render(TestProjectModel.FUNCTIONAL)));
  }
  
  public static Result report(String projectId) {
    JenkinsJobModel job = JenkinsJobHelper.getJobs(new BasicDBObject("project_id", projectId)).iterator().next();
    TestProjectModel project = TestProjectHelper.getProjectById(projectId);
    VMModel jenkins = VMHelper.getVMByID(job.getString("jenkins_id"));
    return ok(report_func.render(project.getName(), "http://" + jenkins.getPublicIP() + ":8080/job/" + job.getId() + "/ws/target/surefire-reports/html/index.html"));
  }
  
  public static Result createProjectByUpload(boolean run) throws Exception {
    TestController.createProjectByUpload(run, TestProjectModel.FUNCTIONAL);
    return redirect(routes.FunctionalController.index());
  }
  
  public static Result runProject(String projectId) throws Exception {
    final TestProjectModel project = TestProjectHelper.getProjectById( projectId);
    TestController.run(project, projectId);
    return redirect(routes.FunctionalController.index());
  }
  
  public static Result deleteProject(String projectId) throws IOException {
    TestController.delete(projectId);
    return redirect(routes.FunctionalController.index());
  }
}
