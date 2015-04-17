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
public class Print implements IAction {
  
  private Value text;
  
  public Print(Value text) {
    this.text = text;
  }

  @Override
  public String transform() throws IOException {
    return null;
  }

  @Override
  public String getAction() {
    return "print";
  }

}
