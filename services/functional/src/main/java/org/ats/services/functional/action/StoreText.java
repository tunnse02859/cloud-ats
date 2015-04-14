/**
 * 
 */
package org.ats.services.functional.action;

import java.io.IOException;

import org.ats.services.functional.locator.ILocator;
import org.rythmengine.Rythm;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Apr 13, 2015
 */
public class StoreText implements IAction {

  private String name;
  
  private ILocator locator;
  
  public StoreText(String name, ILocator locator) {
    this.name = name;
    this.locator = locator;
  }
  
  public String transform() throws IOException {
    StringBuilder sb = new StringBuilder("String ").append(name);
    sb.append(" = wd.findElement(@locator).getText();\n");
    return Rythm.render(sb.toString(), locator.transform());
  }

  public String getAction() {
    return "storeText";
  }

}
