/**
 * 
 */
package org.ats.services.functional.locator;

import java.io.IOException;

import org.ats.services.functional.Value;
import org.rythmengine.Rythm;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Apr 8, 2015
 */
@SuppressWarnings("serial")
public class ClassNameLocator extends AbstractLocator {

  public ClassNameLocator(Value locator) {
    super(locator);
  }

  public String transform() throws IOException {
    return Rythm.render("By.className(@locator)", locator.transform());
  }

  public String getName() {
    return "class name";
  }
  
}
