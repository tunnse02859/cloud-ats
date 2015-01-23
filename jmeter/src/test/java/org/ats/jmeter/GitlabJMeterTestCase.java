/**
 * 
 */
package org.ats.jmeter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.ats.gitlab.AbstractGitlabTestCase;
import org.ats.jenkins.JenkinsMavenJob;
import org.gitlab.api.models.GitlabCommit;
import org.gitlab.api.models.GitlabProject;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Oct 13, 2014
 */
public class GitlabJMeterTestCase extends AbstractGitlabTestCase {

  //@Test
  public void testJMeter() throws Exception {
    JMeterFactory factory = new JMeterFactory();
    GitlabProject project = factory.createProject(api, "com.test", "jmeter", "ubuntu", "ubuntu");

    String jmeterScript = factory.createJmeterScript("Cloud-ATS Login", 1, 100, 5, false, 0, 
        factory.createHttpGet("Signin Page", "http://172.27.4.48:9000/signin", null, 0),
        factory.createHttpPost("Login", "http://172.27.4.48:9000/signin", null, 0, factory.createArgument("email", "root@system.com"), factory.createArgument("password", "admin")),
        factory.createHttpGet("Organization Page", "http://172.27.4.48:9000/portal/o", null, 0),
        factory.createHttpGet("Signout", "http://172.27.4.48:9000/signout", null, 0)).toString();
    
    api.createFile(project, "src/test/jmeter/script.jmx", "master", jmeterScript, "Snapshot 1");
    
    jmeterScript = factory.createJmeterScript("Cloud-ATS Login", 1, 200, 5, false, 0, 
        factory.createHttpGet("Signin Page", "http://172.27.4.48:9000/signin", null, 0),
        factory.createHttpPost("Login", "http://172.27.4.48:9000/signin", null, 0, factory.createArgument("email", "root@system.com"), factory.createArgument("password", "admin")),
        factory.createHttpGet("Organization Page", "http://172.27.4.48:9000/portal/o", null, 0),
        factory.createHttpGet("Signout", "http://172.27.4.48:9000/signout", null, 0)).toString();
    
    FileOutputStream fos = new FileOutputStream("target/test.jmx");
    fos.write(jmeterScript.getBytes("UTF-8"));
    fos.close();
    
    api.updateFile(project, "src/test/jmeter/script.jmx", "master", jmeterScript, "Snapshot 2");

    List<GitlabCommit> commits = api.getCommits(project, "master");
    GitlabCommit snapshot1 = null;
    for (GitlabCommit commit : commits) {
      if ("Snapshot 1".equals(commit.getTitle())) snapshot1 = commit;
    }
    
    Assert.assertNotNull(snapshot1);
    
    /*String gitUrl = project.getHttpUrl().replace("git.sme.org", "172.27.4.77");
    JenkinsMaster master = new JenkinsMaster("172.27.4.77", "http", 8080);

    JenkinsMavenJob job1 = new JenkinsMavenJob(master, "jmeter master", "master", gitUrl, "master","clean verify -Pperformance -Dtest.server=172.27.4.83", null);
    build(job1);
    
    JenkinsMavenJob job2 = new JenkinsMavenJob(master, "jmeter snapshot1", "master", gitUrl, snapshot1.getId(),
        "clean verify -Pperformance -Dtest.server=172.27.4.83", null);
    build(job2);
    
    job1.delete();
    job2.delete();*/

    api.deleteProject(project);
  }
  
  private void build(JenkinsMavenJob job) throws Exception {
    int buildNumber = job.submit();    
    Assert.assertTrue(buildNumber != -1);
    
    int start = 0;
    int last = 0;
    byte[] bytes = null;
    

    while(job.isBuilding(buildNumber, System.currentTimeMillis(), 30 * 1000)) {

      bytes = job.getConsoleOutput(buildNumber, start);
      last = bytes.length;
      byte[] next = new byte[last - start];

      System.arraycopy(bytes, start, next, 0, next.length);

      start += (last - start);

      if (next.length > 0) { 
        String output = new String(next);
        System.out.println(output.trim());
        if (output.indexOf("channel stopped") != -1) break;
      }
    }
    
    Assert.assertEquals("SUCCESS", job.getStatus(buildNumber));
  }
}
