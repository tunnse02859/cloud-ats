/**
 * 
 */
package org.ats.services.functional.action;

import java.io.IOException;

import org.ats.services.DataDrivenModule;
import org.ats.services.KeywordServiceModule;
import org.ats.services.OrganizationServiceModule;
import org.ats.services.data.DatabaseModule;
import org.ats.services.event.EventModule;
import org.ats.services.keyword.ActionFactory;
import org.ats.services.keyword.action.AbstractAction;
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
public class StoreTestCase {
  
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
        new KeywordServiceModule());
    
    this.actionFactory = injector.getInstance(ActionFactory.class);
    ObjectMapper mapper = new ObjectMapper();
    json = mapper.createObjectNode();
    locator = mapper.createObjectNode();
  }
  
  @Test
  public void testStoreBodyText() throws IOException {
    json.put("type", "storeBodyText");
    json.put("variable", "body_text");
    AbstractAction storeBodyText = actionFactory.createAction(json);
    Assert.assertEquals(storeBodyText.transform(), "String body_text = wd.findElement(By.tagName(\"html\")).getText();\n");
  }
  
  @Test
  public void testStorePageSource() throws IOException {
    json.put("type", "storePageSource");
    json.put("variable", "page_source");
    AbstractAction storePageSource = actionFactory.createAction(json);
    Assert.assertEquals(storePageSource.transform(), "String page_source = wd.getPageSource();\n");
  }
  
  @Test
  public void testStoreText() throws IOException {
    json.put("type", "storeText");
    json.put("variable", "text");
    
    locator.put("type", "id");
    locator.put("value", "i_am_an_id");
    
    json.put("locator", locator);
    
    AbstractAction storeText = actionFactory.createAction(json);
    Assert.assertEquals(storeText.transform(), "String text = wd.findElement(By.id(\"i_am_an_id\")).getText();\n");
  }
  
 @Test
  public void testStoreTextPresent() throws IOException {
   json.put("type", "storeTextPresent");
   json.put("variable", "text_is_present");
   json.put("text", "I am another div");
   
   AbstractAction storeTextPresent = actionFactory.createAction(json);
   Assert.assertEquals(storeTextPresent.transform(), 
        "boolean text_is_present = wd.findElement(By.tagName(\"html\")).getText().contains(\"I am another div\");\n");
    
    json.put("text", "${text_variable}");
    storeTextPresent = actionFactory.createAction(json);
    Assert.assertEquals(storeTextPresent.transform(), 
        "text_is_present = wd.findElement(By.tagName(\"html\")).getText().contains(text_variable);\n");
  }
 
  @Test
  public void testStoreElementPresent() throws IOException {
    json.put("type", "storeElementPresent");
    json.put("variable", "element_present");
    
    locator.put("type", "id");
    locator.put("value", "unchecked_checkbox");
    json.put("locator", locator);
    
    AbstractAction storeElementPresent = actionFactory.createAction(json);
    Assert.assertEquals(storeElementPresent.transform(), 
        "boolean element_present = (wd.findElements(By.id(\"unchecked_checkbox\")).size() != 0);\n");
    
    locator.put("value", "${locator_value}");
    json.put("locator", locator);
    storeElementPresent = actionFactory.createAction(json);
    
    Assert.assertEquals(storeElementPresent.transform(), 
        "element_present = (wd.findElements(By.id(locator_value)).size() != 0);\n");
  }

  @Test
  public void testStoreElementAttribute() throws IOException {
    json.put("type",  "storeElementAttribute");
    json.put("variable", "link_href");
    json.put("attributeName", "href");
    
    locator.put("type", "link text");
    locator.put("value", "i am a link");
    json.put("locator", locator);
    
    AbstractAction action = actionFactory.createAction(json);
    Assert.assertEquals(action.transform(), 
        "String link_href = wd.findElement(By.linkText(\"i am a link\")).getAttribute(\"href\");\n");
    
    json.put("attributeName", "${attribute_name}");
    locator.put("value", "${locator_value}");
    json.put("locator", locator);
    
    action = actionFactory.createAction(json);
    Assert.assertEquals(action.transform(), 
        "link_href = wd.findElement(By.linkText(locator_value)).getAttribute(attribute_name);\n");
  }

  @Test
  public void testStore() throws IOException {
    json.put("type", "store");
    json.put("variable", "text_present");
    json.put("text", "I am another div");
    
    AbstractAction store = actionFactory.createAction(json);
    Assert.assertEquals(store.transform(), 
        "String text_present = \"I am another div\";\n");
    
    json.put("text", "${text_variable}");
    store = actionFactory.createAction(json);
    Assert.assertEquals(store.transform(), 
        "text_present = text_variable;\n");
  }

  @Test
  public void testStoreCookiePresent() throws IOException {
    json.put("type", "storeCookiePresent");
    json.put("variable", "cookie_is_present");
    json.put("name", "test_cookie");
    
    AbstractAction storeCookiePresent = actionFactory.createAction(json);
    Assert.assertEquals(storeCookiePresent.transform(), 
        "boolean cookie_is_present = (wd.manage().getCookieNamed(\"test_cookie\") != null);\n");
    
    json.put("name", "${name_variable}");
    storeCookiePresent = actionFactory.createAction(json);
    Assert.assertEquals(storeCookiePresent.transform(), 
        "cookie_is_present = (wd.manage().getCookieNamed(name_variable) != null);\n");
  }

  @Test
  public void testStoreCurrentUrl() throws IOException {
    json.put("type", "storeCurrentUrl");
    json.put("variable", "url");

    AbstractAction storeCurrentUrl = actionFactory.createAction(json);
    Assert.assertEquals(storeCurrentUrl.transform(), "String url = wd.getCurrentUrl();\n");
  }

  @Test
  public void testStoreAlertPresent() throws IOException {
    json.put("type", "storeAlertPresent");
    json.put("variable", "isPresent");
    
    AbstractAction action = actionFactory.createAction(json);
    Assert.assertEquals(action.transform(), "boolean isPresent = isAlertPresent(wd);\n");
  }

  @Test
  public void testStoreEval() throws IOException {
    json.put("type", "storeEval");
    json.put("variable", "test");
    json.put("script", "alert('test_script');");
    
    AbstractAction action = actionFactory.createAction(json);
    Assert.assertEquals(action.transform(), "Object test = wd.executeScript(\"alert('test_script');\");\n");
  }

  @Test
  public void testStoreAlertText() throws IOException {
    json.put("type", "storeAlertText");
    json.put("variable", "alert_text");
    
    AbstractAction storeAlertText = actionFactory.createAction(json);
    Assert.assertEquals(storeAlertText.transform(), "String alert_text = wd.switchTo().alert().getText();\n");
  }

  @Test
  public void testStoreElementStyle() throws IOException {
    json.put("type", "storeElementStyle");
    json.put("propertyName", "height");
    json.put("variable", "element_style");
    
    locator.put("type", "id");
    locator.put("value", "i am a id");
    json.put("locator", locator);
    
    AbstractAction storeElementStyle = actionFactory.createAction(json);
    Assert.assertEquals(storeElementStyle.transform(), 
        "String element_style = wd.findElement(By.id(\"i am a id\")).getCssValue(\"height\");\n");
  }

  @Test
  public void testStoreTitle() throws IOException {
    json.put("type", "storeTitle");
    json.put("variable", "title");
    
    AbstractAction action = actionFactory.createAction(json);
    Assert.assertEquals(action.transform(), "String title = wd.getTitle();\n");
  }

  @Test
  public void testStoreElementValue() throws IOException {
    json.put("type", "storeElementValue");
    json.put("variable", "var");
    
    locator.put("type", "id");
    locator.put("value", "i am id");
    json.put("locator", locator);
    
    AbstractAction action = actionFactory.createAction(json);
    Assert.assertEquals(action.transform(), "String var = wd.findElement(By.id(\"i am id\")).getAttribute(\"value\");\n");
  }

  @Test
  public void testStoreElementSelected() throws IOException {
    json.put("type","storeElementSelected");
    json.put("variable", "var");
    
    locator.put("type", "id");
    locator.put("value", "i am id");
    json.put("locator", locator);
    
    AbstractAction action = actionFactory.createAction(json);
    Assert.assertEquals(action.transform(), "boolean var = (wd.findElement(By.id(\"i am id\")).isSelected());\n");
  }

  @Test
  public void testStoreCookieByName() throws IOException {
    json.put("type", "storeCookieByName");
    json.put("variable", "storeCookie");
    json.put("name", "cookie_name");
    
    AbstractAction storeCookieByName = actionFactory.createAction(json);
    Assert.assertEquals(storeCookieByName.transform(), 
        "String storeCookie = wd.manage().getCookieNamed(\"cookie_name\").getValue();\n");
  }
}
