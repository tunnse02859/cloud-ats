/**
 * 
 */
package org.ats.common.http;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Oct 9, 2014
 */
public class HttpURLTestCase {

  @Test
  public void testLocalhost() throws Exception {
    HttpURL url = new HttpURL("http://localhost:9000/signin?group=true");
    Assert.assertEquals("http", url.getProtocol());
    Assert.assertEquals("localhost", url.getHost());
    Assert.assertEquals(9000, url.getPort());
    Assert.assertEquals("/signin", url.getPath());
    Assert.assertEquals(1, url.getQueryParameters().size());
    Assert.assertEquals("true", url.getQueryParameters().get("group"));
    Assert.assertEquals("http://localhost:9000/signin?group=true", url.getNormalizeURL());
  }
  
  @Test
  public void testSite() throws Exception {
    HttpURL url = new HttpURL("http://www3.code.google.com/search?q=cheer");
    Assert.assertEquals("http", url.getProtocol());
    Assert.assertEquals("code.google.com", url.getHost());
    Assert.assertEquals("www3.code.google.com", url.getDomain());
    Assert.assertEquals(80, url.getPort());
    Assert.assertEquals("/search", url.getPath());
    Assert.assertEquals(1, url.getQueryParameters().size());
    Assert.assertEquals("cheer", url.getQueryParameters().get("q"));
    Assert.assertEquals("http://www3.code.google.com/search?q=cheer", url.getNormalizeURL());
    
    url = new HttpURL("http://www.code.google.com/search?q=cheer");
    Assert.assertEquals("code.google.com", url.getHost());
    Assert.assertEquals("www.code.google.com", url.getDomain());
    Assert.assertEquals("http://www.code.google.com/search?q=cheer", url.getNormalizeURL());
  }
}
