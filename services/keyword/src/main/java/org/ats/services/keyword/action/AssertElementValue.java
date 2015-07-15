/**
 * 
 */
package org.ats.services.keyword.action;

import java.io.IOException;

import org.ats.services.keyword.Value;
import org.ats.services.keyword.locator.AbstractLocator;
import org.rythmengine.Rythm;

/**
 * @author TrinhTV3
 *
 * Email: TrinhTV3@fsoft.com.vn
 */
@SuppressWarnings("serial")
public class AssertElementValue extends AbstractAction {

  private Value value;
  
  private boolean negated;
  
  private AbstractLocator locator;
  
  public AssertElementValue(Value value, AbstractLocator locator, boolean negated) {
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
