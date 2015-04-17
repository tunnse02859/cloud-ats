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
public class Print implements IAction {
  
  private Value text;
  
  public Print(Value text) {
    this.text = text;
  }

  public String transform() throws IOException {
    StringBuilder sb = new StringBuilder("System.out.println(");
    sb.append(text);
    sb.append(");\n");
    return Rythm.render(sb.toString(), text.transform());
  }

  public String getAction() {
    return "print";
  }

}
