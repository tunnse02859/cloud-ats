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
    
    StringBuilder sb = new StringBuilder("String "+var);
    sb.append(" = ").append("wd.executeScript(@script);\n");
    
    return Rythm.render(sb.toString(), script.transform());
  }

  @Override
  public String getAction() {
    return "testStoreEval";
  }

}
