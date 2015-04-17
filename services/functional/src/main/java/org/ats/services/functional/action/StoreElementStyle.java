/**
 * 
 */
package org.ats.services.functional.action;

import java.io.IOException;

import org.ats.services.functional.Value;
import org.ats.services.functional.locator.ILocator;

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
    return null;
  }

  public String getAction() {
    return null;
  }

}
