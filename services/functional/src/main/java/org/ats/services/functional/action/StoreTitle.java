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
 * Apr 16, 2015
 */
public class StoreTitle implements IAction {
  
  private String variable;
  
  private VariableFactory factory;

  public StoreTitle(String variable, VariableFactory factory) {
    this.variable = variable;
    this.factory = factory;
  }
  
  @Override
  public String transform() throws IOException {
    return factory.getVariable(DataType.STRING, variable) + " = wd.getTitle();\n";
  }

  @Override
  public String getAction() {
    return "storeTitle";
  }

}
