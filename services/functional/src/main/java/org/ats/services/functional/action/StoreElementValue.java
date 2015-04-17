/**
 * 
 */
package org.ats.services.functional.action;

import java.io.IOException;

import org.ats.services.functional.locator.ILocator;
import org.rythmengine.Rythm;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Apr 16, 2015
 */
public class StoreElementValue implements IAction {
  
  private ILocator locator;
  
  private String variable;

  public StoreElementValue(ILocator locator, String variable) {
    this.locator = locator;
    this.variable = variable;
  }
  
  @Override
  public String transform() throws IOException {
    
    StringBuilder sb = new StringBuilder("String " + variable + " = ").append("wd.findElement(@locator).getAttribute(");
    sb.append("\"value\")");
    sb.append(";\n");
    
    return Rythm.render(sb.toString(), locator.transform());
  }

  @Override
  public String getAction() {
    return "storeElementValue";
  }

}
