/**
 * 
 */
package org.ats.services.functional.action;

import java.io.IOException;

import org.ats.services.functional.Value;
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
  
  public StoreElementStyle(Value propertyName,String variable, ILocator locator) {
    this.propertyName = propertyName;
    this.variable = variable;
    this.locator = locator;
  }
  
  public String transform() throws IOException {
    StringBuilder sb = new StringBuilder("String ");
    sb.append(variable);
    sb.append(" = wd.findElement(@locator).getCssValue(");
    sb.append(propertyName);
    sb.append(");\n");
    return Rythm.render(sb.toString(), locator.transform(),propertyName.transform());
  }

  public String getAction() {
    return "storeElementStyle";
  }

}
