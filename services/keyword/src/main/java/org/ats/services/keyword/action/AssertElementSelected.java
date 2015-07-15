/**
 * 
 */
package org.ats.services.keyword.action;

import java.io.IOException;

import org.ats.services.keyword.locator.AbstractLocator;
import org.rythmengine.Rythm;

/**
 * @author TrinhTV3
 *
 * Email: TrinhTV3@fsoft.com.vn
 */
@SuppressWarnings("serial")
public class AssertElementSelected extends AbstractAction{

  private boolean negated;
  
  private AbstractLocator locator;
  
  public AssertElementSelected(AbstractLocator locator, boolean negated) {
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
