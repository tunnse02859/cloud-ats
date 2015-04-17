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
public class StoreAlertPresent implements IAction {

  private String var;
  
  /**
   * 
   */
  public StoreAlertPresent(String var) {
    this.var = var;
  }
  @Override
  public String transform() throws IOException {
    StringBuilder sb = new StringBuilder("boolean ").append(var+" = ");
    sb.append("isAlertPresent(wd);\n");
    return sb.toString();
  }

  @Override
  public String getAction() {
    return "testIsAlertPresent";
  }

}
