/**
 * 
 */
package org.ats.jmeter;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.ats.common.http.HttpURL;
import org.ats.common.ssh.SSHClient;
import org.ats.gitlab.GitlabAPI;
import org.gitlab.api.models.GitlabProject;
import org.rythmengine.Rythm;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Oct 8, 2014
 */
public class JmeterFactory {

  private String argument;
  
  private String arguments;
  
  private String jmeter;
  
  private String pom;
  
  private String sampleGet;
  
  private String samplePost;
  
  public JmeterFactory() throws IOException {
    this(null);
  }
  
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
  
  public JmeterFactory(String templateSource) throws IOException {
    this.argument = templateSource != null ?
        asString(new FileInputStream(templateSource + "/argument.xml")) :  
        asString(Thread.currentThread().getContextClassLoader().getResourceAsStream("argument.xml"));
        
    this.arguments = templateSource != null ?
        asString(new FileInputStream(templateSource + "/arguments.xml")) : 
        asString(Thread.currentThread().getContextClassLoader().getResourceAsStream("arguments.xml"));
        
    this.jmeter = templateSource != null ?
        asString(new FileInputStream(templateSource + "/jmeter.xml")) :
        asString(Thread.currentThread().getContextClassLoader().getResourceAsStream("jmeter.xml"));
        
    this.pom = templateSource != null ?
        asString(new FileInputStream(templateSource + "/pom.xml")) :
        asString(Thread.currentThread().getContextClassLoader().getResourceAsStream("pom.xml"));
        
    this.sampleGet = templateSource != null ?
        asString(new FileInputStream(templateSource + "/sample-get.xml")) :
        asString(Thread.currentThread().getContextClassLoader().getResourceAsStream("sample-get.xml"));
        
    this.samplePost = templateSource != null ?
        asString(new FileInputStream(templateSource + "/sample-post.xml")) :
        asString(Thread.currentThread().getContextClassLoader().getResourceAsStream("sample-post.xml"));
  }
  
  public String createPom(String groupId, String artifactId) {
    Map<String, Object> params = ParamBuilder.start().put("groupId", groupId).put("artifactId", artifactId).build();
    return Rythm.render(this.pom, params);
  }
  
  public String createJmeterScript(int loops, int numberThreads, int ramUp, boolean scheduler, int duration, String ... samplers) {
    StringBuilder sb = new StringBuilder();
    for (String sampler : samplers) {
      sb.append(sampler).append('\n');
    }
    
    ParamBuilder params = ParamBuilder.start()
      .put("loops", loops)
      .put("numberThreads", numberThreads)
      .put("ramUp", ramUp)
      .put("scheduler", scheduler)
      .put("duration", duration)
      .put("samplers", sb.toString());
    
    return Rythm.render(this.jmeter, params.build());
  }
  
  public String createArgument(String paramName, String paramValue) {
    Map<String, Object> params = ParamBuilder.start().put("paramName", paramName).put("paramValue", paramValue).build();
    return Rythm.render(this.argument, params);
  }
  
  public String createArguments(String ... arguments) {
    StringBuilder sb = new StringBuilder();
    for (String argument : arguments) {
      sb.append(argument).append('\n');
    }
    return Rythm.render(this.arguments, sb.toString());
  }
  
  public String createHttpGet(String name, String url, String assertionText, long contantTime, String ... arguments) throws UnsupportedEncodingException {
    return createHttpRequest("GET", name, url, assertionText, contantTime, arguments);
  }
  
  public String createHttpPost(String name, String url, String assertionText, long contantTime, String ... arguments) throws UnsupportedEncodingException {
    return createHttpRequest("POST", name, url, assertionText, contantTime, arguments);
  }
  
  public String createHttpRequest(String method, String name, String url, String assertionText, long contantTime, String ... arguments) throws UnsupportedEncodingException {
    HttpURL httpUrl = new HttpURL(url);
    ParamBuilder builder = ParamBuilder.start()
        .put("name", name)
        .put("host", httpUrl.getHost())
        .put("port", httpUrl.getPort())
        .put("protocol", httpUrl.getProtocol())
        .put("path", httpUrl.getPath());
    
    List<String> list = (arguments == null || arguments.length == 0) ? new ArrayList<String>() : Arrays.asList(arguments);
    
    if (!httpUrl.getQueryParameters().isEmpty()) {
      for (Map.Entry<String, String> entry : httpUrl.getQueryParameters().entrySet()) {
        list.add(createArgument(entry.getKey(), entry.getValue()));
      }
    }
    String s = createArguments(list.toArray(new String[list.size()]));
    builder.put("arguments", s);
    
    if (assertionText != null && !assertionText.trim().isEmpty()) builder.put("assertionText", assertionText);
    
    if (contantTime > 0) builder.put("contantTime", contantTime);
    
    if("GET".equals(method)) {
      return Rythm.render(this.sampleGet, builder.build());
    } else if ("POST".equals(method)) {
      builder.put("method", "POST");
    } else if ("PUT".equals(method)) {
      builder.put("method", "PUT");
    } else if ("DELETE".equals(method)) {
      builder.put("method", "DELETE");
    }
    
    return Rythm.render(this.samplePost, builder.build());
  }
  
  private String asString(InputStream is) throws IOException {
    BufferedInputStream bis = new BufferedInputStream(is);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    byte[] buff = new byte[1024];
    for(int l = bis.read(buff); l != -1; l = bis.read(buff)) {
      baos.write(buff, 0, l);
    }
    bis.close();
    return new String(baos.toByteArray(), "UTF-8");
  }
}
