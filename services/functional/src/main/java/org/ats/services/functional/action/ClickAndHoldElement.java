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
 * Apr 10, 2015
 */
public class ClickAndHoldElement implements IAction {
  
  private ILocator locator;
  
  public ClickAndHoldElement(ILocator locator) {
    this.locator = locator;
  }

  public String transform() throws IOException {
    String template = "new Actions(wd).clickAndHold(wd.findElement(@locator)).build.perform();\n";
    return Rythm.render(template, locator.transform());
  }

  public String getAction() {
    return "clickAndHoldElement";
  }

}
