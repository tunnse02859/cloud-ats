/**
 * 
 */
package org.ats.services.functional.action;

import java.io.IOException;

import org.ats.services.functional.Value;

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
    return null;
  }

  @Override
  public String getAction() {
    return null;
  }

}
