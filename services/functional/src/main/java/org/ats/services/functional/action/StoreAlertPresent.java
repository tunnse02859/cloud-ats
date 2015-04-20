/**
 * 
 */
package org.ats.services.functional.action;

import java.io.IOException;

import org.ats.services.functional.VariableFactory;
import org.ats.services.functional.VariableFactory.DataType;

/**
 * @author TrinhTV3
 *
 * Email: TrinhTV3@fsoft.com.vn
 */
public class StoreAlertPresent implements IAction {

  /** .*/
  private String variable;
  
  /** .*/
  private VariableFactory factory;
  
  public StoreAlertPresent(String variable, VariableFactory factory) {
    this.variable = variable;
    this.factory = factory;
  }
  @Override
  public String transform() throws IOException {
    StringBuilder sb = new StringBuilder(factory.getVariable(DataType.BOOLEAN, variable)).append(" = ");
    sb.append("isAlertPresent(wd);\n");
    return sb.toString();
  }

  @Override
  public String getAction() {
    return "storeAlertPresent";
  }

}
