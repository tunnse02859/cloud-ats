/**
 * 
 */
package org.ats.services.functional.action;

import java.io.IOException;

import org.ats.services.functional.Value;
import org.ats.services.functional.VariableFactory;
import org.ats.services.functional.VariableFactory.DataType;
import org.rythmengine.Rythm;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Apr 13, 2015
 */
@SuppressWarnings("serial")
public class StoreTextPresent extends AbstractAction {

  private String variable;
  
  private Value text;
  
  private VariableFactory factory;
  
  public StoreTextPresent(String variable, Value text, VariableFactory factory) {
    this.variable = variable;
    this.text = text;
    this.factory = factory;
  }
  
  public String transform() throws IOException {
    StringBuilder sb = new StringBuilder(factory.getVariable(DataType.BOOLEAN, variable));
    sb.append(" = wd.findElement(By.tagName(\"html\")).getText().contains(@text);\n");
    return Rythm.render(sb.toString(), text.transform());
  }

  public String getAction() {
    return "storeTextPresent";
  }

}
