/**
 * 
 */
package org.ats.services.functional.action;

import java.io.IOException;

import org.ats.services.functional.VariableFactory;
import org.ats.services.functional.VariableFactory.DataType;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Apr 14, 2015
 */
public class StoreCurrentUrl implements IAction {

  private String variable;
  
  private VariableFactory factory;
  
  public StoreCurrentUrl(String variable, VariableFactory factory) {
    this.variable = variable;
    this.factory = factory;
  }
  
  public String transform() throws IOException {
    StringBuilder sb = new StringBuilder(factory.getVariable(DataType.STRING, variable)).append(" = wd.getCurrentUrl();\n");
    return sb.toString();
  }

  public String getAction() {
    return "storeCurrentUrl";
  }
}
