/**
 * 
 */
package org.ats.services.functional.action;

import java.io.IOException;

import org.ats.services.functional.Value;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author TrinhTV3
 *
 * Email: TrinhTV3@fsoft.com.vn
 */
public class NagivationTestCase {

  @Test
  public void testClose() throws IOException {
    Close close = new Close();
    Assert.assertEquals(close.transform(), "wd.close();\n");
  }
  
  @Test
  public void testGet() throws IOException {
    Get get = new Get(new Value("http://saucelabs.com/test/guinea-pig/", false));
    Assert.assertEquals(get.transform(), "wd.get(\"http://saucelabs.com/test/guinea-pig/\");\n");
  }
  
  @Test
  public void testRefresh() throws IOException {
    Refresh refresh = new Refresh();
    Assert.assertEquals(refresh.transform(), "wd.navigate().refresh();\n");
  }
 
  @Test
  public void testGoback() throws IOException {
    GoBack goBack = new GoBack();
    Assert.assertEquals(goBack.transform(), "wd.navigate().back();\n");
  }
  
  @Test
  public void testGoForward() throws IOException {
    GoForward goForward = new GoForward();
    Assert.assertEquals(goForward.transform(), "wd.navigate().forward();\n");
  }
}
