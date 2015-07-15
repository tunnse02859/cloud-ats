/**
 * 
 */
package org.ats.services.keyword.action;

import java.io.IOException;

import org.ats.services.keyword.locator.AbstractLocator;
import org.rythmengine.Rythm;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Apr 8, 2015
 */
@SuppressWarnings("serial")
public class ClickElement extends AbstractAction {
  
  private AbstractLocator locator;
  
  public ClickElement(AbstractLocator locator) {
    this.locator = locator;
  }

  public String transform() throws IOException {
    return Rythm.render("wd.findElement(@locator).click();\n", locator.transform());
  }

  public String getAction() {
    return "clickElement";
  }

}
