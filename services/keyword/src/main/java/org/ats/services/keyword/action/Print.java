/**
 * 
 */
package org.ats.services.keyword.action;

import java.io.IOException;

import org.ats.services.keyword.Value;
import org.rythmengine.Rythm;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Apr 16, 2015
 */
@SuppressWarnings("serial")
public class Print extends AbstractAction {
  
  private Value text;
  
  public Print(Value text) {
    this.text = text;
  }

  public String transform() throws IOException {
    StringBuilder sb = new StringBuilder();
    sb.append("System.out.println(");
    sb.append(text);
    sb.append(");\n");
    sb.append("     System.out.println(\"[End][Step]\"); \n");
    return Rythm.render(sb.toString(), text.transform());
  }

  public String getAction() {
    return "print";
  }

}
