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
public class StoreCookiePresent implements IAction{

  private Value variable, name;
  
  public StoreCookiePresent(Value variable, Value name) {
    this.variable = variable;
    this.name = name;
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
