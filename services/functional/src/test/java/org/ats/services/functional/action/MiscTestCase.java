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
public class MiscTestCase {

  @Test
  public void testAddCookie() throws IOException {
    
    Value valueName = new Value("test_cookie", false);
    Value value = new Value("this-is-a-cookie", false);
    String option = "path=/,max_age=100000000";
    
    AddCookie action = new AddCookie(valueName, value, option);
    
    Assert.assertEquals(action.transform(), "wd.manage().addCookie(new Cookie.Builder(\"test_cookie\", \"this-is-a-cookie\").path(\"/\").expiresOn(new Date(new Date().getTime() + 100000000000l)).build());\n");
    
    valueName = new Value("test_cookie", true);
    value = new Value("this-is-a-cookie", true);
    action = new AddCookie(valueName, value, option);
    
    Assert.assertEquals(action.transform(), "wd.manage().addCookie(new Cookie.Builder(test_cookie, this-is-a-cookie).path(\"/\").expiresOn(new Date(new Date().getTime() + 100000000000l)).build());\n");
   
  }
  
  @Test
  public void testDeleteCookie() throws IOException {
    
    Value valueName = new Value("test_cookie", false);
    
    DeleteCookie action = new DeleteCookie(valueName);
    Assert.assertEquals(action.transform(), "if (wd.manage().getCookieNamed(\"test_cookie\") != null) { wd.manage().deleteCookie(wd.manage().getCookieNamed(\"test_cookie\")); }\n");
    
    valueName = new Value("test_cookie", true);
    
    action = new DeleteCookie(valueName);
    Assert.assertEquals(action.transform(), "if (wd.manage().getCookieNamed(test_cookie) != null) { wd.manage().deleteCookie(wd.manage().getCookieNamed(test_cookie)); }\n");
  }
  
  @Test
  public void testSwitchToWindow() throws IOException {
    
    Value name = new Value("test_witch_to_window", false);
    SwitchToWindow action = new SwitchToWindow(name);
    Assert.assertEquals(action.transform(), "wd = (FirefoxDriver) wd.switchTo().window(\"test_witch_to_window\");\n");
  }
  
  @Test
  public void testSwitchToFrameByIndex() throws IOException {
    Value name = new Value("index", false);
    SwitchToFrameByIndex action = new SwitchToFrameByIndex(name);
    
    Assert.assertEquals(action.transform(), "wd = (FirefoxDriver) wd.switchTo().frame(\"index\");\n");
  }
  
  @Test
  public void testSwitchToFrame() throws IOException {
    Value identifier = new Value("indentifier", false);
    SwitchToFrame action = new SwitchToFrame(identifier);
    
    Assert.assertEquals(action.transform(), "wd = (FirefoxDriver) wd.switchTo().frame(\"indentifier\");\n");
  }
  
  @Test
  public void testSwitchToDefaultContent() throws IOException {
    SwitchToDefaultContent action = new SwitchToDefaultContent();
    Assert.assertEquals(action.transform(), "wd = (FirefoxDriver) wd.switchTo().switchToDefaultContent();\n");
  }
  
  @Test
  public void testPause() throws IOException {
    String waitTime = "10000";
    Pause pause = new Pause(waitTime);
    Assert.assertEquals(pause.transform(), 
        "try { Thread.sleep(10000l); } catch (Exception e) { throw new RuntimeException(e); }\n");
  }
  
  @Test
  public void testSaveScreenShot() throws IOException {
    Value file = new Value("/tmp/screen.png", false);
    SaveScreenShot saveScreenShot = new SaveScreenShot(file);
    Assert.assertEquals(saveScreenShot.transform(), "wd.getScreenshotAs(FILE).renameTo(new File(\"/tmp/screen.png\"));\n");
  }
}
