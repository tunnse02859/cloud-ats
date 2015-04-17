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
public class StoreEval implements IAction {

  private Value script;
  
  private String var;
  
  /**
   * 
   */
  public StoreEval(Value script, String var) {
  
    this.script = script;
    this.var = var;
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
