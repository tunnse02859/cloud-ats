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
public class InputTestCase {

  @Test
  public void testDoubleClickElement() throws IOException {
    IDLocator locator = new IDLocator(new Value("i am id", false));
    
    DoubleClickElement action = new DoubleClickElement(locator);
    Assert.assertEquals(action.transform(), "new Actions(wd).doubleClick(wd.findElement(By.id(\"i am id\"))).build().perform();\n");
    
    locator = new IDLocator(new Value("i am id", true));
    action = new DoubleClickElement(locator);
    Assert.assertEquals(action.transform(), "new Actions(wd).doubleClick(wd.findElement(By.id(i am id))).build().perform();\n");
    
  }
  
  @Test
  public void testDragToAndDropElement() throws IOException {
    IDLocator source = new IDLocator(new Value("i am source", false));
    IDLocator dest = new IDLocator(new Value("i am dest", false));
    
    DragToAndDropElement action = new DragToAndDropElement(source, dest);
    Assert.assertEquals(action.transform(), "new Actions(wd).dragAndDrop(wd.findElement(By.id(\"i am source\")), wd.findElement(By.{locator2By}({locator2}))).build().perform();\n");
    
    source = new IDLocator(new Value("i am source", true));
    dest = new IDLocator(new Value("i am dest", true));
    
    action = new DragToAndDropElement(source, dest);
    Assert.assertEquals(action.transform(), "new Actions(wd).dragAndDrop(wd.findElement(By.id(i am source)), wd.findElement(By.{locator2By}({locator2}))).build().perform();\n");
  
  }
  
  @Test
  public void testClearSelections() throws IOException {
    IDLocator locator = new IDLocator(new Value("foo", false));
    ClearSelections clearSelections = new ClearSelections(locator);
    Assert.assertEquals(clearSelections.transform(), "new Select(wd.findElement(By.id(\"foo\"))).deselectAll();\n");
  }
  
  @Test
  public void testClickAndHoldElement() throws IOException {
    IDLocator locator = new IDLocator(new Value("foo", false));
    ClickAndHoldElement clickAndHoldElement = new ClickAndHoldElement(locator);
    Assert.assertEquals(clickAndHoldElement.transform(), "new Actions(wd).clickAndHold(wd.findElement(By.id(\"foo\"))).build.perform();\n");
  }
  
  @Test
  public void testClickElement() throws IOException {
    LinkTextLocator locator = new LinkTextLocator(new Value("i am a link", false));
    ClickElement clickElement = new ClickElement(locator);
    Assert.assertEquals(clickElement.transform(), "wd.findElement(By.linkText(\"i am a link\")).click();\n");
  }
  
  @Test
  public void testMouseOverElement() throws IOException {
    IDLocator locator = new IDLocator(new Value("foo", false));
    MouseOverElement mouseOverElement = new MouseOverElement(locator);
    Assert.assertEquals(mouseOverElement.transform(), "new Actions(wd).moveToElement(wd.findElement(By.id(\"foo\"))).build().perform();\n");
  }
  
  @Test
  public void testReleaseElement() throws IOException {
    IDLocator locator = new IDLocator(new Value("foo", false));
    ReleaseElement releaseElement = new ReleaseElement(locator);
    Assert.assertEquals(releaseElement.transform(), "new Actions(wd).release(wd.findElement(By.id(\"foo\"))).build.perform();\n");
  }
  
  @Test
  public void testSendKeysToElement() throws IOException {
    Value text = new Value("w00t", false);
    IDLocator locator = new IDLocator(new Value("comments", false));
    SendKeysToElement sendKeysToElement = new SendKeysToElement(locator, text);
    Assert.assertEquals(sendKeysToElement.transform(), 
    "wd.findElement(By.id(\"comments\")).click();\n" +
    "wd.findElement(By.id(\"comments\")).sendKeys(\"w00t\");\n");
  }
  
  @Test
  public void testSetElementNotSelected() throws IOException {
    IDLocator locator = new IDLocator(new Value("unchecked_checkbox", false));
    SetElementNotSelected setElementNotSelected = new SetElementNotSelected(locator);
    Assert.assertEquals(setElementNotSelected.transform(),
            "if (wd.findElement(By.id(\"unchecked_checkbox\")).isSelected()) {\n" +
            "wd.findElement(By.id(\"unchecked_checkbox\")).click();\n" +
            "}\n");
  }
  
  @Test
  public void testSetElementSelected() throws IOException {
    IDLocator locator = new IDLocator(new Value("unchecked_checkbox", false));
    SetElementSelected selected = new SetElementSelected(locator);
    Assert.assertEquals(selected.transform(),
    "if (!wd.findElement(By.id(\"unchecked_checkbox\")).isSelected()) {\n" +
      "wd.findElement(By.id(\"unchecked_checkbox\")).click();\n" +
      "}\n");
  }
  
  @Test
  public void testSetElementText() throws IOException {
    Value text = new Value("foo", false);
    IDLocator locator = new IDLocator(new Value("unchecked_checkbox",false));
    SetElementText setElementText = new SetElementText(locator, text);
    Assert.assertEquals(setElementText.transform(), 
    "wd.findElement(By.id(\"unchecked_checkbox\")).click();\n" +
    "wd.findElement(By.id(\"unchecked_checkbox\")).clear();\n" +
    "wd.findElement(By.id(\"unchecked_checkbox\")).sendKeys(\"foo\");\n"
        );
  }
  
  @Test
  public void testSubmitElement() throws IOException {
    IDLocator locator = new IDLocator(new Value("comments", false));
    SubmitElement submitElement = new SubmitElement(locator);
    Assert.assertEquals(submitElement.transform(), "wd.findElement(By.id(\"comments\")).submit();\n");
  }
  
}
