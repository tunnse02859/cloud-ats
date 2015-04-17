/**
 * 
 */
package org.ats.services.functional.action;

import java.io.IOException;

import org.ats.services.functional.locator.ILocator;

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
    return null;
  }

  @Override
  public String getAction() {
    return "storeElementValue";
  }

}
