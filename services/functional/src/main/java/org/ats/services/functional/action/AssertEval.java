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
public class AssertEval implements IAction {

  private Value script;
  
  private Value value;
  
  public AssertEval(Value script, Value value, boolean negated) {

    this.script = script;
    this.value = value;
  }
  @Override
  public String transform() throws IOException {
    
    StringBuilder sb = new StringBuilder();
    sb.append("assertEquals(wd.executeScript(@script), @value);\n");
    
    return Rythm.render(sb.toString(), script.transform(), value.transform());
  }

  @Override
  public String getAction() {
    return "testAssertEval";
  }

}
