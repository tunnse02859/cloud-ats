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
 * Apr 16, 2015
 */
public class StoreCookieByName implements IAction {

  private Value name;
  
  private String variable;
  
  public StoreCookieByName(Value name, String variable) {
    this.name = name;
    this.variable = variable;
  }
  
  public String transform() throws IOException {
    StringBuilder sb = new StringBuilder("String ");
    sb.append(variable);
    sb.append(" = wd.manage().getCookieNamed(");
    sb.append(name);
    sb.append(").getValue();\n");
    return Rythm.render(sb.toString(), name.transform());
  }

  public String getAction() {
    return "storeCookieByName";
  }

}
