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
public class AcceptAlert implements IAction {

  /**
   * 
   */
  public AcceptAlert() {

  }
  @Override
  public String transform() throws IOException {
    return "wd.switchTo().alert().accept();\n";
  }

  @Override
  public String getAction() {
    return "acceptAlert";
  }

}
