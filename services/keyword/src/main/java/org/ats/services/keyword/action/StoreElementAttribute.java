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
 * @author TrinhTV3
 *
 * Email: TrinhTV3@fsoft.com.vn
 */
@SuppressWarnings("serial")
public class StoreElementAttribute extends AbstractAction{

  private Value attributeName;
  
  private String variable;
  
  private AbstractLocator locator;
  
  private VariableFactory factory;
  
  public StoreElementAttribute(String variable, Value attributeName, AbstractLocator locator, VariableFactory factory) {
    this.variable = variable;
    this.attributeName = attributeName;
    this.locator = locator;
    this.factory = factory;
  }
  
  public String transform() throws IOException {
    
    StringBuilder sb = new StringBuilder(factory.getVariable(DataType.STRING, variable)).append(" = ");
    sb.append("wd.findElement(@locator).getAttribute(@attributeName);\n");
    return Rythm.render(sb.toString(), locator.transform(), attributeName.transform());
  }

  public String getAction() {
    return "storeElementAttribute";
  }

}
