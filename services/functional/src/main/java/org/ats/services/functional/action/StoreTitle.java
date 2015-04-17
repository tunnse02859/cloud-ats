/**
 * 
 */
package org.ats.services.functional.action;

import java.io.IOException;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Apr 16, 2015
 */
public class StoreTitle implements IAction {
  
  private String variable;

  public StoreTitle(String variable) {
    this.variable = variable;
  }
  
  @Override
  public String transform() throws IOException {
    return "String " + variable + " = wd.getTitle();\n";
  }

  @Override
  public String getAction() {
    return "testStoreTitle";
  }

}
