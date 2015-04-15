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
public class AssertionTestCase {

  @Test
  public void testAssertBodyText() throws IOException {
    
    Value value = new Value("not body text", true);
    
    AssertBodyText action = new AssertBodyText(value, true);
    Assert.assertEquals(action.transform(), "assertNotEquals(wd.findElement(By.tagName(\"html\")).getText(), not body text);\n");
  
    action = new AssertBodyText(value, false);
    
    Assert.assertEquals(action.transform(), "assertEquals(wd.findElement(By.tagName(\"html\")).getText(), not body text);\n");
  }
  
  @Test 
  public void testAssertCurrentUrl() throws IOException {
    
    Value value = new Value("http://google.com", true);
    
    AssertCurrentUrl action = new AssertCurrentUrl(value, true);
    
    Assert.assertEquals(action.transform(), "assertNotEquals(wd.getCurrentUrl(), http://google.com);\n");
    
    action = new AssertCurrentUrl(value, false);
    Assert.assertEquals(action.transform(), "assertEquals(wd.getCurrentUrl(), http://google.com);\n");
  }
  
  @Test
  public void testAssertElementPresent() throws IOException {
    
    IDLocator locator = new IDLocator(new Value("i am id", false));
    
    AssertElementPresent action = new AssertElementPresent(locator, true);
    
    Assert.assertEquals(action.transform(), "assertFalse((wd.findElements(By.id(\"i am id\")).size() != 0));\n");
    
    action = new AssertElementPresent(locator, false);
    Assert.assertEquals(action.transform(), "assertTrue((wd.findElements(By.id(\"i am id\")).size() != 0));\n");
  }
  
  @Test
  public void testAssertPageSource() throws IOException {
    
    Value value = new Value("page_source", true);
    
    AssertPageSource action = new AssertPageSource(value, true);
    
    Assert.assertEquals(action.transform(), "assertNotEquals(wd.getPageSource(), page_source);\n");
    
    action = new AssertPageSource(value, false);
    
    Assert.assertEquals(action.transform(), "assertEquals(wd.getPageSource(), page_source);\n");
  }
  
  @Test
  public void testAssertText() throws IOException {
    
    IDLocator locator = new IDLocator(new Value("i_am_an_id", false));
    
    AssertText action = new AssertText(locator, new Value("text", true), true);
    
    Assert.assertEquals(action.transform(), "assertNotEquals(wd.findElement(By.id(\"i_am_an_id\")).getText(), text);\n");
    
    action = new AssertText(locator, new Value("text", true), false);
    
    Assert.assertEquals(action.transform(), "assertEquals(wd.findElement(By.id(\"i_am_an_id\")).getText(), text);\n");
  }
  
  @Test
  public void testAssertTextPresent() throws IOException {
    Value value = new Value("text_present", true);
    
    AssertTextPresent action = new AssertTextPresent(value, true);
    
    Assert.assertEquals(action.transform(), "assertFalse(wd.findElement(By.tagName(\"html\")).getText().contains(text_present));\n");
    
    action = new AssertTextPresent(value, false);
    Assert.assertEquals(action.transform(), "assertTrue(wd.findElement(By.tagName(\"html\")).getText().contains(text_present));\n");
  }
}
