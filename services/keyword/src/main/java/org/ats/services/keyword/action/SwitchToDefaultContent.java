/**
 * 
 */
package org.ats.services.keyword.action;

import java.io.IOException;

/**
 * @author TrinhTV3
 *
 * Email: TrinhTV3@fsoft.com.vn
 */
@SuppressWarnings("serial")
public class SwitchToDefaultContent extends AbstractAction {

  public String transform() throws IOException {
    StringBuilder sb = new StringBuilder("wd = (FirefoxDriver) wd.switchTo().switchToDefaultContent();\n");
    return sb.toString();
  }

  public String getAction() {
    return "switchToDefaultContent";
  }

  
}
