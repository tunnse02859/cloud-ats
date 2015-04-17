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
public class AnswerAlert implements IAction {

  private Value text;
  
  /**
   * 
   */
  public AnswerAlert(Value text) {
    this.text = text;
    
  }
  @Override
  public String transform() throws IOException {
    return null;
  }

  @Override
  public String getAction() {
    return null;
  }

}
