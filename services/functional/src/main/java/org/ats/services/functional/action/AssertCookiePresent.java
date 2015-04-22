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
@SuppressWarnings("serial")
public class AssertCookiePresent extends AbstractAction{

  private Value name;
  
  private boolean negated;
  
  public AssertCookiePresent(Value name, boolean negated) {
    
    this.name = name;
    this.negated = negated;
  }
  
  public String transform() throws IOException {
    
    StringBuilder sb = new StringBuilder(negated ? "assertFalse(" : "assertTrue(");
    sb.append("(wd.manage().getCookieNamed(").append(name).append(") != null));\n");
    
    return sb.toString();
  }

  public String getAction() {
    return "assertCookiePresent";
  }

}
