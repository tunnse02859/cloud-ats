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
public class StoreElementSelected implements IAction {
  
  private String variable;
  
  private ILocator locator;
  
  public StoreElementSelected(String variable, ILocator locator) {
    this.variable = variable;
    this.locator = locator;
  }

  @Override
  public String transform() throws IOException {
    
    StringBuilder sb = new StringBuilder("boolean "+variable);
    sb.append(" = (wd.findElement(@locator).isSelected());\n");
    
    return Rythm.render(sb.toString(), locator.transform());
  }

  @Override
  public String getAction() {
    return "storeElementSelected";
  }

}
