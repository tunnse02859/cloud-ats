/**
 * 
 */
package org.ats.services.functional.action;

import java.io.IOException;

import org.ats.services.FunctionalModule;
import org.ats.services.functional.ActionFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * @author TrinhTV3
 *
 * Email: TrinhTV3@fsoft.com.vn
 */
public class MiscTestCase {
  
  private ActionFactory actionFactory;
  
  private ObjectNode json;
  
  @BeforeMethod
  public void init() {
    Injector injector = Guice.createInjector(new FunctionalModule());
    this.actionFactory = injector.getInstance(ActionFactory.class);
    ObjectMapper mapper = new ObjectMapper();
    json = mapper.createObjectNode();
  }

  @Test
  public void testAddCookie() throws IOException {
    
    json.put("type", "addCookie");
    json.put("name", "test_cookie");
    json.put("value", "this-is-a-cookie");
    json.put("options", "path=/,max_age=100000000");
    
    IAction action = actionFactory.createAction(json);
    Assert.assertEquals(action.transform(), "wd.manage().addCookie(new Cookie.Builder(\"test_cookie\", \"this-is-a-cookie\").path(\"/\").expiresOn(new Date(new Date().getTime() + 100000000000l)).build());\n");
    
    json.put("name", "${test_cookie}");
    json.put("value", "${this-is-a-cookie}");
    action = actionFactory.createAction(json);
    Assert.assertEquals(action.transform(), "wd.manage().addCookie(new Cookie.Builder(test_cookie, this-is-a-cookie).path(\"/\").expiresOn(new Date(new Date().getTime() + 100000000000l)).build());\n");
   
  }
  
  @Test
  public void testDeleteCookie() throws IOException {
    json.put("type", "deleteCookie");
    json.put("name", "test_cookie");
    
    IAction action = actionFactory.createAction(json);
    Assert.assertEquals(action.transform(), 
        "if (wd.manage().getCookieNamed(\"test_cookie\") != null) {\n"
      + "      wd.manage().deleteCookie(wd.manage().getCookieNamed(\"test_cookie\"));\n"
      + "    }\n");
    
    json.put("name", "${test_cookie}");
    
    action = actionFactory.createAction(json);
    Assert.assertEquals(action.transform(), 
        "if (wd.manage().getCookieNamed(test_cookie) != null) {\n"
        + "      wd.manage().deleteCookie(wd.manage().getCookieNamed(test_cookie));\n"
        + "    }\n");
  }
  
  @Test
  public void testSwitchToWindow() throws IOException {
    json.put("type", "switchToWindow");
    json.put("name", "test_switch_to_window");
    IAction action = actionFactory.createAction(json);
    Assert.assertEquals(action.transform(), "wd = (FirefoxDriver) wd.switchTo().window(\"test_switch_to_window\");\n");
  }
  
  @Test
  public void testSwitchToFrameByIndex() throws IOException {
    json.put("type", "switchToFrameByIndex");
    json.put("index", 1);
    
    IAction action = actionFactory.createAction(json); 
    Assert.assertEquals(action.transform(), "wd = (FirefoxDriver) wd.switchTo().frame(1);\n");
  }
  
  @Test
  public void testSwitchToFrame() throws IOException {
    json.put("type", "switchToFrame");
    json.put("identifier", "identifier");
    
    IAction action = actionFactory.createAction(json);
    Assert.assertEquals(action.transform(), "wd = (FirefoxDriver) wd.switchTo().frame(\"identifier\");\n");
  }
  
  @Test
  public void testSwitchToDefaultContent() throws IOException {
    json.put("type", "switchToDefaultContent");
    IAction action = actionFactory.createAction(json);
    Assert.assertEquals(action.transform(), "wd = (FirefoxDriver) wd.switchTo().switchToDefaultContent();\n");
  }
  
  @Test
  public void testPause() throws IOException {
    json.put("type", "pause");
    json.put("waitTime", 10000);
    IAction pause = actionFactory.createAction(json);
    Assert.assertEquals(pause.transform(), 
        "try { Thread.sleep(10000l); } catch (Exception e) { throw new RuntimeException(e); }\n");
  }
  
  @Test
  public void testSaveScreenShot() throws IOException {
    json.put("type", "saveScreenshot");
    json.put("file", "/tmp/screen.png");

    IAction saveScreenShot = actionFactory.createAction(json); 
    Assert.assertEquals(saveScreenShot.transform(), "wd.getScreenshotAs(FILE).renameTo(new File(\"/tmp/screen.png\"));\n");
  }
  
  @Test
  public void testAnswerAlert() throws IOException {
    json.put("type", "answerAlert");
    json.put("text", "test");
    
    IAction action = actionFactory.createAction(json);
    Assert.assertEquals(action.transform(), "wd.switchTo().alert().sendKeys(\"test\");\n");
  }
  
  @Test
  public void testAcceptAlert() throws IOException {
    json.put("type", "acceptAlert");
    IAction action = actionFactory.createAction(json);
    Assert.assertEquals(action.transform(), "wd.switchTo().alert().accept();\n");
  }
  
  @Test
  public void testDismissAlert() throws IOException {
    json.put("type", "dismissAlert");
    IAction action = actionFactory.createAction(json);
    Assert.assertEquals(action.transform(), "wd.switchTo().alert().dismiss();\n");
  }
  
  @Test
  public void testPrint() throws IOException {
    json.put("type", "print");
    json.put("text", "this is printing");
    
    IAction print = actionFactory.createAction(json);
    Assert.assertEquals(print.transform(), "System.out.println(\"this is printing\");\n");
  }
}
