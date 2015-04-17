/**
 * 
 */
package org.ats.services.functional.action;

import java.io.IOException;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Apr 14, 2015
 */
public class StoreCurrentUrl implements IAction {

  private String variable;
  
  public StoreCurrentUrl(String variable) {
    this.variable = variable;
  }
  
  public String transform() throws IOException {
    StringBuilder sb = new StringBuilder("String ").append(variable).append(" = wd.getCurrentUrl();\n");
    return sb.toString();
  }

  public String getAction() {
    return "storeCurrentUrl";
  }
}
