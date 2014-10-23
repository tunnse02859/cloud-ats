/**
 * 
 */
package controllers.test;

import helpertest.PerformanceHelper;
import helpervm.VMHelper;
import interceptor.AuthenticationInterceptor;
import interceptor.Authorization;
import interceptor.WizardInterceptor;

import java.io.FileInputStream;
import java.util.LinkedList;

import models.test.TestProjectModel;
import models.test.TestProjectModel.TestProjectType;
import models.vm.VMModel;

import org.ats.common.StringUtil;
import org.ats.component.usersmgt.group.Group;
import org.ats.component.usersmgt.group.GroupDAO;
import org.ats.component.usersmgt.user.User;
import org.ats.component.usersmgt.user.UserDAO;
import org.ats.gitlab.GitlabAPI;
import org.ats.jmeter.JMeterFactory;
import org.ats.jmeter.JMeterParser;
import org.ats.jmeter.models.JMeterScript;
import org.gitlab.api.models.GitlabProject;

import play.data.DynamicForm;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.Result;
import play.mvc.With;
import views.html.defaultpages.error;
import views.html.test.index;

import com.mongodb.BasicDBObject;

import controllers.Application;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Oct 20, 2014
 */

@With({WizardInterceptor.class, AuthenticationInterceptor.class})
@Authorization(feature = "Performance", operation = "")
public class PerformanceController extends TestController {

  public static Result index() {
    return ok(index.render("p"));
  }

  public static Result createProjectByUpload() {
    try {
      MultipartFormData body = request().body().asMultipartFormData();
      DynamicForm form = DynamicForm.form().bindFromRequest();

      String testName = form.get("name");

      FilePart uploaded = body.getFile("uploaded");
      if (uploaded != null) {
        FileInputStream fis = new FileInputStream(uploaded.getFile());
        String content = StringUtil.readStream(fis);

        JMeterFactory factory = new JMeterFactory();
        JMeterParser parser = factory.createJMeterParser(content);
        JMeterScript script = parser.parse();

        User currentUser = UserDAO.getInstance(Application.dbName).findOne(session("user_id"));
        Group currentGroup = GroupDAO.getInstance(Application.dbName).findOne(session("group_id"));

        Group company = null;
        if (currentGroup.getInt("level") > 1) {
          LinkedList<Group> parents = GroupDAO.getInstance(Application.dbName).buildParentTree(currentGroup);
          company = parents.get(0);
        } else if (currentGroup.getInt("level") == 1) {
          company = currentGroup;
        }

        VMModel jenkins = VMHelper.getVMs(new BasicDBObject("group_id", company.getId()).append("jenkins", true)).iterator().next();

        String gitlabToken = VMHelper.getSystemProperty("gitlab-api-token");

        GitlabAPI gitlabAPI = new GitlabAPI("http://" + jenkins.getPublicIP(), gitlabToken);

        GitlabProject gitProject = factory.createProject(gitlabAPI, company.getString("name"), testName);

        gitlabAPI.createFile(gitProject, "src/test/jmeter/script.jmx", "master", script.toString(), "Snapshot 1");

        String gitUrl = gitProject.getSshUrl().replace("git.sme.org", jenkins.getPublicIP());

        TestProjectModel project = new TestProjectModel(
            testName, 
            currentGroup.getId(), 
            currentUser.getId(), 
            TestProjectType.PERFORMANCE, 
            gitUrl, 
            uploaded.getFilename(), content.getBytes("UTF-8"));

        project.put("jmeter", script);

        PerformanceHelper.createPerformanceProject(project);
      }
      return redirect(controllers.test.routes.PerformanceController.index());
    } catch (Exception e) {
      e.printStackTrace();
      return forbidden(views.html.forbidden.render());
    }
  }
}
