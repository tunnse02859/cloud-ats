/**
 * 
 */
package org.ats.services.functional.action;

import java.io.IOException;

import org.ats.services.functional.Value;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Apr 16, 2015
 */
public class StoreCookieByName implements IAction {

  private Value name;
  
  private String variable;
  
  public StoreCookieByName(Value name, String variable) {
    this.name = name;
    this.variable = variable;
  }
  
  @Override
  public String transform() throws IOException {
    return null;
  }

  @Override
  public String getAction() {
    return "storeCookieByName";
  }

}
