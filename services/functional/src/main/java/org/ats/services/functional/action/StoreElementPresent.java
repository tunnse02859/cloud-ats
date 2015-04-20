/**
 * 
 */
package org.ats.services.functional.action;

import java.io.IOException;

import org.ats.services.functional.VariableFactory;
import org.ats.services.functional.VariableFactory.DataType;
import org.ats.services.functional.locator.ILocator;
import org.rythmengine.Rythm;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Apr 13, 2015
 */
public class StoreElementPresent implements IAction {

  private ILocator locator;
  
  private String variable;
  
  private VariableFactory factory;
  
  public StoreElementPresent(ILocator locator, String variable, VariableFactory factory) {
    this.locator = locator;
    this.variable = variable;
    this.factory = factory;
  }
  
  public String transform() throws IOException {
    StringBuilder sb = new StringBuilder(factory.getVariable(DataType.BOOLEAN, variable));
    sb.append(" = (wd.findElements(@locator).size() != 0);\n");
    return Rythm.render(sb.toString(), locator.transform());
  }

  public String getAction() {
    return "storeElementPresent";
  }
}
  