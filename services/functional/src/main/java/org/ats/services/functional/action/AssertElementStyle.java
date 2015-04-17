/**
 * 
 */
package org.ats.services.functional.action;

import java.io.IOException;

import org.ats.services.functional.Value;
import org.ats.services.functional.locator.ILocator;
import org.rythmengine.Rythm;

/**
 * @author NamBV2
 *
 * Apr 17, 2015
 */
public class AssertElementStyle implements IAction{

  private Value propertyName, value;
  
  private ILocator locator;
  
  private boolean negated;
  
  public AssertElementStyle(Value propertyName,Value value, ILocator locator, boolean negated) {
    this.propertyName = propertyName;
    this.value = value;
    this.locator = locator;
    this.negated = negated;
  }
  
  public String transform() throws IOException {
    StringBuilder sb = new StringBuilder(negated ? "assertNotEquals(" : "assertEquals(");
    sb.append("wd.findElement(@locator).getCssValue(");
    sb.append(propertyName);
    sb.append("), ");
    sb.append(value);
    sb.append(");\n");
    return Rythm.render(sb.toString(), locator.transform(),value.transform(),propertyName.transform());
  }

  public String getAction() {
    return "assertElementStyle";
  }

}
