/**
 * 
 */
package org.ats.services.functional.action;

import java.io.IOException;

import org.ats.services.functional.Value;
import org.ats.services.functional.locator.ILocator;
import org.rythmengine.Rythm;

/**
 * @author TrinhTV3
 *
 * Email: TrinhTV3@fsoft.com.vn
 */
public class VerifyElementValue implements IAction {

  private ILocator locator;
  
  private Value value;
  
  private boolean negated;

  /**
   * 
   */
  public VerifyElementValue(ILocator locator, Value value, boolean negated) {
    
    this.locator = locator;
    this.value = value;
    this.negated = negated;
    
  }
  public String transform() throws IOException {
    
    StringBuilder sb = new StringBuilder("if (").append(negated ? "" : "!");
    sb.append("wd.findElement(@locator).getAttribute(\"value\").equals(@value)) {\n");
    sb.append("System.out.println(\"").append(negated ? "!" : "").append("verifyElementValue failed\");\n");
    sb.append("}\n");
    return Rythm.render(sb.toString(), locator.transform(), value.transform());
  }

  public String getAction() {
    return "testVerifyElementValue";
  }
  
}
