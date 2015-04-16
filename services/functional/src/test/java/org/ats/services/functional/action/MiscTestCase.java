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
}
