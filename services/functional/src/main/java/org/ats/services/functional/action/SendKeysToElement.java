/**
 * 
 */
package org.ats.services.functional.action;

import java.io.IOException;

import org.ats.services.functional.Value;
import org.ats.services.functional.locator.ILocator;
import org.rythmengine.Rythm;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Apr 9, 2015
 */
public class SendKeysToElement implements IAction {

  private ILocator locator;
  
  private Value text;
  
  public SendKeysToElement(ILocator locator, Value text) {
    this.locator = locator;
    this.text = text;
  }
  
  public String transform() throws IOException {
    String template = "wd.findElement(@locator).click();\n"
        + "wd.findElement(@locator).sendKeys(@text);\n";
    return Rythm.render(template, locator.transform(), text.transform());
  }

  public String getAction() {
    return "sendKeysToElement";
  }

}
