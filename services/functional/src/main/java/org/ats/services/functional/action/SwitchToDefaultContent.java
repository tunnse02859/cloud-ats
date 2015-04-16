/**
 * 
 */
package org.ats.services.functional.action;

import java.io.IOException;

/**
 * @author TrinhTV3
 *
 * Email: TrinhTV3@fsoft.com.vn
 */
public class SwitchToDefaultContent implements IAction {

  /**
   * 
   */
  public SwitchToDefaultContent() {
  }
  public String transform() throws IOException {
    
    StringBuilder sb = new StringBuilder("wd = (FirefoxDriver) wd.switchTo().switchToDefaultContent();\n");
    return sb.toString();
  }

  public String getAction() {
    return "testSwitchDefaultContent";
  }

  
}
