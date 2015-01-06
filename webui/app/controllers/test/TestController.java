/**
 * 
 */
package controllers.test;

import static akka.pattern.Patterns.ask;
import helpertest.JMeterScriptHelper;
import helpertest.JenkinsJobHelper;
import helpertest.TestProjectHelper;
import helpervm.VMHelper;
import interceptor.AuthenticationInterceptor;
import interceptor.WizardInterceptor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import models.test.JenkinsJobModel;
import models.test.JenkinsJobStatus;
import models.test.TestProjectModel;
import models.vm.VMModel;

import org.ats.common.StringUtil;
import org.ats.common.ssh.SSHClient;
import org.ats.component.usersmgt.UserManagementException;
import org.ats.component.usersmgt.feature.Feature;
import org.ats.component.usersmgt.feature.FeatureDAO;
import org.ats.component.usersmgt.feature.Operation;
import org.ats.component.usersmgt.group.Group;
import org.ats.component.usersmgt.group.GroupDAO;
import org.ats.component.usersmgt.role.Permission;
import org.ats.component.usersmgt.role.Role;
import org.ats.component.usersmgt.user.User;
import org.ats.component.usersmgt.user.UserDAO;
import org.ats.gitlab.GitlabAPI;
import org.ats.jmeter.JMeterFactory;
import org.ats.jmeter.JMeterParser;
import org.ats.jmeter.models.JMeterArgument;
import org.ats.jmeter.models.JMeterSampler;
import org.ats.jmeter.models.JMeterSampler.Method;
import org.ats.jmeter.models.JMeterScript;
import org.gitlab.api.models.GitlabCommit;
import org.gitlab.api.models.GitlabProject;

import play.Logger;
import play.api.templates.Html;
import play.data.DynamicForm;
import play.mvc.Controller;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.Result;
import play.mvc.WebSocket;
import play.mvc.With;
import scala.concurrent.Await;
import scala.concurrent.duration.Duration;
import views.html.test.index;
import views.html.test.newproject;
import views.html.test.project;

import com.fasterxml.jackson.databind.JsonNode;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.Session;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBList;
import com.mongodb.DBObject;

import controllers.Application;
import controllers.vm.VMCreator;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Oct 21, 2014
 */

@With({WizardInterceptor.class, AuthenticationInterceptor.class})
public class TestController extends Controller {
  
  public static Result wizard() {
    String type = request().getQueryString("type");
    String name = request().getQueryString("name");
    
    if (type == null || type.isEmpty()) type = TestProjectModel.PERFORMANCE;
    return ok(index.render(type, views.html.test.wizard.render(type, name)));
  }
  
  public static Result doWizard(String testType) throws Exception {
    Map<String, String[]> params = request().body().asFormUrlEncoded();
    Set<String> keys = params.keySet();
    
    int samplerCount = 0;
    for (String key : keys) {
      if (key.startsWith("sampler-url[")) samplerCount++;
    }
    
    JMeterFactory factory = new JMeterFactory();
    
    JMeterSampler[] samplers = new JMeterSampler[samplerCount];
    
    for(int i = 0; i < samplerCount; i++) {
      String prefix = "sampler[" + i + "]";

      int paramCount = 0;
      for (String key : keys) {
        if (key.startsWith(prefix + "-param-name")) paramCount++;
      }
      
      JMeterArgument[] args = new JMeterArgument[paramCount];
      for (int j = 0; j < paramCount; j++) {
        String paramName = params.get(prefix + "-param-name[" + j + "]")[0];
        String paramValue = params.get(prefix + "-param-value[" + j + "]")[0];
        args[j] = factory.createArgument(paramName, paramValue);
      }
 
      String samplerName = params.get(prefix)[0];
      String samplerMethod = params.get("sampler-method[" + i + "]")[0];
      String samplerUrl = params.get("sampler-url[" + i + "]")[0];
      
      String assertionText = params.get("sampler-assertion-text[" + i +"]")[0];
      
      if(assertionText != null) assertionText = assertionText.trim();
      
      if (assertionText.isEmpty()) assertionText = null;
      
      long constantTime = Integer.parseInt(params.get("sampler-constant-time[" + i + "]" )[0]) * 1000;
      
      samplers[i] = factory.createHttpRequest(
          Method.valueOf(samplerMethod), 
          samplerName, 
          samplerUrl, assertionText, constantTime, args);
    }
    
    int duration = Integer.parseInt(params.get("duration")[0]);
    int loops = Integer.parseInt(params.get("loops")[0]);
    int rampup = Integer.parseInt(params.get("rampup")[0]);
    int users = Integer.parseInt(params.get("users")[0]);
    
    String testName = params.get("test-name")[0];
    
    JMeterScript script = factory.createJmeterScript(testName, loops, users, rampup, duration > 0, duration, samplers);
    
    //create project
    Group company = TestController.getCompany();
    User currentUser = UserDAO.getInstance(Application.dbName).findOne(session("user_id"));
    Group currentGroup = GroupDAO.getInstance(Application.dbName).findOne(session("group_id"));

    VMModel jenkins = VMHelper.getVMs(new BasicDBObject("group_id", company.getId()).append("jenkins", true)).iterator().next();

    String gitlabToken = VMHelper.getSystemProperty("gitlab-api-token");

    GitlabAPI gitlabAPI = new GitlabAPI("http://" + jenkins.getPublicIP(), gitlabToken);

    String gitName = testName + "-" + UUID.randomUUID();

    GitlabProject gitProject = null;

    if (TestProjectModel.FUNCTIONAL.equals(testType)) {
      gitProject = createGitProject(gitlabAPI, gitName);
    }

    if (TestProjectModel.PERFORMANCE.equals(testType)) {
      gitProject = factory.createProject(gitlabAPI, company.getString("name"), gitName);
    }

    String gitSshUrl = gitProject.getSshUrl().replace("git.sme.org", jenkins.getPublicIP());

    String gitHttpUrl = gitProject.getHttpUrl().replace("git.sme.org", jenkins.getPublicIP());

    TestProjectModel project = new TestProjectModel(
        gitProject.getId(),
        testName, 
        currentGroup.getId(), 
        currentUser.getId(), 
        testType, 
        gitSshUrl,
        gitHttpUrl,
        null,
        script.toString().getBytes("UTF-8"));

    project.put("status", JenkinsJobStatus.Ready.toString());
    project.put("jenkins_id", jenkins.getId());


    TestProjectHelper.createProject(project);
    
    //
    gitlabAPI.createFile(gitProject, "src/test/jmeter/script.jmx", "master", script.toString(), "Snapshot 1");

    GitlabCommit commit = gitlabAPI.getCommits(gitProject, "master").get(0);

    script.put("_id", commit.getId());
    script.put("project_id", project.getId());
    script.put("commit", commit.getTitle());
    script.put("index", 1);
    script.put("status", JenkinsJobStatus.Ready.toString());

    JMeterScriptHelper.createScript(script);
    
    return redirect(routes.PerformanceController.index());
  }
  
  public static Result doUpdateWizard(String projectId) throws Exception {
    TestProjectModel project = TestProjectHelper.getProjectById(projectId);
    Map<String, String[]> params = request().body().asFormUrlEncoded();
    Set<String> keys = params.keySet();
    
    int samplerCount = 0;
    for (String key : keys) {
      if (key.startsWith("sampler-url[")) samplerCount++;
    }
    
    JMeterFactory factory = new JMeterFactory();
    
    JMeterSampler[] samplers = new JMeterSampler[samplerCount];
    
    for(int i = 0; i < samplerCount; i++) {
      String prefix = "sampler[" + i + "]";

      int paramCount = 0;
      for (String key : keys) {
        if (key.startsWith(prefix + "-param-name")) paramCount++;
      }
      
      JMeterArgument[] args = new JMeterArgument[paramCount];
      for (int j = 0; j < paramCount; j++) {
        String paramName = params.get(prefix + "-param-name[" + j + "]")[0];
        String paramValue = params.get(prefix + "-param-value[" + j + "]")[0];
        args[j] = factory.createArgument(paramName, paramValue);
      }
 
      String samplerName = params.get(prefix)[0];
      String samplerMethod = params.get("sampler-method[" + i + "]")[0];
      String samplerUrl = params.get("sampler-url[" + i + "]")[0];
      
      String assertionText = params.get("sampler-assertion-text[" + i +"]")[0];
      
      if(assertionText != null) assertionText = assertionText.trim();
      
      if (assertionText.isEmpty()) assertionText = null;
      
      long constantTime = Integer.parseInt(params.get("sampler-constant-time[" + i + "]" )[0]) * 1000;
      
      samplers[i] = factory.createHttpRequest(
          Method.valueOf(samplerMethod), 
          samplerName, 
          samplerUrl, assertionText, constantTime, args);
    }
    
    int duration = Integer.parseInt(params.get("duration")[0]);
    int loops = Integer.parseInt(params.get("loops")[0]);
    int rampup = Integer.parseInt(params.get("rampup")[0]);
    int users = Integer.parseInt(params.get("users")[0]);
    
    String testName = params.get("test-name")[0];
    project.put("name", testName);
    
    JMeterScript script = factory.createJmeterScript(testName, loops, users, rampup, duration > 0, duration, samplers);

    String scriptContent = script.toString();
    
    project.put("source_content", scriptContent.getBytes("UTF-8"));
    project.put("status", JenkinsJobStatus.Ready.toString());
    project.put("modified_date", System.currentTimeMillis());
    
    //Update in database
    TestProjectHelper.updateProject(project);
    
    //Create commit on GitLab
    Group company = TestController.getCompany();
    VMModel jenkins = VMHelper.getVMs(new BasicDBObject("group_id", company.getId()).append("jenkins", true)).iterator().next();
    String gitlabToken = VMHelper.getSystemProperty("gitlab-api-token");
    GitlabAPI gitlabAPI = new GitlabAPI("http://" + jenkins.getPublicIP(), gitlabToken);
    
    int snapshotCount = JMeterScriptHelper.getJMeterScript(projectId).size() + 1;
    String commitMsg = "Snapshot " + snapshotCount;
    
    gitlabAPI.updateFile(project.getGitlabProjectId(), 
        "src/test/jmeter/script.jmx", "master", 
        scriptContent, commitMsg);
    
    List<GitlabCommit> commits = gitlabAPI.getCommits(project.getGitlabProjectId(), "master");
    GitlabCommit commit = null;
    for (GitlabCommit self : commits) {
      if (self.getTitle().equals(commitMsg)) commit = self;
    }

    script.put("_id", commit.getId());
    script.put("project_id", project.getId());
    script.put("commit", commit.getTitle());
    script.put("index", snapshotCount);
    script.put("status", JenkinsJobStatus.Ready.toString());

    JMeterScriptHelper.createScript(script);
    
    return redirect(routes.PerformanceController.index());
  }
  public static Result projectWebSocketJs(String type) {
    return ok(views.js.test.project_websocket.render(type)).as("text/javascript");
  }
  
  public static void delete(String projectId) throws IOException  {
    TestProjectModel project = TestProjectHelper.getProjectById(projectId);
    
    TestProjectHelper.removeProject(project);
    JenkinsJobHelper.deleteBuildOfProject(project.getId());
    JMeterScriptHelper.deleteScriptOfProject(project.getId());
    
    VMModel jenkins = VMHelper.getVMByID(project.getString("jenkins_id"));
    String gitlabToken = VMHelper.getSystemProperty("gitlab-api-token");
    GitlabAPI gitlabAPI = new GitlabAPI("http://" + jenkins.getPublicIP(), gitlabToken);
    gitlabAPI.deleteProject(project.getGitlabProjectId());
  }
  
  private static GitlabProject createGitProject(GitlabAPI api, String projectName) throws Exception {
    GitlabProject project = api.getAPI().createProject(projectName);
    
    String url = project.getSshUrl().replace("git.sme.org", api.getHost());
    
    String hash = UUID.randomUUID().toString();
    
    StringBuilder sb = new StringBuilder("ssh-keyscan -H ").append(api.getHost()).append(" >> ~/.ssh/known_hosts").append(" && ");
    sb.append("git config --global user.name 'Administrator'").append(" && ");
    sb.append("git config --global user.email 'admin@local.host'").append(" && ");
    sb.append("rm -rf /tmp/").append(hash).append(" && ");
    sb.append("mkdir /tmp/").append(hash).append(" && ");
    sb.append("cd /tmp/").append(hash).append(" && ");
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
    channel.disconnect();
    session.disconnect();
    return project;
  }
  
  public static void createProjectByUpload(boolean run, String testType) throws Exception {
    MultipartFormData body = request().body().asMultipartFormData();
    DynamicForm form = DynamicForm.form().bindFromRequest();

    String testName = form.get("name");

    FilePart uploaded = body.getFile("uploaded");
    if (uploaded != null) {

      File file = uploaded.getFile();
      JMeterFactory factory = new JMeterFactory();

      Group company = TestController.getCompany();
      User currentUser = UserDAO.getInstance(Application.dbName).findOne(session("user_id"));
      Group currentGroup = GroupDAO.getInstance(Application.dbName).findOne(session("group_id"));

      VMModel jenkins = VMHelper.getVMs(new BasicDBObject("group_id", company.getId()).append("jenkins", true)).iterator().next();

      String gitlabToken = VMHelper.getSystemProperty("gitlab-api-token");

      GitlabAPI gitlabAPI = new GitlabAPI("http://" + jenkins.getPublicIP(), gitlabToken);

      String gitName = testName + "-" + UUID.randomUUID();

      GitlabProject gitProject = null;

      if (TestProjectModel.FUNCTIONAL.equals(testType)) {
        gitProject = createGitProject(gitlabAPI, gitName);
      }

      if (TestProjectModel.PERFORMANCE.equals(testType)) {
        gitProject = factory.createProject(gitlabAPI, company.getString("name"), gitName);
      }

      String gitSshUrl = gitProject.getSshUrl().replace("git.sme.org", jenkins.getPublicIP());

      String gitHttpUrl = gitProject.getHttpUrl().replace("git.sme.org", jenkins.getPublicIP());

      TestProjectModel project = new TestProjectModel(
          gitProject.getId(),
          testName, 
          currentGroup.getId(), 
          currentUser.getId(), 
          testType, 
          gitSshUrl,
          gitHttpUrl,
          uploaded.getFilename(), StringUtil.readStream(new FileInputStream(file)).getBytes());

      project.put("status", run ? JenkinsJobStatus.Initializing.toString() : JenkinsJobStatus.Ready.toString());
      project.put("jenkins_id", jenkins.getId());


      TestProjectHelper.createProject(project);

      try {
        if (TestProjectModel.FUNCTIONAL.equals(testType)) {
          Session session = SSHClient.getSession(jenkins.getPublicIP(), 22, "ubuntu", "ubuntu");
          ChannelExec channel = (ChannelExec) session.openChannel("exec");

          //clone project
          StringBuilder sb = new StringBuilder("cd /tmp && ").append("git clone ").append(gitSshUrl).append(" ").append(project.getId());
          channel.setCommand(sb.toString());
          channel.connect();
          int exitCode = SSHClient.printOut(System.out, channel);
          if (exitCode != 0) throw new RuntimeException("Can not execute command: `" + sb.toString());
          channel.disconnect();

          SSHClient.sendFile(jenkins.getPublicIP(), 22, jenkins.getUsername(), jenkins.getPassword(), 
              "/tmp/" + project.getId(), uploaded.getFilename(), file);

          //        make commit
          sb = new StringBuilder("cd /tmp/").append(project.getId()).append(" && ");
          sb.append("tar xvf ").append(uploaded.getFilename()).append(" && ");
          sb.append("rm ").append(uploaded.getFilename()).append(" && ");
          sb.append("git add -A && git commit -m 'Snapshot 1' && git push origin master");

          channel = (ChannelExec) session.openChannel("exec");
          channel.setCommand(sb.toString());
          channel.connect();

          exitCode = SSHClient.printOut(System.out, channel);
          if (exitCode != 0) throw new RuntimeException("Can not execute command: `" + sb.toString());
          channel.disconnect();

          //disconnect session
          session.disconnect();

          if (run) run(project, project.getId());
        }

        if (TestProjectModel.PERFORMANCE.equals(testType)) {

          FileInputStream fis = new FileInputStream(file);
          String content = StringUtil.readStream(fis);

          JMeterParser parser = factory.createJMeterParser(content);
          JMeterScript script = parser.parse();

          gitlabAPI.createFile(gitProject, "src/test/jmeter/script.jmx", "master", script.toString(), "Snapshot 1");

          GitlabCommit commit = gitlabAPI.getCommits(gitProject, "master").get(0);

          script.put("_id", commit.getId());
          script.put("project_id", project.getId());
          script.put("commit", commit.getTitle());
          script.put("index", 1);
          script.put("status", run ? JenkinsJobStatus.Initializing.toString() : JenkinsJobStatus.Ready.toString());

          JMeterScriptHelper.createScript(script);

          if (run) TestController.run(project, script.getString("_id"));
        }
      } catch (Exception e) {
        TestProjectHelper.removeProject(project);
        gitlabAPI.deleteProject(gitProject);
        throw new RuntimeException(e);
      }
    }
  }
  
  public static void run(final TestProjectModel project, final String snapshotId) throws UserManagementException {
    
    final VMModel jenkins = VMHelper.getVMByID(project.getString("jenkins_id"));

    final Group company = getCompany();

    List<VMModel> list = VMHelper.getReadyVMs(company.getId(), new BasicDBObject("gui", TestProjectModel.PERFORMANCE.equals(project.getType()) ? false : true));

    final JMeterScript snapshot = new JMeterScript();
    if (TestProjectModel.PERFORMANCE.equals(project.getType())) {
      snapshot.from(JMeterScriptHelper.getJMeterScriptById(snapshotId));
    }
    
    //remove last build
    //JenkinsJobHelper.deleteBuildOfSnapshot(snapshot.getString("_id"));
    
    if (list.isEmpty()) {
      Thread thread = new Thread(new Runnable() {
        @Override
        public void run() {
          try {
            JenkinsJobModel job = JenkinsJobHelper.getJobById(snapshotId);
            
            if (job == null) {
              job = new JenkinsJobModel(
                  JenkinsJobHelper.getCurrentBuildIndex(project.getId()) + 1, 
                  snapshotId, 
                  project.getId(), 
                  null, 
                  jenkins.getId(), 
                  project.getType());
              
              JenkinsJobHelper.createJenkinsJob(job);
            } else {
              job.put("status", JenkinsJobStatus.Initializing.toString());
              job.put("log", null);
              JenkinsJobHelper.updateJenkinsJob(job);
            }

            project.put("status", job.getStatus().toString());
            TestProjectHelper.updateProject(project);

            if (TestProjectModel.PERFORMANCE.equals(project.getType())) {
              snapshot.put("status", job.getStatus().toString());
              JMeterScriptHelper.updateJMeterScript(snapshot);
            }

            VMModel vm = TestProjectModel.PERFORMANCE.equals(project.getType()) ? VMCreator.createNormalNonGuiVM(company) : VMCreator.createNormalGuiVM(company);
            job.put("vm_id", vm.getId());
            JenkinsJobHelper.updateJenkinsJob(job);

          } catch (Exception e) {
            Logger.debug("has an error when run functional project", e);
          }
        }
      });
      thread.start();
    } else {
      VMModel vm = list.get(0);
      
      JenkinsJobModel job = JenkinsJobHelper.getJobById(snapshotId);
      if (job == null) {
        job = new JenkinsJobModel(
            JenkinsJobHelper.getCurrentBuildIndex(project.getId()) + 1, 
            snapshotId, 
            project.getId(), 
            vm.getId(), 
            jenkins.getId(), 
            project.getType());
        JenkinsJobHelper.createJenkinsJob(job);
      } else {
        job.put("status", JenkinsJobStatus.Initializing.toString());
        job.put("vm_id", vm.getId());
        job.put("log", null);
        JenkinsJobHelper.updateJenkinsJob(job);
      }

      project.put("status", job.getStatus().toString());
      TestProjectHelper.updateProject(project);

      if (TestProjectModel.PERFORMANCE.equals(project.getType())) {
        snapshot.put("status", job.getStatus().toString());
        JMeterScriptHelper.updateJMeterScript(snapshot);
      }
    }
  }
  
  public static void stop(TestProjectModel project, String projectId) throws UserManagementException {
    
    final VMModel jenkins = VMHelper.getVMByID(project.getString("jenkins_id"));

    final Group company = getCompany();
    final JMeterScript snapshot = new JMeterScript();
    List<JMeterScript> listScript = JMeterScriptHelper.getJMeterScript(projectId);
    if (TestProjectModel.PERFORMANCE.equals(project.getType())) {
      snapshot.from(listScript.get(listScript.size()-1));
    }
    List<VMModel> list = VMHelper.getReadyVMs(company.getId(), new BasicDBObject("gui", TestProjectModel.PERFORMANCE.equals(project.getType()) ? false : true));
      
    //remove last build
    //JenkinsJobHelper.deleteBuildOfSnapshot(snapshot.getString("_id"));
      VMModel vm = list.get(0);
      
      JenkinsJobModel job = JenkinsJobHelper.getJobById(projectId);
      if (job == null) {
        job = new JenkinsJobModel(
            JenkinsJobHelper.getCurrentBuildIndex(project.getId()) + 1, 
            projectId, 
            project.getId(), 
            vm.getId(), 
            jenkins.getId(), 
            project.getType());
        JenkinsJobHelper.createJenkinsJob(job);
      } else {
        job.put("status", JenkinsJobStatus.Aborted.toString());
        job.put("vm_id", vm.getId());
        job.put("log", null);
        JenkinsJobHelper.updateJenkinsJob(job);
      }

      project.put("status", job.getStatus().toString());
      TestProjectHelper.updateProject(project);

      if (TestProjectModel.PERFORMANCE.equals(project.getType())) {
        snapshot.put("status", job.getStatus().toString());
        JMeterScriptHelper.updateJMeterScript(snapshot);
      }
    
  }
  
  public static Result createNewProject(String type) {
    return ok(index.render(type, newproject.render(type)));
  }
  
  public static WebSocket<JsonNode> projectLog(final String type, final String sessionId, final String currentUserId) {
    return new WebSocket<JsonNode>() {

      @Override
      public void onReady(play.mvc.WebSocket.In<JsonNode> in, play.mvc.WebSocket.Out<JsonNode> out) {
        try {
          Await.result(ask(ProjectLogActor.actor, new ProjectChannel(sessionId, currentUserId, type, out), 1000), Duration.create(1, TimeUnit.SECONDS));
        } catch (Exception e) {
          Logger.debug("Can not create akka for project log actor", e);
        }
      }
    };
  }
  
  public static WebSocket<JsonNode> projectStatus(final String type, final String sessionId, final String currentUserId) {
    return new WebSocket<JsonNode>() {
      @Override
      public void onReady(play.mvc.WebSocket.In<JsonNode> in, play.mvc.WebSocket.Out<JsonNode> out) {
        try {
          Await.result(ask(ProjectStatusActor.actor, new ProjectChannel(sessionId, currentUserId, type, out), 1000), Duration.create(1, TimeUnit.SECONDS));
        } catch (Exception e) {
          Logger.debug("Can not create akka for project status actor", e);
        }
      }
    };
  }
  
  public static String getColorByStatus(String status) {
    JenkinsJobStatus _status = JenkinsJobStatus.valueOf(status);
    switch (_status) {
    case Ready:
    case Completed:  
      return "green";
    case Running:
      return "cyan";
    case Aborted:
      return "orange";
    case Errors:
      return "red";
    case Initializing:
      return "blue";
    default:
      return "";
    }
  }
  
  public static Html groupMenuList(String type) throws UserManagementException {
    scala.collection.mutable.StringBuilder sb = new scala.collection.mutable.StringBuilder();
    List<Group> groups = getAvailableGroups(type, session("user_id"));
    
    for (Group group : groups) {
      buildGroupPath(sb, group);
    }
    
    return new Html(sb);
  }
  
  public static Result getProjectList() throws UserManagementException {
    String type = request().getQueryString("type");
    String group_id = request().getQueryString("group");
    String userText = request().getQueryString("user");

    return ok(getProjectListHtml(type.toString(), group_id, userText));
  }
  
  public static Html getProjectListHtml(String type, String group_id, String userText) throws UserManagementException {
    scala.collection.mutable.StringBuilder sb = new scala.collection.mutable.StringBuilder();
    
    Set<TestProjectModel> set = new HashSet<TestProjectModel>();
    if (group_id == null) {
      for (Group group : getAvailableGroups(type, session("user_id"))) {
        set.addAll(TestProjectHelper.getProject(new BasicDBObject("group_id", group.getId()).append("type", type)));
      }
    }
    
    List<TestProjectModel> projects = new ArrayList<TestProjectModel>(set);
    Collections.sort(projects, new Comparator<TestProjectModel>() {
      @Override
      public int compare(TestProjectModel o1, TestProjectModel o2) {
        return (int)(o2.getLong("created_date") - o1.getLong("created_date"));
      }
    });
    
    for (TestProjectModel p : projects) {
      sb.append(project.render(p));
    }
    return new Html(sb);
  }
  
  public static List<Group> getAvailableGroups(String testType, String currentUserId) throws UserManagementException {
    
    User currentUser = UserDAO.getInstance(Application.dbName).findOne(currentUserId);
    
    Feature feature = FeatureDAO.getInstance(Application.dbName).find(new BasicDBObject("name", testType)).iterator().next();
    Operation perfAdOperation = null;
    Operation perfTestOperation = null;
    for (Operation op : feature.getOperations()) {
      if (op.getName().equals("Administration")) {
        perfAdOperation = op;
      } else if (op.getName().equals("Test")) {
        perfTestOperation = op;
      }
    }
    
    List<Role> adRoles = new ArrayList<Role>();
    List<Role> testRoles = new ArrayList<Role>();
    
    for (Role role : currentUser.getRoles()) {
      for (Permission per : role.getPermissions()) {
        Feature f = per.getFeature();
        Operation op = per.getOpertion();
        if (f.equals(feature)) {
          if (op.equals(perfAdOperation))
            adRoles.add(role);
          else if (op.equals(perfTestOperation)) {
            testRoles.add(role);
          }
        }
      }
    }
    
    List<Group> adminGroups = getGroupsHasPermission(currentUser, adRoles);
    List<Group> testGroups = getGroupsHasPermission(currentUser, testRoles);
    Set<Group> set = new HashSet<Group>();
    
    for (Group group : adminGroups) {
      set.add(group);
      set.addAll(group.getAllChildren());
    }
    set.addAll(testGroups);
    List<Group> groups = new ArrayList<Group>(set);
    
    Collections.sort(groups, new Comparator<Group>() {
      @Override
      public int compare(Group o1, Group o2) {
        return o1.getInt("level") - o2.getInt("level");
      }
    });
    
    return groups;
  }
  
  private static void buildGroupPath(scala.collection.mutable.StringBuilder sb, Group group) throws UserManagementException {
    sb.append("<li><a href='javascript:void(0);'>");
    LinkedList<Group> parents = GroupDAO.getInstance(Application.dbName).buildParentTree(group);
    for (Group p : parents) {
      sb.append(" / ").append(p.getString("name"));
    }
    sb.append(" / ").append(group.getString("name"));
    sb.append("</a></li>");
  }
  
  private static List<Group> getGroupsHasPermission(User currentUser, List<Role> adRoles) {
    List<Group> groups = currentUser.getGroups();
    
    List<Group> holder = new ArrayList<Group>();
    for (Role role : adRoles) {
      Group group = role.getGroup();
      if (groups.contains(group)) holder.add(group);
    }
    
    return holder;
  }
  
  public static Group getCompany() throws UserManagementException {
    User currentUser = UserDAO.getInstance(Application.dbName).findOne(session("user_id"));
    List<Group> groups = currentUser.getGroups();
    for (Group group : groups) {
      if (group.getInt("level") == 1) return group;
    }
    
    return null;
  }
}
