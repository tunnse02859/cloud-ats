/**
 * 
 */
package org.ats.services.functional.action;

import java.io.IOException;

/**
 * @author NamBV2
 *
 * Apr 17, 2015
 */
public class StoreAlertText implements IAction{

  private String variable;
  
  public StoreAlertText(String variable) {
    this.variable = variable;
  }
  
  public String transform() throws IOException {
    StringBuilder sb = new StringBuilder("String ");
    sb.append(variable);
    sb.append(" = wd.switchTo().alert().getText();\n");
    return sb.toString();
  }

  @Override
  public String getAction() {
    return "storeAlertText";
  }

}
