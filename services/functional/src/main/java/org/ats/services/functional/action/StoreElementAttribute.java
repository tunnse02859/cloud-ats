/**
 * 
 */
package org.ats.services.functional.action;

import java.io.IOException;

import org.ats.services.functional.Value;
import org.ats.services.functional.locator.ILocator;

/**
 * @author TrinhTV3
 *
 * Email: TrinhTV3@fsoft.com.vn
 */
public class StoreElementAttribute implements IAction{

  private Value variable, attributeName;
  
  private ILocator locator;
  
  public StoreElementAttribute(Value variable, Value attributeName, ILocator locator) {
    this.variable = variable;
    this.attributeName = attributeName;
    this.locator = locator;
  }
  
  public String transform() throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  public String getAction() {
    // TODO Auto-generated method stub
    return null;
  }

}
