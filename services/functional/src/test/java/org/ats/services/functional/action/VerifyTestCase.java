/**
 * 
 */
package org.ats.services.functional.action;

import java.io.IOException;

import org.ats.services.functional.Value;
import org.ats.services.functional.locator.IDLocator;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author TrinhTV3
 *
 * Email: TrinhTV3@fsoft.com.vn
 */
public class VerifyTestCase {

  @Test
  public void testVerifyTextPresent() throws IOException {
    
    Value value = new Value("foo", true);
    VerifyTextPresent action = new VerifyTextPresent(value, true);
    
    Assert.assertEquals(action.transform(),"if (wd.findElement(By.tagName(\"html\")).getText().contains(foo)) "+
    "{\nSystem.out.println(\"!verifyTextPresent failed\");\n}\n");
    
    action = new VerifyTextPresent(value, false);
    
    Assert.assertEquals(action.transform(), "if (!wd.findElement(By.tagName(\"html\")).getText().contains(foo)) "+
    "{\nSystem.out.println(\"verifyTextPresent failed\");\n}\n");
  }
  
  @Test
  public void testVerityText() throws IOException {
    
    IDLocator locator = new IDLocator(new Value("test", false));
    Value value = new Value("foo", true);
    VerifyText action = new VerifyText(locator, value, true);
    
    Assert.assertEquals(action.transform(), "if (wd.findElement(By.id(\"test\")).getText().equals(foo)) "
        + "{\nSystem.out.println(\"!verifyText failed\");\n}\n");
    
    action = new VerifyText(locator, value, false);
    
    Assert.assertEquals(action.transform(), "if (!wd.findElement(By.id(\"test\")).getText().equals(foo)) "
        + "{\nSystem.out.println(\"verifyText failed\");\n}\n");
    
  }
  
  @Test
  public void testVerifyPageSource() throws IOException {
    
    Value value = new Value("https://google.com.vn", true);
    
    VerifyPageSource action = new VerifyPageSource(value, true);
    Assert.assertEquals(action.transform(), "if (wd.getPageSource().equals(https://google.com.vn)) "
        + "{\nSystem.out.println(\"!verifyPageSource failed\");\n}\n");
   
    action = new VerifyPageSource(value, false);
    
    Assert.assertEquals(action.transform(), "if (!wd.getPageSource().equals(https://google.com.vn)) "
        + "{\nSystem.out.println(\"verifyPageSource failed\");\n}\n");
  }
  
  @Test
  public void testVerifyElementPresent() throws IOException {
    
    IDLocator locator = new IDLocator(new Value("i am id", false));
    
    VerifyElementPresent action = new VerifyElementPresent(locator, true);
    Assert.assertEquals(action.transform(), "if ((wd.findElements(By.id(\"i am id\")).size() != 0)) "
        + "{\nSystem.out.println(\"!verifyElementPresent failed\");\n}\n");
  
    action = new VerifyElementPresent(locator, false);
    Assert.assertEquals(action.transform(), "if (!(wd.findElements(By.id(\"i am id\")).size() != 0)) "
        + "{\nSystem.out.println(\"verifyElementPresent failed\");\n}\n");
    
  }
  
  @Test
  public void testVerifyCurrentUrl() throws IOException {
    
    Value value = new Value("https://google.com.vn", true);
    
    VerifyCurrentUrl action = new VerifyCurrentUrl(value, true);
    Assert.assertEquals(action.transform(), "if (wd.getCurrentUrl().equals(https://google.com.vn)) "
        + "{\nSystem.out.println(\"!verifyCurrentUrl failed\");\n}\n");
    
    action = new VerifyCurrentUrl(value, false);
    
    Assert.assertEquals(action.transform(), "if (!wd.getCurrentUrl().equals(https://google.com.vn)) "
        + "{\nSystem.out.println(\"verifyCurrentUrl failed\");\n}\n");
    
  }
  
  @Test
  public void testVerifyBodyText() throws IOException {
    
    Value value = new Value("not body text", true);
    
    VerifyBodyText action = new VerifyBodyText(value, true);
    
    Assert.assertEquals(action.transform(), "if (wd.findElement(By.tagName(\"html\")).getText().equals(not body text)) "
        + "{\nSystem.out.println(\"!verifyBodyText failed\");\n}\n");
    
    action = new VerifyBodyText(value, false);
    Assert.assertEquals(action.transform(), "if (!wd.findElement(By.tagName(\"html\")).getText().equals(not body text)) "
        + "{\nSystem.out.println(\"verifyBodyText failed\");\n}\n");
  
  }
  
  @Test
  public void testVerifyTitle() throws IOException {
    
    Value value = new Value("title", false);
    
    VerifyTitle action = new VerifyTitle(value, true);
    Assert.assertEquals(action.transform(), "if (wd.getTitle().equals(\"title\")) {\nSystem.out.println(\"!verifyTitle failed\");\n}\n");
    
    action = new VerifyTitle(value, false);
    Assert.assertEquals(action.transform(), "if (!wd.getTitle().equals(\"title\")) {\nSystem.out.println(\"verifyTitle failed\");\n}\n");
 
  }
}
