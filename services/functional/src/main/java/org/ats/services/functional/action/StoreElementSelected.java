/**
 * 
 */
package org.ats.services.functional.action;

import java.io.IOException;

import org.ats.services.functional.VariableFactory;
import org.ats.services.functional.VariableFactory.DataType;
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
  
  private VariableFactory factory;
  
  public StoreElementSelected(String variable, ILocator locator, VariableFactory factory) {
    this.variable = variable;
    this.locator = locator;
    this.factory = factory;
  }

  @Override
  public String transform() throws IOException {
    
    StringBuilder sb = new StringBuilder(factory.getVariable(DataType.BOOLEAN, variable));
    sb.append(" = (wd.findElement(@locator).isSelected());\n");
    
    return Rythm.render(sb.toString(), locator.transform());
  }

  @Override
  public String getAction() {
    return "storeElementSelected";
  }

}
