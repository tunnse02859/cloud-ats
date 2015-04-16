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
public class StoreCookiePresent implements IAction{

  private Value name;
  
  private String variable;
  
  public StoreCookiePresent(String variable, Value name) {
    this.variable = variable;
    this.name = name;
  }
  
  public String transform() throws IOException {
    StringBuilder sb = new StringBuilder("boolean ").append(variable);
    sb.append(" = (wd.manage().getCookieNamed(");
    sb.append(name);
    sb.append(") != null);\n");
    return sb.toString();
  }

  public String getAction() {
    return "storeCookiePresent";
  }

}
