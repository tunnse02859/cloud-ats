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

  private String variable;
  
  public StoreAlertText(String variable) {
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
