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
public class VerifyCookiePresent implements IAction {

  private Value name;
  private boolean negated;
  
  /**
   * 
   */
  public VerifyCookiePresent(Value name, boolean negated) {
  
    this.name = name;
    this.negated = negated;
    
  }

  public String transform() throws IOException {
    
    StringBuilder sb = new StringBuilder("if (").append(negated ? "" : "!");
    sb.append("(wd.manage().getCookieNamed(@name) != null)) {\n");
    sb.append("System.out.println(\"").append(negated ? "!" : "").append("verifyCookiePresent failed\");\n");
    sb.append("}\n");
    
    return Rythm.render(sb.toString(), name.transform());
  }

  public String getAction() {
    return "verifyCookiePresent";
  }
}
