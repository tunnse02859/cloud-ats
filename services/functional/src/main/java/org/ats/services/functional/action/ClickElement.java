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
 * Apr 8, 2015
 */
public class ClickElement implements IAction {
  
  private ILocator locator;
  
  public ClickElement(ILocator locator) {
    this.locator = locator;
  }

  public String transform() throws IOException {
    return Rythm.render("wd.findElement(@locator).click();\n", locator.transform());
  }

  public String getAction() {
    return "clickElement";
  }

}
