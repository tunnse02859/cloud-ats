/**
 * 
 */
package org.ats.services.functional.action;

import java.io.IOException;

import org.ats.services.functional.VariableFactory;
import org.ats.services.functional.VariableFactory.DataType;

/**
 * @author NamBV2
 *
 * Apr 17, 2015
 */
public class StoreAlertText implements IAction{

  /** .*/
  private String variable;
  
  /** .*/
  private VariableFactory factory;
  
  public StoreAlertText(String variable, VariableFactory factory) {
    this.variable = variable;
    this.factory = factory;
  }
  
  public String transform() throws IOException {
    StringBuilder sb = new StringBuilder(factory.getVariable(DataType.STRING, variable));
    sb.append(" = wd.switchTo().alert().getText();\n");
    return sb.toString();
  }

  @Override
  public String getAction() {
    return "storeAlertText";
  }

}
