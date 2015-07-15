/**
 * 
 */
package org.ats.services.keyword.action;

import java.io.IOException;

import org.ats.services.keyword.VariableFactory;
import org.ats.services.keyword.VariableFactory.DataType;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Apr 16, 2015
 */
@SuppressWarnings("serial")
public class StoreTitle extends AbstractAction {
  
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
