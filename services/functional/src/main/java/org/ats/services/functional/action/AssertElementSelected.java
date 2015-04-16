/**
 * 
 */
package org.ats.services.functional.action;

import java.io.IOException;

import org.ats.services.functional.locator.ILocator;
import org.rythmengine.Rythm;

/**
 * @author TrinhTV3
 *
 * Email: TrinhTV3@fsoft.com.vn
 */
public class AssertElementSelected implements IAction{

  private boolean negated;
  
  private ILocator locator;
  
  public AssertElementSelected(ILocator locator, boolean negated) {
    this.locator = locator;
    this.negated = negated;
  }
  
  public String transform() throws IOException {
    StringBuilder sb = new StringBuilder(negated ? "assertFalse(" : "assertTrue(");
    sb.append("(wd.findElement(@locator).isSelected()));\n");
    return Rythm.render(sb.toString(), locator.transform());
  }

  public String getAction() {
    return "assertElementSelected";
  }

}
