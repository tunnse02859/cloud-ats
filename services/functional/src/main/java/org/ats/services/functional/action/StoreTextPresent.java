/**
 * 
 */
package org.ats.services.functional.action;

import java.io.IOException;

import org.ats.services.functional.Value;
import org.rythmengine.Rythm;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Apr 13, 2015
 */
public class StoreTextPresent implements IAction {

  private String variable;
  
  private Value text;
  
  public StoreTextPresent(String variable, Value text) {
    this.variable = variable;
    this.text = text;
  }
  
  public String transform() throws IOException {
    StringBuilder sb = new StringBuilder("boolean ").append(variable);
    sb.append(" = wd.findElement(By.tagName(\"html\")).getText().contains(@text);\n");
    return Rythm.render(sb.toString(), text.transform());
  }

  public String getAction() {
    return "storeTextPresent";
  }

}
