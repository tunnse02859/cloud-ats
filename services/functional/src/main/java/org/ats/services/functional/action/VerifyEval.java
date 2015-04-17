/**
 * 
 */
package org.ats.services.functional.action;

import java.io.IOException;

import org.ats.services.functional.Value;
import org.rythmengine.Rythm;

/**
 * @author TrinhTV3
 *
 * Email: TrinhTV3@fsoft.com.vn
 */
public class VerifyEval implements IAction {

  private Value script;
  
  private Value value;
  
  private boolean negated;
  
  /**
   * 
   */
  public VerifyEval(Value script, Value value, boolean negated) {
    
    this.script = script;
    this.value = value;
    this.negated = negated;
  }
  
  @Override
  public String transform() throws IOException {
    
    StringBuilder sb = new StringBuilder("if (").append(negated ? "" : "!").append("wd.executeScript(@script).equals(@value)) {\n");
    sb.append("System.out.println(\"").append(negated ? "!" : "").append("verifyEval failed\");\n");
    sb.append("}\n");
    
    return Rythm.render(sb.toString(), script.transform(), value.transform());
  }

  @Override
  public String getAction() {
    return "testVerifyEval";
  }

}
