/**
 * 
 */
package org.ats.services.functional.action;

import java.io.IOException;

import org.ats.services.functional.Value;

/**
 * @author TrinhTV3
 *
 * Email: TrinhTV3@fsoft.com.vn
 */
public class Store implements IAction {

  private Value text;
  private String variable;
  
  public Store(Value text, String variable) {
    this.text = text;
    this.variable = variable;
  }
  public String transform() throws IOException {
    StringBuilder sb = new StringBuilder("String ").append(variable).append(" = \"\" + ");
    sb.append(text);
    sb.append(";\n");
    return sb.toString();
  }

  public String getAction() {
    return "store";
  }

}
