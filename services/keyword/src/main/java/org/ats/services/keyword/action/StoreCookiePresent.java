/**
 * 
 */
package org.ats.services.keyword.action;

import java.io.IOException;

import org.ats.services.keyword.Value;
import org.ats.services.keyword.VariableFactory;
import org.ats.services.keyword.VariableFactory.DataType;

/**
 * @author TrinhTV3
 *
 * Email: TrinhTV3@fsoft.com.vn
 */
@SuppressWarnings("serial")
public class StoreCookiePresent extends AbstractAction{

  private Value name;
  
  private String variable;
  
  private VariableFactory factory;
  
  public StoreCookiePresent(String variable, Value name, VariableFactory factory) {
    this.variable = variable;
    this.name = name;
    this.factory = factory;
  }
  
  public String transform() throws IOException {
    StringBuilder sb = new StringBuilder(factory.getVariable(DataType.BOOLEAN, variable));
    sb.append(" = (wd.manage().getCookieNamed(").append(name).append(") != null);\n");
    sb.append("     System.out.println(\"[End][Step]\"); \n");
    return sb.toString();
  }

  public String getAction() {
    return "storeCookiePresent";
  }

}
