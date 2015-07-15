/**
 * 
 */
package org.ats.services.keyword.action;

import java.io.IOException;

import org.ats.services.keyword.VariableFactory;
import org.ats.services.keyword.VariableFactory.DataType;
import org.ats.services.keyword.locator.AbstractLocator;
import org.rythmengine.Rythm;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Apr 13, 2015
 */
@SuppressWarnings("serial")
public class StoreText extends AbstractAction {

  private String variable;
  
  private AbstractLocator locator;
  
  private VariableFactory factory;
  
  public StoreText(String variable, AbstractLocator locator, VariableFactory factory) {
    this.variable = variable;
    this.locator = locator;
    this.factory = factory;
  }
  
  public String transform() throws IOException {
    StringBuilder sb = new StringBuilder(factory.getVariable(DataType.STRING, variable));
    sb.append(" = wd.findElement(@locator).getText();\n");
    return Rythm.render(sb.toString(), locator.transform());
  }

  public String getAction() {
    return "storeText";
  }

}
