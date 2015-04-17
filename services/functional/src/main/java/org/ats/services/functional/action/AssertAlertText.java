/**
 * 
 */
package org.ats.services.functional.action;

import java.io.IOException;

import org.ats.services.functional.Value;

/**
 * @author NamBV2
 *
 * Apr 17, 2015
 */
public class AssertAlertText implements IAction{

  private Value text;
  
  public AssertAlertText(Value text) {
    this.text = text;
  }
  
  public String transform() throws IOException {
    return null;
  }

  public String getAction() {
    return null;
  }

}
