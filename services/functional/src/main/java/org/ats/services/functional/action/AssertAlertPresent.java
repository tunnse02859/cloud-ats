/**
 * 
 */
package org.ats.services.functional.action;

import java.io.IOException;

/**
 * @author NamBV2
 *
 * Apr 17, 2015
 */
public class AssertAlertPresent implements IAction{

  private boolean nageted;
  
  public AssertAlertPresent(boolean negated) {
    this.nageted = negated;
  }
  
  public String transform() throws IOException {
    return null;
  }

  public String getAction() {
    return null;
  }

}
