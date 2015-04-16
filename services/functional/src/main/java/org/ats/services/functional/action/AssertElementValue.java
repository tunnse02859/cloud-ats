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
public class AssertElementValue implements IAction {

  private Value value;
  
  private boolean negated;
  
  private ILocator locator;
  
  public AssertElementValue(Value value, ILocator locator, boolean negated) {
    this.value = value;
    this.locator = locator;
    this.negated = negated;
  }
  
  public String transform() throws IOException {
    StringBuilder sb = new StringBuilder(negated ? "assertNotEquals(" : "assertEquals(");
    sb.append("wd.findElement(@locator).getAttribute(\"value\"), ").append(value).append(");\n");
    return Rythm.render(sb.toString(), locator.transform());
  }

  public String getAction() {
    return "assertElementValue";
  }

}
