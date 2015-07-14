/**
 * 
 */
package org.ats.services.functional.action;

import java.io.IOException;

import org.ats.services.functional.Value;
import org.ats.services.functional.locator.AbstractLocator;
import org.rythmengine.Rythm;

/**
 * @author TrinhTV3
 *
 * Email: TrinhTV3@fsoft.com.vn
 */
@SuppressWarnings("serial")
public class AssertElementAttribute extends AbstractAction{

  private Value attributeName,value;
  
  private AbstractLocator locator;
  
  private boolean negated;
  
  public AssertElementAttribute(Value attributeName, Value value, AbstractLocator locator, boolean negated) {
    this.attributeName = attributeName;
    this.value = value;
    this.locator = locator;
    this.negated = negated;
  }
  
  public String transform() throws IOException {
    StringBuilder sb = new StringBuilder(negated ? "assertNotEquals(" : "assertEquals(");
    sb.append("wd.findElement(@locator).getAttribute(").append(attributeName).append("), @value);\n");
    return Rythm.render(sb.toString(),locator.transform(),value.transform());
  }

  public String getAction() {
    return "assertElementAttribute";
  }

}
