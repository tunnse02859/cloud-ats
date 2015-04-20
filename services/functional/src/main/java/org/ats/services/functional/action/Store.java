/**
 * 
 */
package org.ats.services.functional.action;

import java.io.IOException;

import org.ats.services.functional.Value;
import org.ats.services.functional.VariableFactory;
import org.ats.services.functional.VariableFactory.DataType;

/**
 * @author TrinhTV3
 *
 * Email: TrinhTV3@fsoft.com.vn
 */
public class Store implements IAction {

  /** .*/
  private Value text;
  
  /** .*/
  private String variable;
  
  /** .*/
  private VariableFactory factory;
  
  public Store(Value text, String variable, VariableFactory factory) {
    this.text = text;
    this.variable = variable;
    this.factory = factory;
  }
  public String transform() throws IOException {
    StringBuilder sb = new StringBuilder(factory.getVariable(DataType.STRING, variable)).append(" = ");
    sb.append(text.transform());
    sb.append(";\n");
    return sb.toString();
  }

  public String getAction() {
    return "store";
  }

}
