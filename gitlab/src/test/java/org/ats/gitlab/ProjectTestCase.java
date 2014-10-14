/**
 * 
 */
package org.ats.gitlab;

import java.io.FileNotFoundException;
import java.util.List;

import junit.framework.Assert;

import org.ats.common.ssh.SSHClient;
import org.gitlab.api.models.GitlabCommit;
import org.gitlab.api.models.GitlabProject;
import org.junit.Test;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.Session;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Oct 10, 2014
 */
public class ProjectTestCase extends AbstractGitlabTestCase {
  
  private String command = "ssh-keyscan -H 172.27.4.77 >> ~/.ssh/known_hosts && " +
      "git config --global user.name 'Administrator' && git config --global user.email 'admin@local.host' && " +
      "rm -rf /tmp/test && mkdir /tmp/test && cd /tmp/test && git init && touch README && git add README && git commit -m 'first commit'" +
   "&& git remote add origin git@172.27.4.77:root/test.git && "
   + "git push -u origin master";

  @Test
  public void testCreateAndDeleteProject() throws Exception {
    try {
      api.getAPI().getProject(-1);
      Assert.fail();
    } catch (FileNotFoundException e) {}
    
    GitlabProject project = api.getAPI().createProject("dummy");
    Assert.assertNotNull(api.getAPI().getProject(project.getId()));
    
    try {
      api.getAPI().createGroup("test");
      Assert.fail();
    } catch (FileNotFoundException e) { }
    
    api.deleteProject(project);
    
    try {
      api.getAPI().getProject(project.getId());
      Assert.fail();
    } catch (FileNotFoundException e) {}
  }
  
  @Test
  public void testCRUDFile() throws Exception {
    GitlabProject project = api.getAPI().createProject("test");
    
    Session session = SSHClient.getSession("172.27.4.77", 22, "ubuntu", "ubuntu");
    ChannelExec channel = (ChannelExec) session.openChannel("exec");
       
    channel.setCommand(command);
    channel.connect();
    
    int exitCode = SSHClient.printOut(System.out, channel);
    Assert.assertEquals(0, exitCode);
    
    api.createFile(project.getId(), "test/core/test.txt", "master", "Hello World", "Snapshot 1");
    
    String content = api.getRawFileAsString(project, "master", "test/core/test.txt");
    Assert.assertEquals("Hello World", content);
    
    api.updateFile(project, "test/core/test.txt", "master", "Hello World Updated", "Snapshot 2");
    content = api.getRawFileAsString(project, "master", "test/core/test.txt");
    Assert.assertEquals("Hello World Updated", content);
    
    List<GitlabCommit> commits = api.getCommits(project, "master");
    Assert.assertEquals(3, commits.size());
    
    GitlabCommit init = commits.get(2);
    Assert.assertEquals("first commit", init.getTitle());
    
    GitlabCommit snapshot1 = commits.get(1);
    Assert.assertEquals("Snapshot 1", snapshot1.getTitle());
    
    GitlabCommit snapshot2 = commits.get(0);
    Assert.assertEquals("Snapshot 2", snapshot2.getTitle());
    
    Assert.assertEquals("Hello World", api.getFile(project, "test/core/test.txt", snapshot1.getId()).getContent());
    Assert.assertEquals("Hello World Updated", api.getFile(project, "test/core/test.txt", snapshot2.getId()).getContent());
    
    api.deleteProject(project);
  }
  
  @Test
  public void searchProject() throws Exception {
    List<GitlabProject> projects = api.searchProjects("performance");
    Assert.assertEquals(1, projects.size());
    Assert.assertEquals("performance", projects.get(0).getName());
    
    projects = api.searchProjects("simple");
    Assert.assertEquals(2, projects.size());
    Assert.assertEquals("simple-nlp", projects.get(0).getName());
    Assert.assertEquals("simple-crawler", projects.get(1).getName());
  }
}
