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
public class VerifyBodyText implements IAction {

  private Value text;
  
  private boolean negated;
  
  public VerifyBodyText(Value text, boolean negated) {
    this.text = text;
    this.negated = negated;
  }
  
  public String transform() throws IOException {
    StringBuilder sb = new StringBuilder("if (").append(negated ? "" : "!");
    sb.append("wd.findElement(By.tagName(\"html\")).getText().equals(@text)) {\n");
    sb.append("System.out.println(\"").append(negated ? "!" : "").append("verifyBodyText failed\");\n}\n");
    return Rythm.render(sb.toString(), text.transform());
  }

  public String getAction() {
    return "verifyBodyText";
  }

}
