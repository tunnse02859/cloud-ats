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
public class StoreAlertText implements IAction{

  private Value variable;
  
  public StoreAlertText(Value variable) {
    this.variable = variable;
  }
  
  public String transform() throws IOException {
    return null;
  }

  @Override
  public String getAction() {
    return null;
  }

}
