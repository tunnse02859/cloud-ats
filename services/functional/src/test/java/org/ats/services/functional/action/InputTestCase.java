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
public class InputTestCase {
  
  private ActionFactory actionFactory;
  
  private ObjectNode json;
  
  private ObjectNode locator;
  
  private ObjectNode targetLocator;
  
  @BeforeMethod
  public void init() throws Exception {
    Injector injector = Guice.createInjector(new FunctionalModule());
    this.actionFactory = injector.getInstance(ActionFactory.class);
    ObjectMapper mapper = new ObjectMapper();
    this.json = mapper.createObjectNode();
    this.locator = mapper.createObjectNode();
    this.targetLocator = mapper.createObjectNode();
  }

  @Test
  public void testDoubleClickElement() throws IOException {
    json.put("type", "doubleClickElement");
    locator.put("type", "id");
    locator.put("value", "i am id");
    json.put("locator", locator);
    
    IAction action = actionFactory.createAction(json);
    Assert.assertEquals(action.transform(), "new Actions(wd).doubleClick(wd.findElement(By.id(\"i am id\"))).build().perform();\n");

    locator.put("value", "${value}");
    json.put("locator", locator);
    action = actionFactory.createAction(json);
    Assert.assertEquals(action.transform(), "new Actions(wd).doubleClick(wd.findElement(By.id(value))).build().perform();\n");
  }
  
  @Test
  public void testDragToAndDropElement() throws IOException {
    json.put("type", "dragToAndDropElement");
    
    locator.put("type", "id");
    locator.put("value", "i am source");
    json.put("locator", locator);
    
    targetLocator.put("type", "id");
    targetLocator.put("value", "i am dest");
    json.put("targetLocator", targetLocator);
    
    IAction action = actionFactory.createAction(json);
    Assert.assertEquals(action.transform(), "new Actions(wd).dragAndDrop(wd.findElement(By.id(\"i am source\")), wd.findElement(By.id(\"i am dest\")).build().perform();\n");
    
    locator.put("type", "id");
    locator.put("value", "${source}");
    json.put("locator", locator);
    
    targetLocator.put("type", "id");
    targetLocator.put("value", "${dest}");
    json.put("targetLocator", targetLocator);
    
    action = actionFactory.createAction(json);
    Assert.assertEquals(action.transform(), "new Actions(wd).dragAndDrop(wd.findElement(By.id(source)), wd.findElement(By.id(dest)).build().perform();\n");
  }
  
  @Test
  public void testClearSelections() throws IOException {
    json.put("type", "clearSelections");
    locator.put("type", "id");
    locator.put("value", "foo");
    json.put("locator", locator);
    
    IAction clearSelections = actionFactory.createAction(json);
    Assert.assertEquals(clearSelections.transform(), "new Select(wd.findElement(By.id(\"foo\"))).deselectAll();\n");
  }
  
  @Test
  public void testClickAndHoldElement() throws IOException {
    json.put("type", "clickAndHoldElement");
    locator.put("type", "id");
    locator.put("value", "foo");
    json.put("locator", locator);
    
    IAction clickAndHoldElement = actionFactory.createAction(json);
    Assert.assertEquals(clickAndHoldElement.transform(), "new Actions(wd).clickAndHold(wd.findElement(By.id(\"foo\"))).build.perform();\n");
  }
  
  @Test
  public void testClickElement() throws IOException {
    json.put("type", "clickElement");
    locator.put("type", "link text");
    locator.put("value", "i am a link");
    json.put("locator", locator);
    
    IAction clickElement = actionFactory.createAction(json);
    Assert.assertEquals(clickElement.transform(), "wd.findElement(By.linkText(\"i am a link\")).click();\n");
  }
  
  @Test
  public void testMouseOverElement() throws IOException {
    json.put("type", "mouseOverElement");
    locator.put("type", "id");
    locator.put("value", "foo");
    json.put("locator", locator);
    
    IAction mouseOverElement = actionFactory.createAction(json);
    Assert.assertEquals(mouseOverElement.transform(), "new Actions(wd).moveToElement(wd.findElement(By.id(\"foo\"))).build().perform();\n");
  }
  
  @Test
  public void testReleaseElement() throws IOException {
    json.put("type", "releaseElement");
    locator.put("type", "id");
    locator.put("value", "foo");
    json.put("locator", locator);
    
    IAction releaseElement = actionFactory.createAction(json);
    Assert.assertEquals(releaseElement.transform(), "new Actions(wd).release(wd.findElement(By.id(\"foo\"))).build.perform();\n");
  }
  
  @Test
  public void testSendKeysToElement() throws IOException {
    json.put("type", "sendKeysToElement");
    json.put("text", "w00t");
    locator.put("type", "id");
    locator.put("value", "comments");
    json.put("locator", locator);
    
    IAction sendKeysToElement = actionFactory.createAction(json); 
    Assert.assertEquals(sendKeysToElement.transform(), 
    "wd.findElement(By.id(\"comments\")).click();\n" +
    "wd.findElement(By.id(\"comments\")).sendKeys(\"w00t\");\n");
  }
  
  @Test
  public void testSetElementNotSelected() throws IOException {
    json.put("type", "setElementNotSelected");
    locator.put("type", "id");
    locator.put("value", "unchecked_checkbox");
    json.put("locator", locator);
    
    IAction setElementNotSelected = actionFactory.createAction(json);
    Assert.assertEquals(setElementNotSelected.transform(),
            "if (wd.findElement(By.id(\"unchecked_checkbox\")).isSelected()) {\n" +
            "wd.findElement(By.id(\"unchecked_checkbox\")).click();\n" +
            "}\n");
  }
  
  @Test
  public void testSetElementSelected() throws IOException {
    json.put("type", "setElementSelected");
    locator.put("type", "id");
    locator.put("value", "unchecked_checkbox");
    json.put("locator", locator);

    IAction selected = actionFactory.createAction(json);
    Assert.assertEquals(selected.transform(),
    "if (!wd.findElement(By.id(\"unchecked_checkbox\")).isSelected()) {\n" +
      "wd.findElement(By.id(\"unchecked_checkbox\")).click();\n" +
      "}\n");
  }
  
  @Test
  public void testSetElementText() throws IOException {
    json.put("type", "setElementText");
    json.put("text", "foo");
    locator.put("type", "id");
    locator.put("value", "unchecked_checkbox");
    json.put("locator", locator);
    
    IAction setElementText = actionFactory.createAction(json);
    Assert.assertEquals(setElementText.transform(), 
    "wd.findElement(By.id(\"unchecked_checkbox\")).click();\n" +
    "wd.findElement(By.id(\"unchecked_checkbox\")).clear();\n" +
    "wd.findElement(By.id(\"unchecked_checkbox\")).sendKeys(\"foo\");\n"
        );
  }
  
  @Test
  public void testSubmitElement() throws IOException {
    json.put("type", "submitElement");
    locator.put("type", "id");
    locator.put("value", "comments");
    json.put("locator", locator);
    
    IAction submitElement = actionFactory.createAction(json);
    Assert.assertEquals(submitElement.transform(), "wd.findElement(By.id(\"comments\")).submit();\n");
  }
  
}
