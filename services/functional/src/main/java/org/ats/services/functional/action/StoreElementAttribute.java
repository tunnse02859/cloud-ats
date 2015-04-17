/**
 * 
 */
package org.ats.services.functional.action;

import java.io.IOException;

import org.ats.services.functional.Value;
import org.ats.services.functional.locator.ILocator;
import org.rythmengine.Rythm;

/**
 * @author TrinhTV3
 *
 * Email: TrinhTV3@fsoft.com.vn
 */
public class StoreElementAttribute implements IAction{

  private Value attributeName;
  
  private String variable;
  
  private ILocator locator;
  
  public StoreElementAttribute(String variable, Value attributeName, ILocator locator) {
    this.variable = variable;
    this.attributeName = attributeName;
    this.locator = locator;
  }
  
  public String transform() throws IOException {
    
    StringBuilder sb = new StringBuilder("String @variable = ");
    sb.append(" wd.findElement(@locator).getAttribute(@attributeName);\n");
    return Rythm.render(sb.toString(), variable, locator.transform(), attributeName.transform());
  }

  public String getAction() {
    return "testStoreElementAttribute";
  }

}
