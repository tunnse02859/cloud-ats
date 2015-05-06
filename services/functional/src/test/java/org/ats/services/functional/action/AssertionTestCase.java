/**
 * 
 */
package org.ats.services.functional.action;

import java.io.IOException;

import org.ats.services.DataDrivenModule;
import org.ats.services.FunctionalServiceModule;
import org.ats.services.OrganizationServiceModule;
import org.ats.services.data.DatabaseModule;
import org.ats.services.event.EventModule;
import org.ats.services.functional.ActionFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
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
public class AssertionTestCase {
  
  private ActionFactory actionFactory;
  
  private ObjectNode json;
  
  private ObjectNode locator;
  
  @BeforeClass
  public void init() throws Exception {
    Injector injector = Guice.createInjector(
        new DatabaseModule(), 
        new EventModule(),
        new OrganizationServiceModule(),
        new DataDrivenModule(),
        new FunctionalServiceModule());
    
    this.actionFactory = injector.getInstance(ActionFactory.class);
    ObjectMapper mapper = new ObjectMapper();
    json = mapper.createObjectNode();
    locator = mapper.createObjectNode();
  }

  @Test
  public void testAssertBodyText() throws IOException {
    json.put("type", "assertBodyText");
    json.put("text", "not body text");
    json.put("negated", true);
    
    AbstractAction action = actionFactory.createAction(json);
    Assert.assertEquals(action.transform(), "assertNotEquals(wd.findElement(By.tagName(\"html\")).getText(), \"not body text\");\n");
  
    json.put("text", "${text_variable}");
    json.put("negated", false);
    
    action = actionFactory.createAction(json);
    Assert.assertEquals(action.transform(), "assertEquals(wd.findElement(By.tagName(\"html\")).getText(), text_variable);\n");
  }
  
  @Test 
  public void testAssertCurrentUrl() throws IOException {
    json.put("type", "assertCurrentUrl");
    json.put("url", "http://google.com");
    json.put("negated", true);
    
    AbstractAction action = actionFactory.createAction(json);
    Assert.assertEquals(action.transform(), "assertNotEquals(wd.getCurrentUrl(), \"http://google.com\");\n");
    
    json.put("url", "${url_variable}");
    json.put("negated", false);
    
    action = actionFactory.createAction(json);
    Assert.assertEquals(action.transform(), "assertEquals(wd.getCurrentUrl(), url_variable);\n");
  }
  
  @Test
  public void testAssertElementPresent() throws IOException {
    json.put("type", "assertElementPresent");
    json.put("negated", true);
    
    locator.put("type", "id");
    locator.put("value", "i am id");
    json.put("locator", locator);
    
    AbstractAction action = actionFactory.createAction(json);
    Assert.assertEquals(action.transform(), "assertFalse((wd.findElements(By.id(\"i am id\")).size() != 0));\n");

    json.put("negated", false);
   
    action = actionFactory.createAction(json);
    Assert.assertEquals(action.transform(), "assertTrue((wd.findElements(By.id(\"i am id\")).size() != 0));\n");
  }
  
  @Test
  public void testAssertPageSource() throws IOException {
    json.put("type", "assertPageSource");
    json.put("source", "${page_source}");
    json.put("negated", true);
    
    AbstractAction action = actionFactory.createAction(json);
    Assert.assertEquals(action.transform(), "assertNotEquals(wd.getPageSource(), page_source);\n");

    json.put("negated", false);
    action = actionFactory.createAction(json);
    Assert.assertEquals(action.transform(), "assertEquals(wd.getPageSource(), page_source);\n");
  }
  
  @Test
  public void testAssertText() throws IOException {
    json.put("type", "assertText");
    json.put("text", "${text}");
    json.put("negated", true);
    
    locator.put("type", "id");
    locator.put("value", "i_am_an_id");
    json.put("locator", locator);
    
    AbstractAction action = actionFactory.createAction(json);
    
    Assert.assertEquals(action.transform(), "assertNotEquals(wd.findElement(By.id(\"i_am_an_id\")).getText(), text);\n");
    
    json.put("negated", false);
    action = actionFactory.createAction(json);
    Assert.assertEquals(action.transform(), "assertEquals(wd.findElement(By.id(\"i_am_an_id\")).getText(), text);\n");
  }
  
  @Test
  public void testAssertTextPresent() throws IOException {
    json.put("type", "assertTextPresent");
    json.put("text", "${text_present}");
    json.put("negated", true);
    
    AbstractAction action = actionFactory.createAction(json);
    Assert.assertEquals(action.transform(), "assertFalse(wd.findElement(By.tagName(\"html\")).getText().contains(text_present));\n");
    
    json.put("negated", false);
    action = actionFactory.createAction(json); 
    Assert.assertEquals(action.transform(), "assertTrue(wd.findElement(By.tagName(\"html\")).getText().contains(text_present));\n");
  }
  
  @Test
  public void testAssertCookieByName() throws IOException {
    json.put("type", "assertCookieByName");
    json.put("name", "test_cookie");
    json.put("value", "${cookie}");
    
    AbstractAction assertCookieByName = actionFactory.createAction(json);
    Assert.assertEquals(assertCookieByName.transform(), 
        "assertEquals(wd.manage().getCookieNamed(\"test_cookie\").getValue(), cookie);\n");
    
    json.put("negated", true);
    assertCookieByName = actionFactory.createAction(json);
    Assert.assertEquals(assertCookieByName.transform(), 
        "assertNotEquals(wd.manage().getCookieNamed(\"test_cookie\").getValue(), cookie);\n");
  }
  
  @Test
  public void testAssertCookiePresent() throws IOException {
    json.put("type", "assertCookiePresent");
    json.put("name", "test_cookie");
    json.put("negated", true);
    
    AbstractAction assertCookiePresent = actionFactory.createAction(json);
    Assert.assertEquals(assertCookiePresent.transform(), 
        "assertFalse((wd.manage().getCookieNamed(\"test_cookie\") != null));\n");
    
    json.put("negated", false);
    assertCookiePresent = actionFactory.createAction(json);
    Assert.assertEquals(assertCookiePresent.transform(), 
        "assertTrue((wd.manage().getCookieNamed(\"test_cookie\") != null));\n");
  }
  
  @Test
  public void testAssertElementAttribute() throws IOException {
    json.put("type", "assertElementAttribute");
    json.put("attributeName", "href");
    json.put("value", "${link_href}");
    json.put("negated", true);
    
    locator.put("type", "link text");
    locator.put("value", "i am a link");
    json.put("locator", locator);
    
    AbstractAction assertElementAttribute = actionFactory.createAction(json);
    Assert.assertEquals(assertElementAttribute.transform(), 
        "assertNotEquals(wd.findElement(By.linkText(\"i am a link\")).getAttribute(\"href\"), link_href);\n");
    
    json.put("negated", false);
    assertElementAttribute = actionFactory.createAction(json);
    Assert.assertEquals(assertElementAttribute.transform(), 
        "assertEquals(wd.findElement(By.linkText(\"i am a link\")).getAttribute(\"href\"), link_href);\n");
  }
  
  @Test
  public void testAssertElementSelected() throws IOException {
    json.put("type", "assertElementSelected");
    locator.put("type", "id");
    locator.put("value", "unchecked_checkbox");
    json.put("locator", locator);
    json.put("negated", true);
    
    AbstractAction assertElementSelected = actionFactory.createAction(json);
    Assert.assertEquals(assertElementSelected.transform(),
        "assertFalse((wd.findElement(By.id(\"unchecked_checkbox\")).isSelected()));\n");
    
    json.put("negated", false);
    assertElementSelected = actionFactory.createAction(json);
    Assert.assertEquals(assertElementSelected.transform(),
        "assertTrue((wd.findElement(By.id(\"unchecked_checkbox\")).isSelected()));\n");
  }
  
  @Test
  public void testAssertElementValue() throws IOException {
    json.put("type", "assertElementValue");
    json.put("value", "not w00t");
    json.put("negated", true);
    locator.put("type", "id");
    locator.put("value", "comments");
    json.put("locator", locator);
    
    AbstractAction assertElementValue = actionFactory.createAction(json);
    Assert.assertEquals(assertElementValue.transform(), 
        "assertNotEquals(wd.findElement(By.id(\"comments\")).getAttribute(\"value\"), \"not w00t\");\n");
    
    json.put("negated", false);
    assertElementValue = actionFactory.createAction(json);
    Assert.assertEquals(assertElementValue.transform(), 
        "assertEquals(wd.findElement(By.id(\"comments\")).getAttribute(\"value\"), \"not w00t\");\n");
  }
  
  @Test
  public void testAssertTitle() throws IOException {
    json.put("type", "assertTitle");
    json.put("title", "this is title");
    json.put("negated", true);
    
    AbstractAction assertTitle = actionFactory.createAction(json);
    Assert.assertEquals(assertTitle.transform(), 
        "assertNotEquals(wd.getTitle(), \"this is title\");\n");
    
    json.put("negated", false);
    assertTitle = actionFactory.createAction(json);
    Assert.assertEquals(assertTitle.transform(), 
        "assertEquals(wd.getTitle(), \"this is title\");\n");
  }
  
  @Test
  public void testAssertElementStyle() throws IOException {
    json.put("type", "assertElementStyle");
    json.put("value", "value is bar");
    json.put("propertyName", "bar");
    locator.put("type", "id");
    locator.put("value", "i am a id");
    json.put("locator", locator);
    json.put("negated", true);
    
    AbstractAction assertElementStyle = actionFactory.createAction(json);
    Assert.assertEquals(assertElementStyle.transform(), 
        "assertNotEquals(wd.findElement(By.id(\"i am a id\")).getCssValue(\"bar\"), \"value is bar\");\n");
    
    json.put("negated", false);
    assertElementStyle = actionFactory.createAction(json);
    Assert.assertEquals(assertElementStyle.transform(), 
        "assertEquals(wd.findElement(By.id(\"i am a id\")).getCssValue(\"bar\"), \"value is bar\");\n");
  }
  
  @Test
  public void testAssertAlertPresent() throws IOException {
    json.put("type", "assertAlertPresent");
    json.put("negated", true);
    
    AbstractAction alertPresent = actionFactory.createAction(json);
    Assert.assertEquals(alertPresent.transform(), "assertFalse(isAlertPresent(wd));\n");

    json.put("negated", false);
    alertPresent = actionFactory.createAction(json);
    Assert.assertEquals(alertPresent.transform(), "assertTrue(isAlertPresent(wd));\n");
  }
  
  @Test
  public void testAssertAlertText() throws IOException {
    json.put("type", "assertAlertText");
    json.put("text", "this is alert text");
    json.put("negated", true);
    
    AbstractAction alertText = actionFactory.createAction(json);
    Assert.assertEquals(alertText.transform(), 
        "assertNotEquals(wd.switchTo().alert().getText(), \"this is alert text\");\n");
    
    json.put("negated", false);
    alertText = actionFactory.createAction(json);
    Assert.assertEquals(alertText.transform(), 
        "assertEquals(wd.switchTo().alert().getText(), \"this is alert text\");\n");
  }
  
  @Test
  public void testAssertEval() throws IOException {
    json.put("type", "assertEval");
    json.put("script", "test");
    json.put("value", "value");

    AbstractAction action = actionFactory.createAction(json);
    Assert.assertEquals(action.transform(), "assertEquals(wd.executeScript(\"test\"), \"value\");\n");
    
    json.put("negated", true);
    action = actionFactory.createAction(json);
    Assert.assertEquals(action.transform(), "assertNotEquals(wd.executeScript(\"test\"), \"value\");\n");
  }
}
