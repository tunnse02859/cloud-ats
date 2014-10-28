/**
 * 
 */
package controllers.test;

import helpertest.TestHelper;
import helpervm.VMHelper;
import interceptor.AuthenticationInterceptor;
import interceptor.Authorization;
import interceptor.WizardInterceptor;

import java.io.File;
import java.io.FileInputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import models.test.JenkinsJobStatus;
import models.test.TestProjectModel;
import models.test.TestProjectModel.TestProjectType;
import models.vm.VMModel;

import org.ats.common.StringUtil;
import org.ats.common.ssh.SSHClient;
import org.ats.component.usersmgt.group.Group;
import org.ats.component.usersmgt.group.GroupDAO;
import org.ats.component.usersmgt.user.User;
import org.ats.component.usersmgt.user.UserDAO;
import org.ats.gitlab.GitlabAPI;
import org.gitlab.api.models.GitlabProject;

import play.data.DynamicForm;
import play.mvc.Controller;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.Result;
import play.mvc.With;
import views.html.test.index;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.Session;
import com.mongodb.BasicDBObject;

import controllers.Application;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Oct 27, 2014
 */
@With({WizardInterceptor.class, AuthenticationInterceptor.class})
@Authorization(feature = "Functional", operation = "")
public class FunctionalController extends Controller {

  public static Result index() {
    return ok(index.render(TestProjectType.functional.toString()));
  }
  
  public static Result createProjectByUpload(boolean run) {
    try {
      MultipartFormData body = request().body().asMultipartFormData();
      DynamicForm form = DynamicForm.form().bindFromRequest();

      String testName = form.get("name");

      FilePart uploaded = body.getFile("uploaded");
      if (uploaded != null) {
        
        File file = uploaded.getFile();

        Group company = TestController.getCompany();
        User currentUser = UserDAO.getInstance(Application.dbName).findOne(session("user_id"));
        Group currentGroup = GroupDAO.getInstance(Application.dbName).findOne(session("group_id"));
        
        VMModel jenkins = VMHelper.getVMs(new BasicDBObject("group_id", company.getId()).append("jenkins", true)).iterator().next();

        String gitlabToken = VMHelper.getSystemProperty("gitlab-api-token");

        GitlabAPI gitlabAPI = new GitlabAPI("http://" + jenkins.getPublicIP(), gitlabToken);

        GitlabProject gitProject = createGitProject(gitlabAPI, testName);

        String gitSshUrl = gitProject.getSshUrl().replace("git.sme.org", jenkins.getPublicIP());

        String gitHttpUrl = gitProject.getHttpUrl().replace("git.sme.org", jenkins.getPublicIP());
        
        TestProjectModel project = new TestProjectModel(
            TestHelper.getCurrentProjectIndex(TestProjectType.performance) + 1,
            gitProject.getId(),
            testName, 
            currentGroup.getId(), 
            currentUser.getId(), 
            TestProjectType.functional, 
            gitSshUrl,
            gitHttpUrl,
            uploaded.getFilename(), StringUtil.readStream(new FileInputStream(file)).getBytes());

        project.put("status", run ? JenkinsJobStatus.Initializing.toString() : JenkinsJobStatus.Ready.toString());
        project.put("jenkins_id", jenkins.getId());
        
        
        TestHelper.createProject(TestProjectType.functional, project);
        
        ZipFile zip = new ZipFile(file);
        Enumeration<? extends ZipEntry> entries = zip.entries();
        while(entries.hasMoreElements()){
            ZipEntry entry = entries.nextElement();
            System.out.println(entry.getName());
            gitlabAPI.createFile(gitProject, entry.getName(), "master", StringUtil.readStream(zip.getInputStream(entry)), "Snapshot 1");
        }
        
        if (run) return runProject(project.getId());
      }
      return redirect(routes.FunctionalController.index());
    } catch (Exception e) {
      e.printStackTrace();
      return forbidden(views.html.forbidden.render());
    }
  }
  
  public static Result runProject(String projectId) throws Exception {
    return redirect(routes.FunctionalController.index());
  }
  
  private static GitlabProject createGitProject(GitlabAPI api, String projectName) throws Exception {
    GitlabProject project = api.getAPI().createProject(projectName);
    
    String url = project.getSshUrl().replace("git.sme.org", api.getHost());
    
    StringBuilder sb = new StringBuilder("ssh-keyscan -H ").append(api.getHost()).append(" >> ~/.ssh/known_hosts").append(" && ");
    sb.append("git config --global user.name 'Administrator'").append(" && ");
    sb.append("git config --global user.email 'admin@local.host'").append(" && ");
    sb.append("rm -rf /tmp/").append(projectName).append(" && ");
    sb.append("mkdir /tmp/").append(projectName).append(" && ");
    sb.append("cd /tmp/").append(projectName).append(" && ");
    sb.append("git init").append(" && ");
    sb.append("touch README").append(" && ");
    sb.append("git add README").append(" && ");
    sb.append("git commit -m 'first commit'").append(" && ");
    sb.append("git remote add origin ").append(url).append(" && ");
    sb.append("git push -u origin master");
  
    Session session = SSHClient.getSession(api.getHost(), 22, "ubuntu", "ubuntu");
    ChannelExec channel = (ChannelExec) session.openChannel("exec");
       
    channel.setCommand(sb.toString());
    channel.connect();
    
    SSHClient.printOut(System.out, channel);
    return project;
  }
}
