/**
 * 
 */
package org.ats.services.functional.action;

import java.io.IOException;

import org.ats.services.functional.Value;

/**
 * @author TrinhTV3
 *
 * Email: TrinhTV3@fsoft.com.vn
 */
@SuppressWarnings("serial")
public class AssertTitle extends AbstractAction{

  private boolean negated;
  
  private Value title;
  
  public AssertTitle(Value title, boolean negated) {
    this.title = title;
    this.negated = negated;
  }
  
  public String transform() throws IOException {
    StringBuilder sb = new StringBuilder(negated ? "assertNotEquals(" : "assertEquals(");
    sb.append("wd.getTitle(), ").append(title).append(");\n");
    return sb.toString();
  }

  public String getAction() {
    return "assertTitle";
  }

}
