/**
 * 
 */
package org.ats.services.functional.action;

import java.io.IOException;

import org.ats.services.functional.Value;
import org.rythmengine.Rythm;

/**
 * @author NamBV2
 *
 * Apr 17, 2015
 */
public class AssertAlertText implements IAction{

  private Value text;
  
  private boolean negated;
  
  public AssertAlertText(Value text, boolean negated) {
    this.text = text;
    this.negated = negated;
  }
  
  public String transform() throws IOException {
    StringBuilder sb = new StringBuilder(negated ? "assertNotEquals(" : "assertEquals(");
    sb.append("wd.switchTo().alert().getText(), ");
    sb.append(text);
    sb.append(");\n");
    return Rythm.render(sb.toString(), text.transform());
  }

  public String getAction() {
    return "assertAlertText";
  }

}
