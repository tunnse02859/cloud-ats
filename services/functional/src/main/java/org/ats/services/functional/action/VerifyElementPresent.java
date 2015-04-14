/**
 * 
 */
package org.ats.services.functional.action;

import java.io.IOException;

import org.ats.services.functional.locator.ILocator;
import org.rythmengine.Rythm;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Apr 13, 2015
 */
public class VerifyElementPresent implements IAction {
  
  private ILocator locator;
  
  private boolean negated;
  
  public VerifyElementPresent(ILocator locator, boolean negated) {
    this.locator = locator;
    this.negated = negated;
  }

  public String transform() throws IOException {
    StringBuilder sb = new StringBuilder("if (").append(negated ? "" : "!");
    sb.append("(wd.findElements(@locator).size() == 0)) {\n");
    sb.append("System.out.println(\"").append(negated ? "!" : "").append("verifyElementPresent failed\");\n");
    sb.append("}\n");
    return Rythm.render(sb.toString(), locator.transform());
  }

  public String getAction() {
    return "verifyElementPresent";
  }

}
