/**
 * 
 */
package helpertest;

import helpervm.VMHelper;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.ats.jenkins.JenkinsMaster;
import org.ats.jenkins.JenkinsMavenJob;
import org.ats.jmeter.models.JMeterScript;

import play.Logger;
import models.test.JenkinsJobModel;
import models.test.JenkinsJobStatus;
import models.test.TestProjectModel;
import models.test.TestProjectModel.TestProjectType;
import models.vm.VMModel;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Oct 26, 2014
 */
public class JenkinsJobExecutor {

  /** .*/
  private ScheduledExecutorService service = null;
  
  private JenkinsJobExecutor() {}
  
  private static JenkinsJobExecutor instance = null;
  
  public final static JenkinsJobExecutor getInstance() { 
    return instance == null ? (instance = new JenkinsJobExecutor()) : instance;
  }

  public void start() {
    System.out.println("Start Jenkins Job Executor");

    this.service = Executors.newSingleThreadScheduledExecutor();

    this.service.scheduleAtFixedRate(new Runnable() {

      public void run() {
        DBObject source = JenkinsJobHelper.getCollection().findOne(new BasicDBObject("status", JenkinsJobStatus.Initializing.toString()));
        
        if (source == null) return;

        JenkinsJobModel jobModel = new JenkinsJobModel().from(source);
        
        jobModel.put("status", JenkinsJobStatus.Running.toString());
        JenkinsJobHelper.updateJenkinsJob(jobModel);
        
        VMModel jenkins = VMHelper.getVMByID(jobModel.getString("jenkins_id"));
        VMModel vm = VMHelper.getVMByID(jobModel.getString("vm_id"));

        TestProjectModel project = TestHelper.getProjectById(TestProjectType.valueOf(jobModel.getString("job_type")), jobModel.getString("project_id"));

        JenkinsMaster jenkinsMaster = new JenkinsMaster(jenkins.getPublicIP(), "http", 8080);

        switch (project.getType()) {
        case performance:
          JMeterScript snapshot = JMeterScriptHelper.getJMeterScriptById(jobModel.getString("snapshot_id"));

          try {

            JenkinsMavenJob job = new JenkinsMavenJob(jenkinsMaster, jobModel.getString("_id") , "master", 
                project.geGitHttpUrl(), snapshot.getString("_id"), "clean verify -Pperformance -Dtest.server=" + vm.getPublicIP(), null);

            int buildNumber = job.submit();
            snapshot.put("status", JenkinsJobStatus.Running.toString());
            JMeterScriptHelper.updateJMeterScript(snapshot);
            
            project.put("status", JenkinsJobStatus.Running.toString());
            TestHelper.updateProject(project);
            while (job.isBuilding(buildNumber)) {
              Thread.sleep(1000);
            }
            
            long time = System.currentTimeMillis();
            
            if ("SUCCESS".equals(job.getStatus(buildNumber))) {
              jobModel.put("status", JenkinsJobStatus.Completed.toString());
              JenkinsJobHelper.updateJenkinsJob(jobModel);
              
              snapshot.put("status", JenkinsJobStatus.Completed.toString());
              snapshot.put("last_build", time);
              JMeterScriptHelper.updateJMeterScript(snapshot);
              
              project.put("status", JenkinsJobStatus.Completed.toString());
              project.put("last_build", time);
              TestHelper.updateProject(project);
              
            } else if ("FAILURE".equals(job.getStatus(buildNumber))) {
              jobModel.put("status", JenkinsJobStatus.Errors.toString());
              JenkinsJobHelper.updateJenkinsJob(jobModel);
              
              snapshot.put("status", JenkinsJobStatus.Errors.toString());
              snapshot.put("time", time);
              JMeterScriptHelper.updateJMeterScript(snapshot);
              
              project.put("status", JenkinsJobStatus.Errors.toString());
              project.put("time", time);
              TestHelper.updateProject(project);
            } else if ("UNSTABLE".equals(job.getStatus(buildNumber))) {
              //TODO
            }
            
          } catch (Exception e) {
            jobModel.put("status", JenkinsJobStatus.Errors.toString());
            JenkinsJobHelper.updateJenkinsJob(jobModel);
            
            snapshot.put("status", JenkinsJobStatus.Errors.toString());
            JMeterScriptHelper.updateJMeterScript(snapshot);
            
            project.put("status", JenkinsJobStatus.Errors.toString());
            TestHelper.updateProject(project);
            
            e.printStackTrace();
            Logger.debug("An error occurs when dequeue a jenkins maven job", e);
          }
          break;
        default:
          break;
        }
      }
    }, 0, 1000, TimeUnit.MILLISECONDS);
  }
  
  private void updateResult(TestProjectType type, JenkinsJobModel job, JenkinsMaster jenkinsMaster) {
  }
  
  public void stop() {
    this.service.shutdown();
  }
}
