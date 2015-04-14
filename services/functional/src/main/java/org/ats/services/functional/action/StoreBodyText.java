/**
 * 
 */
package org.ats.services.functional.action;

import java.io.IOException;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Apr 13, 2015
 */
public class StoreBodyText implements IAction {

  private String name;
  
  public StoreBodyText(String name) {
    this.name = name;
  }
  
  public String transform() throws IOException {
    StringBuilder sb = new StringBuilder("String ").append(name).append(" = wd.findElement(By.tagName(\"html\")).getText();\n");
    return sb.toString();
  }

  public String getAction() {
    return "storeBodyText";
  }

}
