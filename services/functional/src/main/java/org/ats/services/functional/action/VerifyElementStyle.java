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
public class VerifyElementStyle implements IAction{

  private Value propertyName, value;
  
  private ILocator locator;
  
  private boolean negated;
  
  public VerifyElementStyle(Value propertyName,Value value, ILocator locator, boolean negated) {
    this.propertyName = propertyName;
    this.value = value;
    this.locator = locator;
    this.negated = negated;
  }
  
  public String transform() throws IOException {
    StringBuilder sb = new StringBuilder();
    sb.append("if (!wd.findElement(@locator).getCssValue(");
    sb.append(propertyName);
    sb.append(").equals(");
    sb.append(value);
    sb.append(")) {\nSystem.out.println(\"verifyElementStyle failed\");\n}\n");
    return Rythm.render(sb.toString(), locator.transform());
  }

  @Override
  public String getAction() {
    return "verifyElementStyle";
  }

}
