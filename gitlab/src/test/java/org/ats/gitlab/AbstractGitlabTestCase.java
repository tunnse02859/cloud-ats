/**
 * 
 */
package org.ats.gitlab;

import java.io.IOException;

import org.junit.Before;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Oct 10, 2014
 */
public class AbstractGitlabTestCase {

  protected GitlabAPI api;
  
  @Before
  public void setUp() throws IOException {
    GitlabAPI api = new GitlabAPI(org.gitlab.api.GitlabAPI.connect("http://172.27.4.77", "uvL17u8Fo7zTKmcA4NsK"));
    this.api = api;
  }
}
