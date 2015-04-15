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
}
