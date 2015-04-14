/**
 * 
 */
package org.ats.services.functional.action;

import java.io.IOException;

import org.ats.services.functional.Value;
import org.ats.services.functional.locator.IDLocator;
import org.testng.annotations.Test;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Apr 9, 2015
 */
public class ActionTestCase {

  @Test
  public void testSetElementText() throws IOException {
    IDLocator locator = new IDLocator(new Value("test", false));
    SetElementText action = new SetElementText(locator, new Value("This is a test", false));
    System.out.println(action.transform());
  }
}
