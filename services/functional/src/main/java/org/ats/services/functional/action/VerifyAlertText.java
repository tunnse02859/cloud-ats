/**
 * 
 */
package org.ats.services.functional.action;

import java.io.IOException;

import org.ats.common.MapBuilder;
import org.ats.services.functional.Value;
import org.rythmengine.RythmEngine;

/**
 * @author NamBV2
 *
 * Apr 17, 2015
 */
@SuppressWarnings("serial")
public class VerifyAlertText extends AbstractAction {

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
    sb.append(")) {\n      System.out.println(\"").append(negated ? "!" : "").append("verifyAlertText failed\");\n    }\n");
    
    RythmEngine engine = new RythmEngine(new MapBuilder<String, Boolean>("codegen.compact", false).build());
    return engine.render(sb.toString(), text.transform());
  }

  public String getAction() {
    return "verifyAlertText";
  }

}
