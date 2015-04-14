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
 * Apr 9, 2015
 */
public class SetElementSelected implements IAction {

  private ILocator locator;
  
  public SetElementSelected(ILocator locator) {
    this.locator = locator;
  }
  
  public String transform() throws IOException {
    String template = "if (!wd.findElement(@locator).isSelected()) {\n"
        + "wd.findElement(@locator).click();\n"
        + "}\n";
    return Rythm.render(template, locator.transform());
  }

  public String getAction() {
    return "setElementSelected";
  }
}
