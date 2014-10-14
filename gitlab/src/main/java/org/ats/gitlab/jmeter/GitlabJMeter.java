/**
 * 
 */
package org.ats.gitlab.jmeter;

import java.io.IOException;

import org.ats.common.ssh.SSHClient;
import org.ats.gitlab.GitlabAPI;
import org.ats.jmeter.JmeterFactory;
import org.gitlab.api.models.GitlabProject;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Oct 13, 2014
 */
public class GitlabJMeter {

  public GitlabProject createProject(GitlabAPI api, String companyName, String projectName) throws IOException, JSchException {
    GitlabProject project = api.getAPI().createProject(projectName);
    
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
    sb.append("git remote add origin git@").append(api.getHost()).append(":root/").append(projectName).append(".git").append(" && ");
    sb.append("git push -u origin master");
  
    Session session = SSHClient.getSession(api.getHost(), 22, "ubuntu", "ubuntu");
    ChannelExec channel = (ChannelExec) session.openChannel("exec");
       
    channel.setCommand(sb.toString());
    channel.connect();
    
    SSHClient.printOut(System.out, channel);

    JmeterFactory factory = new JmeterFactory();
    String pom = factory.createPom(companyName, projectName);
    api.createFile(project, "pom.xml", "master", pom, "init pom");
    
    return project;
  }
  
}
