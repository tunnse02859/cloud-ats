/**
 * 
 */
package org.ats.gitlab;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.gitlab.api.models.GitlabCommit;
import org.gitlab.api.models.GitlabProject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Oct 6, 2014
 */
public class GitlabAPITestCase {

  private GitlabAPI api;
  
  private GitlabProject project;
  
  @Before
  public void setUp() throws IOException {
    GitlabAPI api = new GitlabAPI(org.gitlab.api.GitlabAPI.connect("http://172.27.4.77", "uvL17u8Fo7zTKmcA4NsK"));
    this.api = api;
    GitlabProject project = api.getAPI().getProject(1);
    this.project = project;
  }
  
  @Test
  public void testTree() throws Exception {
    List<GitlabTree> list = api.getTree(project, null, null);
    Assert.assertEquals(2, list.size());
    
    GitlabTree core = list.get(0);
    Assert.assertEquals("core", core.getName());
    Assert.assertEquals("tree", core.getType());
    
    GitlabTree pom = list.get(1);
    Assert.assertEquals("pom.xml", pom.getName());
    Assert.assertEquals("blob", pom.getType());
    
    //Change to test branch
    list = api.getTree(project.getId(), null, "test");
    Assert.assertEquals(3, list.size());
    
    GitlabTree test = list.get(1);
    Assert.assertEquals("test", test.getName());
    
    list = api.getTree(project, test, "test");
    Assert.assertEquals(1, list.size());
    Assert.assertEquals("hello.txt", list.get(0).getName());
  }
  
  @Test
  public void testGetCommit() throws Exception {
    List<GitlabCommit> commits = api.getCommits(project, null);
    Assert.assertEquals(16, commits.size());
    
    commits = api.getCommits(project, "test");
    Assert.assertEquals(18, commits.size());
  }
  
  @Test
  public void testGetFile() throws Exception {
    GitlabFile file = api.getFile(project, "pom.xml", "master");
    Assert.assertNotNull(file);
    Assert.assertEquals("base64", file.getEncoding());
    System.out.println(file.getFilePath());
    
    try {
      file = api.getFile(project, "core", "master");
      Assert.fail();
    } catch (FileNotFoundException e) {
    }
  }
  
  @Test
  public void testGetRawFile() throws Exception {
    GitlabFile file = api.getFile(project, "pom.xml", "master");
    InputStream is = api.getRawFile(project, file.getBlobId());
    
    BufferedInputStream bis = new BufferedInputStream(is);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    byte[] buff = new byte[1024];
    for(int l = bis.read(buff); l != -1; l = bis.read(buff)) {
      baos.write(buff, 0, l);
    }
    
    String raw = new String(baos.toByteArray(), "UTF-8");
    byte[] bytes = Base64.decodeBase64(file.getContent());
    String content = new String(bytes, "UTF-8");
    Assert.assertEquals(raw, content);
    
    file = api.getFile(project, "test/hello.txt", "test");
    Assert.assertNotNull(file);
    
    is = api.getRawFile(project, "test", "test/hello.txt");
    
    bis = new BufferedInputStream(is);
    baos = new ByteArrayOutputStream();
    buff = new byte[1024];
    for(int l = bis.read(buff); l != -1; l = bis.read(buff)) {
      baos.write(buff, 0, l);
    }
    
    raw = new String(baos.toByteArray(), "UTF-8");
    bytes = Base64.decodeBase64(file.getContent());
    content = new String(bytes, "UTF-8");
    
    Assert.assertEquals("Hello World\n", raw);
    Assert.assertEquals(raw, content);
  }
}