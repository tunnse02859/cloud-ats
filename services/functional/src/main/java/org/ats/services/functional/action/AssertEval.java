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
@SuppressWarnings("serial")
public class AssertEval extends  AbstractAction {

  private Value script;
  
  private Value value;
  
  private boolean negated;
  
  public AssertEval(Value script, Value value, boolean negated) {
    this.script = script;
    this.value = value;
    this.negated = negated;
  }
  @Override
  public String transform() throws IOException {
    StringBuilder sb = new StringBuilder(negated ? "assertNotEquals(" : "assertEquals(");
    sb.append("wd.executeScript(@script), @value);\n");
    
    return Rythm.render(sb.toString(), script.transform(), value.transform());
  }

  @Override
  public String getAction() {
    return "assertEval";
  }

}
