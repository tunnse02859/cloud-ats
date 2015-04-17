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
public class VerifyAlertText implements IAction{

  private Value text;
  
  private boolean negated;
  
  public VerifyAlertText(Value text, boolean negated) {
    
    this.text = text;
    this.negated = negated;
  }
  
  public String transform() throws IOException {
    StringBuilder sb = new StringBuilder(negated ? "if (wd." : "if (!wd.");
    sb.append("switchTo().alert().getText().equals(");
    sb.append(text);
    sb.append(")) {\nSystem.out.println(\"").append(negated ? "!" : "").append("verifyAlertText failed\");\n}\n");
    return Rythm.render(sb.toString(), text.transform());
  }

  public String getAction() {
    return "verifyAlertText";
  }

}
