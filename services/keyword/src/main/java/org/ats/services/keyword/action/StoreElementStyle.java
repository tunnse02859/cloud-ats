/**
 * 
 */
package org.ats.services.keyword.action;

import java.io.IOException;

import org.ats.services.keyword.Value;
import org.ats.services.keyword.VariableFactory;
import org.ats.services.keyword.VariableFactory.DataType;
import org.ats.services.keyword.locator.AbstractLocator;
import org.rythmengine.Rythm;

/**
 * @author NamBV2
 *
 * Apr 17, 2015
 */
@SuppressWarnings("serial")
public class StoreElementStyle extends AbstractAction{

  private Value propertyName;
  
  private String variable;
  
  private AbstractLocator locator;
  
  private VariableFactory factory;
  
  public StoreElementStyle(Value propertyName,String variable, AbstractLocator locator, VariableFactory factory) {
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
