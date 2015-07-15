/**
 * 
 */
package org.ats.services.keyword.action;

import java.io.IOException;

import org.ats.services.keyword.Value;
import org.ats.services.keyword.locator.AbstractLocator;
import org.rythmengine.Rythm;

/**
 * @author NamBV2
 *
 * Apr 17, 2015
 */
@SuppressWarnings("serial")
public class AssertElementStyle extends AbstractAction{

  private Value propertyName, value;
  
  private AbstractLocator locator;
  
  private boolean negated;
  
  public AssertElementStyle(Value propertyName,Value value, AbstractLocator locator, boolean negated) {
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
