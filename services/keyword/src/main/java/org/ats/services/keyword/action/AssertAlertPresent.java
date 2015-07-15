/**
 * 
 */
package org.ats.services.keyword.action;

import java.io.IOException;

/**
 * @author NamBV2
 *
 * Apr 17, 2015
 */
@SuppressWarnings("serial")
public class AssertAlertPresent extends AbstractAction {

  private boolean negated;
  
  public AssertAlertPresent(boolean negated) {
    this.negated = negated;
  }
  
  public String transform() throws IOException {
    StringBuilder sb = new StringBuilder(negated ? "assertFalse(" : "assertTrue(");
    sb.append("isAlertPresent(wd));\n");
    return sb.toString();
  }

  public String getAction() {
    return "assertAlertPresent";
  }

}
