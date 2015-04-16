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
public class SwitchToFrame implements IAction {

  private Value identifier;
  
  /**
   * 
   */
  public SwitchToFrame(Value identifier) {
    this.identifier = identifier;
  }
  public String transform() throws IOException {
    return null;
  }

  public String getAction() {
    return null;
  }

}
