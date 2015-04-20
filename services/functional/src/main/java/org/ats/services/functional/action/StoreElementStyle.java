/**
 * 
 */
package org.ats.services.functional.action;

import java.io.IOException;

import org.ats.services.functional.Value;
import org.ats.services.functional.VariableFactory;
import org.ats.services.functional.VariableFactory.DataType;
import org.ats.services.functional.locator.ILocator;
import org.rythmengine.Rythm;

/**
 * @author NamBV2
 *
 * Apr 17, 2015
 */
public class StoreElementStyle implements IAction{

  private Value propertyName;
  
  private String variable;
  
  private ILocator locator;
  
  private VariableFactory factory;
  
  public StoreElementStyle(Value propertyName,String variable, ILocator locator, VariableFactory factory) {
    this.propertyName = propertyName;
    this.variable = variable;
    this.locator = locator;
    this.factory = factory;
  }
  
  public String transform() throws IOException {
    StringBuilder sb = new StringBuilder(factory.getVariable(DataType.STRING, variable));
    sb.append(" = wd.findElement(@locator).getCssValue(");
    sb.append(propertyName);
    sb.append(");\n");
    return Rythm.render(sb.toString(), locator.transform(),propertyName.transform());
  }

  public String getAction() {
    return "storeElementStyle";
  }

}
