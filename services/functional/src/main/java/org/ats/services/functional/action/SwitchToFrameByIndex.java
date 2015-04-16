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
public class SwitchToFrameByIndex implements IAction {

  private Value index;

  /**
   * 
   */
  public SwitchToFrameByIndex(Value index) {
    this.index = index;
  }
  public String transform() throws IOException {
    return null;
  }

  public String getAction() {
    return null;
  }
  
}
