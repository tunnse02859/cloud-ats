/**
 * 
 */
package org.ats.services.functional.action;

import java.io.IOException;

import org.ats.services.functional.Value;
import org.ats.services.functional.locator.IDLocator;
import org.ats.services.functional.locator.LinkTextLocator;
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
  
  @Test
  public void testVerifyElementValue() throws IOException {
    
    Value value = new Value("comments", false);
    IDLocator locator = new IDLocator(value);
    
    VerifyElementValue action = new VerifyElementValue(locator, new Value("w00t", false), true);
    
    Assert.assertEquals(action.transform(), "if (wd.findElement(By.id(\"comments\")).getAttribute(\"value\").equals(\"w00t\")) "
        + "{\nSystem.out.println(\"!verifyElementValue failed\");\n}\n");
    
    action = new VerifyElementValue(locator, new Value("w00t", false), false);
    
    Assert.assertEquals(action.transform(), "if (!wd.findElement(By.id(\"comments\")).getAttribute(\"value\").equals(\"w00t\")) "
        + "{\nSystem.out.println(\"verifyElementValue failed\");\n}\n");
    
  }
  
  @Test
  public void testVerifyElementSelected() throws IOException {
    
    Value value = new Value("unchecked_checkbox", false);
    
    IDLocator locator = new IDLocator(value);
    
    VerifyElementSelected action = new VerifyElementSelected(locator, true);
    Assert.assertEquals(action.transform(), "if ((wd.findElement(By.id(\"unchecked_checkbox\")).isSelected())) "
        + "{\nSystem.out.println(\"!verifyElementSelected failed\");\n}\n");
 
    action = new VerifyElementSelected(locator, false);
    Assert.assertEquals(action.transform(), "if (!(wd.findElement(By.id(\"unchecked_checkbox\")).isSelected())) "
        + "{\nSystem.out.println(\"verifyElementSelected failed\");\n}\n");
    
    value = new Value("unchecked_checkbox", true);
    locator = new IDLocator(value);
    action = new VerifyElementSelected(locator, false);
    Assert.assertEquals(action.transform(), "if (!(wd.findElement(By.id(unchecked_checkbox)).isSelected())) "
        + "{\nSystem.out.println(\"verifyElementSelected failed\");\n}\n");
    
  }
  
  @Test
  public void testVerifyElementAttribute() throws IOException {
    Value valueLocator = new Value("i am a link", false);
    
    Value value = new Value("link_href", true);
    
    Value attributeName = new Value("href", false);
    
    LinkTextLocator locator = new LinkTextLocator(valueLocator);
    
    VerifyElementAttribute action = new VerifyElementAttribute(locator, attributeName, value, true);
    
    Assert.assertEquals(action.transform(), "if (wd.findElement(By.linkText(\"i am a link\")).getAttribute(\"href\").equals(link_href)) "
        + "{\nSystem.out.println(\"!verifyElementAttribute failed\");\n}\n");
    
    action = new VerifyElementAttribute(locator, attributeName, value, false);
    Assert.assertEquals(action.transform(), "if (!wd.findElement(By.linkText(\"i am a link\")).getAttribute(\"href\").equals(link_href)) "
        + "{\nSystem.out.println(\"verifyElementAttribute failed\");\n}\n");
    
  }
  
  @Test
  public void testVerifyCookiePresent() throws IOException {
    Value name = new Value("test_cookie", false);
    VerifyCookiePresent action = new VerifyCookiePresent(name, true);
    
    Assert.assertEquals(action.transform(), "if ((wd.manage().getCookieNamed(\"test_cookie\") != null)) "
        + "{\nSystem.out.println(\"!verifyCookiePresent failed\");\n}\n");
    
    action = new VerifyCookiePresent(name, false);
    
    Assert.assertEquals(action.transform(), "if (!(wd.manage().getCookieNamed(\"test_cookie\") != null)) {\nSystem.out.println(\"verifyCookiePresent failed\");\n}\n");
  }
  
  @Test
  public void testVerifyCookieByName() throws IOException {
    
    Value name = new Value("test_cookie", false);
    
    Value value = new Value("cookie", true);
    
    VerifyCookieByName action = new VerifyCookieByName(name, value, true);
    Assert.assertEquals(action.transform(), "if (wd.manage().getCookieNamed(\"test_cookie\").getValue().equals(cookie)) {\nSystem.out.println(\"!verifyCookieByName failed\");\n}\n");
  }
  
  @Test
  public void testVerifyElementStyle() throws IOException {
    IDLocator locator = new IDLocator(new Value("i am a id", false));
    Value value = new Value("value is bar", false);
    Value propertyName = new Value("bar", false);
    
    VerifyElementStyle verifyElementStyle = new VerifyElementStyle(propertyName, value, locator);
    Assert.assertEquals(verifyElementStyle.transform(), 
        "if (!wd.findElement(By.id(\"i am a id\")).getCssValue(\"bar\").equals(\"value is bar\")) {\n"+
            "System.out.println(\"verifyElementStyle failed\");\n}\n");
  }
  
  @Test
  public void testVerifyAlertPresent() throws IOException {
    
    VerifyAlertPresent action = new VerifyAlertPresent();
    Assert.assertEquals(action.transform(), "if (!isAlertPresent(wd)) {\nSystem.out.println(\"verifyAlertPresent failed\");\n}\n");
  }
  
  @Test
  public void testVerifyEval() throws IOException {
    Value script = new Value("test", false);
    
    Value value = new Value("value", false);
    
    VerifyEval action = new VerifyEval(script, value);
    Assert.assertEquals(action.transform(), "if (!wd.executeScript(\"test\").equals(\"value\")) {\nSystem.out.println(\"verifyEval failed\");\n}\n");
  }
  
  @Test
  public void testVerifyAlertText() throws IOException {
    Value text = new Value("this is alert text", false);
    
    VerifyAlertText verifyAlertText = new VerifyAlertText(text, true);
    Assert.assertEquals(verifyAlertText.transform(), 
        "if (wd.switchTo().alert().getText().equals(\"this is alert text\")) {\n"+
            "System.out.println(\"!verifyAlertText failed\");}");
  }
}
