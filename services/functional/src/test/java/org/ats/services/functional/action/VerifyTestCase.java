/**
 * 
 */
package org.ats.services.functional.action;

import java.io.IOException;

import org.ats.services.FunctionalModule;
import org.ats.services.functional.ActionFactory;
import org.ats.services.functional.Value;
import org.ats.services.functional.locator.IDLocator;
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
public class VerifyTestCase {
  
  private ActionFactory actionFactory;
  
  private ObjectNode json;
  
  private ObjectNode locator;
  
  @BeforeMethod
  public void init() throws Exception {
    Injector injector = Guice.createInjector(new FunctionalModule());
    this.actionFactory = injector.getInstance(ActionFactory.class);
    ObjectMapper mapper = new ObjectMapper();
    json = mapper.createObjectNode();
    locator = mapper.createObjectNode();
  }

  @Test
  public void testVerifyTextPresent() throws IOException {
    json.put("type", "verifyTextPresent");
    json.put("text", "${foo}");
    json.put("negated", true);
    
    AbstractAction action = actionFactory.createAction(json);
    
    Assert.assertEquals(action.transform(),"if (wd.findElement(By.tagName(\"html\")).getText().contains(foo)) {\n"
        + "      System.out.println(\"!verifyTextPresent failed\");\n"
        + "    }\n");

    json.put("negated", false);
    action = actionFactory.createAction(json);
    
    Assert.assertEquals(action.transform(), "if (!wd.findElement(By.tagName(\"html\")).getText().contains(foo)) {\n"
        + "      System.out.println(\"verifyTextPresent failed\");\n"
        + "    }\n");
  }
  
  @Test
  public void testVerityText() throws IOException {
    json.put("type", "verifyText");
    json.put("text", "${foo}");
    json.put("negated", true);
    locator.put("type", "id");
    locator.put("value", "test");
    json.put("locator", locator);
    
    AbstractAction action = actionFactory.createAction(json);
    
    Assert.assertEquals(action.transform(), "if (wd.findElement(By.id(\"test\")).getText().equals(foo)) {\n"
        + "      System.out.println(\"!verifyText failed\");\n"
        + "    }\n");
    
    json.put("negated", false);
    action = actionFactory.createAction(json);
    
    Assert.assertEquals(action.transform(), "if (!wd.findElement(By.id(\"test\")).getText().equals(foo)) {\n"
        + "      System.out.println(\"verifyText failed\");\n"
        + "    }\n");
  }
  
  @Test
  public void testVerifyPageSource() throws IOException {
    json.put("type", "verifyPageSource");
    json.put("source", "https://google.com.vn");
    json.put("negated", true);
    
    AbstractAction action = actionFactory.createAction(json);
    Assert.assertEquals(action.transform(), "if (wd.getPageSource().equals(\"https://google.com.vn\")) {\n"
        + "      System.out.println(\"!verifyPageSource failed\");\n"
        + "    }\n");
   
    json.put("negated", false);
    action = actionFactory.createAction(json);
    Assert.assertEquals(action.transform(), "if (!wd.getPageSource().equals(\"https://google.com.vn\")) {\n"
        + "      System.out.println(\"verifyPageSource failed\");\n"
        + "    }\n");
  }
  
  @Test
  public void testVerifyElementPresent() throws IOException {
    json.put("type", "verifyElementPresent");
    json.put("negated", true);
    locator.put("type", "id");
    locator.put("value", "i am id");
    json.put("locator", locator);
    
    AbstractAction action = actionFactory.createAction(json);
    Assert.assertEquals(action.transform(), "if ((wd.findElements(By.id(\"i am id\")).size() != 0)) {\n"
        + "      System.out.println(\"!verifyElementPresent failed\");\n"
        + "    }\n");
  
    json.put("negated", false);
    action = actionFactory.createAction(json);
    Assert.assertEquals(action.transform(), "if (!(wd.findElements(By.id(\"i am id\")).size() != 0)) {\n"
        + "      System.out.println(\"verifyElementPresent failed\");\n"
        + "    }\n");
  }
  
  @Test
  public void testVerifyCurrentUrl() throws IOException {
    json.put("type", "verifyCurrentUrl");
    json.put("url", "https://google.com.vn");
    json.put("negated", true);
    
    AbstractAction action = actionFactory.createAction(json);
    Assert.assertEquals(action.transform(), "if (wd.getCurrentUrl().equals(\"https://google.com.vn\")) {\n"
        + "      System.out.println(\"!verifyCurrentUrl failed\");\n"
        + "    }\n");
    
    json.put("negated", false);
    action = actionFactory.createAction(json);
    
    Assert.assertEquals(action.transform(), "if (!wd.getCurrentUrl().equals(\"https://google.com.vn\")) {\n"
        + "      System.out.println(\"verifyCurrentUrl failed\");\n"
        + "    }\n");
    
  }
  
  @Test
  public void testVerifyBodyText() throws IOException {
    json.put("type", "verifyBodyText");
    json.put("text", "not body text");
    json.put("negated", true);

    AbstractAction action = actionFactory.createAction(json);
    Assert.assertEquals(action.transform(), "if (wd.findElement(By.tagName(\"html\")).getText().equals(\"not body text\")) {\n"
        + "      System.out.println(\"!verifyBodyText failed\");\n"
        + "    }\n");

    json.put("negated", false);
    action = actionFactory.createAction(json);
    Assert.assertEquals(action.transform(), "if (!wd.findElement(By.tagName(\"html\")).getText().equals(\"not body text\")) {\n"
        + "      System.out.println(\"verifyBodyText failed\");\n"
        + "    }\n");
  
  }
  
  @Test
  public void testVerifyTitle() throws IOException {
    json.put("type", "verifyTitle");
    json.put("title", "title");
    json.put("negated", true);
    
    AbstractAction action = actionFactory.createAction(json);
    Assert.assertEquals(action.transform(), "if (wd.getTitle().equals(\"title\")) {\n"
        + "      System.out.println(\"!verifyTitle failed\");\n"
        + "    }\n");
    
    json.put("negated", false);
    action = actionFactory.createAction(json);
    Assert.assertEquals(action.transform(), "if (!wd.getTitle().equals(\"title\")) {\n"
        + "      System.out.println(\"verifyTitle failed\");\n"
        + "    }\n");
  }
  
  @Test
  public void testVerifyElementValue() throws IOException {
    json.put("type", "verifyElementValue");
    json.put("value", "w00t");
    json.put("negated", true);
    
    locator.put("type", "id");
    locator.put("value", "comments");
    json.put("locator", locator);
    
    AbstractAction action = actionFactory.createAction(json);
    
    Assert.assertEquals(action.transform(), "if (wd.findElement(By.id(\"comments\")).getAttribute(\"value\").equals(\"w00t\")) {\n"
        + "      System.out.println(\"!verifyElementValue failed\");\n"
        + "    }\n");
    
    json.put("negated", false);
    action = actionFactory.createAction(json);
    
    Assert.assertEquals(action.transform(), "if (!wd.findElement(By.id(\"comments\")).getAttribute(\"value\").equals(\"w00t\")) {\n"
        + "      System.out.println(\"verifyElementValue failed\");\n"
        + "    }\n");
    
  }
  
  @Test
  public void testVerifyElementSelected() throws IOException {
    json.put("type", "verifyElementSelected");
    json.put("negated", true);

    locator.put("type", "id");
    locator.put("value", "unchecked_checkbox");
    json.put("locator", locator);
    
    AbstractAction action = actionFactory.createAction(json);
    Assert.assertEquals(action.transform(), "if ((wd.findElement(By.id(\"unchecked_checkbox\")).isSelected())) {\n"
        + "      System.out.println(\"!verifyElementSelected failed\");\n"
        + "    }\n");
 
    json.put("negated", false);
    action = actionFactory.createAction(json);
    Assert.assertEquals(action.transform(), "if (!(wd.findElement(By.id(\"unchecked_checkbox\")).isSelected())) {\n"
        + "      System.out.println(\"verifyElementSelected failed\");\n"
        + "    }\n");
    
    locator.put("value", "${unchecked_checkbox}");
    json.put("locator", locator);
    
    action = actionFactory.createAction(json);
    Assert.assertEquals(action.transform(), "if (!(wd.findElement(By.id(unchecked_checkbox)).isSelected())) {\n"
        + "      System.out.println(\"verifyElementSelected failed\");\n"
        + "    }\n");
    
  }
  
  @Test
  public void testVerifyElementAttribute() throws IOException {
    json.put("type", "verifyElementAttribute");
    json.put("negated", true);

    json.put("attributeName", "href");
    json.put("value", "${link_href}");

    locator.put("type", "link text");
    locator.put("value", "i am a link");
    json.put("locator", locator);
    
    AbstractAction action = actionFactory.createAction(json);
    
    Assert.assertEquals(action.transform(), "if (wd.findElement(By.linkText(\"i am a link\")).getAttribute(\"href\").equals(link_href)) {\n"
        + "      System.out.println(\"!verifyElementAttribute failed\");\n"
        + "    }\n");
    
    json.put("negated", false);
    action = actionFactory.createAction(json);
    Assert.assertEquals(action.transform(), "if (!wd.findElement(By.linkText(\"i am a link\")).getAttribute(\"href\").equals(link_href)) {\n"
        + "      System.out.println(\"verifyElementAttribute failed\");\n"
        + "    }\n");
    
  }
  
  @Test
  public void testVerifyCookiePresent() throws IOException {
    json.put("type", "verifyCookiePresent");
    json.put("name", "test_cookie");
    json.put("negated", true);
    
    AbstractAction action = actionFactory.createAction(json);
    Assert.assertEquals(action.transform(), "if ((wd.manage().getCookieNamed(\"test_cookie\") != null)) {\n"
        + "      System.out.println(\"!verifyCookiePresent failed\");\n"
        + "    }\n");

    json.put("negated", false);
    action = actionFactory.createAction(json);
    Assert.assertEquals(action.transform(), "if (!(wd.manage().getCookieNamed(\"test_cookie\") != null)) {\n"
        + "      System.out.println(\"verifyCookiePresent failed\");\n"
        + "    }\n");
  }
  
  @Test
  public void testVerifyCookieByName() throws IOException {
    json.put("type", "verifyCookieByName");
    json.put("name", "test_cookie");
    json.put("value", "${cookie}");
    json.put("negated", true);
    
    AbstractAction action = actionFactory.createAction(json);
    Assert.assertEquals(action.transform(), 
        "if (wd.manage().getCookieNamed(\"test_cookie\").getValue().equals(cookie)) {\n"
        + "      System.out.println(\"!verifyCookieByName failed\");\n"
        + "    }\n");
  }
  
  @Test
  public void testVerifyElementStyle() throws IOException {
    IDLocator locator = new IDLocator(new Value("i am a id", false));
    Value value = new Value("value is bar", false);
    Value propertyName = new Value("bar", false);
    
    VerifyElementStyle verifyElementStyle = new VerifyElementStyle(propertyName, value, locator,true);
    Assert.assertEquals(verifyElementStyle.transform(), 
        "if (wd.findElement(By.id(\"i am a id\")).getCssValue(\"bar\").equals(\"value is bar\")) {\n"+
            "      System.out.println(\"!verifyElementStyle failed\");\n"
            + "    }\n");
    
    verifyElementStyle = new VerifyElementStyle(propertyName, value, locator,false);
    Assert.assertEquals(verifyElementStyle.transform(), 
        "if (!wd.findElement(By.id(\"i am a id\")).getCssValue(\"bar\").equals(\"value is bar\")) {\n"+
            "      System.out.println(\"verifyElementStyle failed\");\n"
            + "    }\n");
  }
  
  @Test
  public void testVerifyAlertPresent() throws IOException {
    json.put("type", "verifyAlertPresent");
    json.put("negated", true);
    AbstractAction action = actionFactory.createAction(json);
    Assert.assertEquals(action.transform(), "if (isAlertPresent(wd)) {\n"
        + "      System.out.println(\"!verifyAlertPresent failed\");\n"
        + "    }\n");
    
    json.put("negated", false);
    action = new VerifyAlertPresent(false);
    Assert.assertEquals(action.transform(), "if (!isAlertPresent(wd)) {\n"
        + "      System.out.println(\"verifyAlertPresent failed\");\n"
        + "    }\n");
    
  }
  
  @Test
  public void testVerifyEval() throws IOException {
    json.put("type", "verifyEval");
    json.put("script", "test");
    json.put("value", "value");
    json.put("negated", false);
    
    AbstractAction action = actionFactory.createAction(json);
    Assert.assertEquals(action.transform(), "if (!wd.executeScript(\"test\").equals(\"value\")) {\n"
        + "      System.out.println(\"verifyEval failed\");\n"
        + "    }\n");
    
    json.put("negated", true);
    action = actionFactory.createAction(json);
    Assert.assertEquals(action.transform(), "if (wd.executeScript(\"test\").equals(\"value\")) {\n"
        + "      System.out.println(\"!verifyEval failed\");\n"
        + "    }\n");
  }
  
  @Test
  public void testVerifyAlertText() throws IOException {
    json.put("type", "verifyAlertText");
    json.put("negated", true);
    json.put("text", "this is alert text");
    
    AbstractAction verifyAlertText = actionFactory.createAction(json);
    Assert.assertEquals(verifyAlertText.transform(), 
        "if (wd.switchTo().alert().getText().equals(\"this is alert text\")) {\n"
          + "      System.out.println(\"!verifyAlertText failed\");\n"
          + "    }\n");

    json.put("negated", false);
    verifyAlertText = actionFactory.createAction(json);
    Assert.assertEquals(verifyAlertText.transform(), 
        "if (!wd.switchTo().alert().getText().equals(\"this is alert text\")) {\n"
     + "      System.out.println(\"verifyAlertText failed\");\n"
     + "    }\n");
  }
}
