/**
 * 
 */
package org.ats.knife;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.util.Queue;

import org.ats.common.ssh.SSHClient;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSchException;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Apr 26, 2014
 */
public class Knife {
  
  /** .*/
  String workstation;
  
  /** .*/
  String server;
  
  /** .*/
  String username;
  
  /** .*/
  String password;

  @Deprecated
  private Knife() throws IOException {
    InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("knife.properties");
    Properties properties = new Properties();
    properties.load(is);
    this.workstation = properties.getProperty("chef-workstation");
    this.username = properties.getProperty("username");
    this.password = properties.getProperty("password");
  }
  
  public Knife(String workstation, String username, String password) {
    this.workstation = workstation;
    this.username = username;
    this.password = password;
  }
  
  public boolean bootstrap(String nodeIP, String nodeName, String userName, String password, String... recipes) throws JSchException, IOException {
    return bootstrap(nodeIP, nodeName, userName, password, null, System.err, recipes);
  }
  
  public boolean bootstrap(String nodeIP, String nodeName, String userName, String password, InputStream is, OutputStream err, String... recipes) throws JSchException, IOException {
    StringBuilder sb = new StringBuilder("knife bootstrap ").append(nodeIP);
    sb.append(" -x ").append(userName).append(" -P ").append(password).append(" --sudo --use-sudo-password --no-host-key-verify -N ").append(nodeName);
    if(recipes != null && recipes.length != 0) {
      sb.append(" -r ");
      for (int i = 0; i < recipes.length; i++) {
        sb.append(recipes[i]);
        if (i < recipes.length - 1) sb.append(",");
      }
    }
    Channel channel = SSHClient.execCommand(workstation, 22, username, password, sb.toString(), is, err);
    int exitCode = SSHClient.printOut(System.out, channel);
    return exitCode == 0;
  }
  
  public boolean bootstrap(String nodeIP, String nodeName, String userName, String password, Queue<String> queue, String... recipes) throws JSchException, IOException {
    StringBuilder sb = new StringBuilder("knife bootstrap ").append(nodeIP);
    sb.append(" -x ").append(userName).append(" -P ").append(password).append(" --sudo --use-sudo-password --no-host-key-verify -N ").append(nodeName);
    System.out.println("Knife command: " + sb.toString());
    if(recipes != null && recipes.length != 0) {
      sb.append(" -r ");
      for (int i = 0; i < recipes.length; i++) {
        sb.append(recipes[i]);
        if (i < recipes.length - 1) sb.append(",");
      }
    }
    Channel channel = SSHClient.execCommand(workstation, 22, username, password, sb.toString(), null, System.err);
    int exitCode = SSHClient.printOut(queue, channel);
    return exitCode == 0;
  }
  
  public boolean deleteNode(String nodeName) throws JSchException, IOException {
    StringBuilder sb = new StringBuilder("knife node delete ").append(nodeName).append(" -y && knife client delete ").append(nodeName).append(" -y");
    Channel channel = SSHClient.execCommand(workstation, 22, username, password, sb.toString(), null, System.err);
    int exitCode = SSHClient.printOut(System.out, channel);
    return exitCode == 0;
  }

  @Deprecated
  public static Knife getInstance() throws IOException {
    return  new Knife();
  }
}
