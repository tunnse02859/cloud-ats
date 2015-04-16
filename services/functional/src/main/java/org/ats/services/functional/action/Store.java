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
public class Store implements IAction{

  private Value text, variable;
  
  public Store(Value text, Value variable) {
    this.text = text;
    this.variable = variable;
  }
  public String transform() throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  public String getAction() {
    // TODO Auto-generated method stub
    return null;
  }

}
