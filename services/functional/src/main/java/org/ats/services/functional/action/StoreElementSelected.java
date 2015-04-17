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
public class StoreElementSelected implements IAction {
  
  private String variable;
  
  private ILocator locator;
  
  public StoreElementSelected(String variable, ILocator locator) {
    this.variable = variable;
    this.locator = locator;
  }

  @Override
  public String transform() throws IOException {
    return null;
  }

  @Override
  public String getAction() {
    return "storeElementSelected";
  }

}
