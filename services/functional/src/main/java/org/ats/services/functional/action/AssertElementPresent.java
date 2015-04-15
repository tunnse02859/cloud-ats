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
public class AssertElementPresent implements IAction {
  
  private ILocator locator;
  
  private boolean negated;
  
  public AssertElementPresent(ILocator locator, boolean negated) {
    this.locator = locator;
    this.negated = negated;
  }

  public String transform() throws IOException {
    StringBuilder sb = new StringBuilder(negated ? "assertFalse((" : "assertTrue((");
    sb.append("wd.findElements(@locator).size() != 0));\n");
    return Rythm.render(sb.toString(), locator.transform());
  }

  public String getAction() {
    return "assertElementPresent";
  }
}
