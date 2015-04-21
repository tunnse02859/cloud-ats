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
public class DismissAlert implements IAction {

  /**
   * 
   */
  public DismissAlert() {

  }
  @Override
  public String transform() throws IOException {
    return "wd.switchTo().alert().dismiss();\n";
  }

  @Override
  public String getAction() {
    return "dismissAlert";
  }

}
