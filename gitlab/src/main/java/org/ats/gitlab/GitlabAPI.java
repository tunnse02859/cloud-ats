/**
 * 
 */
package org.ats.gitlab;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.http.HttpResponse;
import org.ats.common.http.HttpClientFactory;
import org.ats.common.http.HttpClientUtil;
import org.gitlab.api.http.Query;
import org.gitlab.api.models.GitlabCommit;
import org.gitlab.api.models.GitlabProject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Oct 6, 2014
 */
public class GitlabAPI {

  /** .*/
  private final org.gitlab.api.GitlabAPI api;
  
  /** .*/
  private final String host;
  
  public GitlabAPI(String hostUrl, String apiToken) {
   this.api = org.gitlab.api.GitlabAPI.connect(hostUrl, apiToken);
   String host = hostUrl.substring(hostUrl.indexOf("://") + 3);
   if (host.indexOf(':') != -1) this.host = host.substring(0, host.indexOf(':'));
   else this.host = host;
  }
  
  public GitlabAPI(org.gitlab.api.GitlabAPI api) throws IOException {
    this.api = api;
    String hostUrl = api.getUrl("").toString();
    this.host = hostUrl.substring(hostUrl.indexOf("://") + 3, hostUrl.lastIndexOf('/'));
  }
  
  public String getHost() {
    return this.host;
  }
  
  public org.gitlab.api.GitlabAPI getAPI() {
    return api;
  }
  
  public void createFile(GitlabProject project, String filePath, String branchName, String content, String commitMsg) throws IOException {
    this.createFile(project.getId(), filePath, branchName, content, commitMsg);
  }
  
  public void createFile(Integer projectId, String filePath, String branchName, String content, String commitMsg) throws IOException {
    String tailUrl = GitlabProject.URL + "/" + projectId + GitlabFile.URL;
    api.dispatch()
      .with("file_path", filePath)
      .with("branch_name", branchName)
      .with("content", content)
      .with("commit_message", commitMsg)
      .to(tailUrl,Void.class);
  }

  public void updateFile(GitlabProject project, String filePath, String branchName, String content, String commitMsg) throws IOException {
    this.updateFile(project.getId(), filePath, branchName, content, commitMsg);
  }
  
  public void updateFile(Integer projectId, String filePath, String branchName, String content, String commitMsg) throws IOException {
    String tailUrl = GitlabProject.URL + "/" + projectId + GitlabFile.URL;
    api.retrieve().method("PUT")
      .with("file_path", filePath)
      .with("branch_name", branchName)
      .with("content", content)
      .with("commit_message", commitMsg)
      .to(tailUrl,Void.class);
  }
  
  public List<GitlabProject> searchProjects(String query) throws IOException {
    String tailUrl = GitlabProject.URL + "/search/" + query;
    GitlabProject[] projects = api.retrieve().to(tailUrl, GitlabProject[].class);
    return Arrays.asList(projects);
  }
  
  public void deleteProject(GitlabProject project) throws IOException {
    this.deleteProject(project.getId());
  }
  
  public void deleteProject(Integer projectId) throws IOException {
    String tailUrl = GitlabProject.URL + "/" + projectId;
    api.retrieve().method("DELETE").to(tailUrl, Void.class);
  }
  
  public List<GitlabTree> getTree(GitlabProject project, GitlabTree tree, String branch) throws IOException {
    return getTree(project.getId(), tree == null ? null :tree.getName(), branch);
  }

  public List<GitlabTree> getTree(Integer projectId, String folder, String branch) throws IOException {
    
    String tailUrl = GitlabProject.URL + "/" + projectId + GitlabTree.URL;

    Query query = new Query();
    
    if (folder != null) {
      query.append("path", folder);
    }
    
    if (branch != null) {
      query.append("ref_name", branch);
    }
    tailUrl = tailUrl + query.toString();
    
    GitlabTree[] files =  api.retrieve().to(tailUrl, GitlabTree[].class);
    return files == null ? null : Arrays.asList(files);
  }
  
  public List<GitlabCommit> getCommits(GitlabProject project, String branch) throws IOException {
    return getCommits(project.getId(), branch);
  }
  
  public List<GitlabCommit> getCommits(Integer projectId, String branch) throws IOException {
    String tailURL = GitlabProject.URL + "/" + projectId + "/repository" + GitlabCommit.URL;
    if (branch != null) {
      tailURL = tailURL + new Query().append("ref_name", branch);
    }
    GitlabCommit[]  commits = api.retrieve().to(tailURL, GitlabCommit[].class);
    return commits == null ? Collections.<GitlabCommit>emptyList() : Arrays.<GitlabCommit>asList(commits);
  }
  
  public GitlabFile getFile(GitlabProject project, String filePath, String ref) throws IOException {
    return getFile(project.getId(), filePath, ref);
  }
  
  public GitlabFile getFile(Integer projectId, String filePath, String ref) throws IOException {
    Query query = new Query().append("file_path", filePath).append("ref", ref);
    String tailUrl = GitlabProject.URL + "/" + projectId + GitlabFile.URL + query.toString();
    return api.retrieve().to(tailUrl, GitlabFile.class);
  }
  
  public String getRawFileAsString(Integer projectId, String branch, String filePath) throws IOException {
    InputStream is = getRawFile(projectId, branch, filePath);
    BufferedInputStream bis = new BufferedInputStream(is);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    byte[] buff = new byte[1024];
    for(int l = bis.read(buff); l != -1; l = bis.read(buff)) {
      baos.write(buff, 0, l);
    }
    return new String(baos.toByteArray(), "UTF-8");
  }
  
  public InputStream getRawFile(Integer projectId, String branch, String filePath) throws IOException {
    String tailUrl = GitlabProject.URL + "/" + projectId + "/repository/blobs/" + branch + new Query().append("filepath", filePath).toString();
    String apiUrl = api.getAPIUrl(tailUrl).toString();
    HttpResponse response = HttpClientUtil.execute(HttpClientFactory.getInstance(), apiUrl);
    return response.getEntity().getContent();
  }
  
  public String getRawFileAsString(Integer projectId, String rawSHA)  throws IOException {
    InputStream is = getRawFile(projectId, rawSHA);
    BufferedInputStream bis = new BufferedInputStream(is);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    byte[] buff = new byte[1024];
    for(int l = bis.read(buff); l != -1; l = bis.read(buff)) {
      baos.write(buff, 0, l);
    }
    return new String(baos.toByteArray(), "UTF-8");
  }
  
  public InputStream getRawFile(Integer projectId, String rawSHA)  throws IOException {
    String tailUrl = GitlabProject.URL + "/" + projectId + "/repository/raw_blobs/" + rawSHA;
    String apiUrl = api.getAPIUrl(tailUrl).toString();
    HttpResponse response = HttpClientUtil.execute(HttpClientFactory.getInstance(), apiUrl);
    return response.getEntity().getContent();
  }

  public String getRawFileAsString(GitlabProject project, String branch, String filePath) throws IOException {
    return getRawFileAsString(project.getId(), branch, filePath);
  }
  
  public InputStream getRawFile(GitlabProject project, String branch, String filePath) throws IOException {
    return getRawFile(project.getId(), branch, filePath);
  }
  
  public String getRawFileAsString(GitlabProject project, String rawSHA) throws IOException {
    return getRawFileAsString(project.getId(), rawSHA);
  }
  
  public InputStream getRawFile(GitlabProject project, String rawSHA) throws IOException {
    return getRawFile(project.getId(), rawSHA);
  }
}