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
 * Apr 9, 2015
 */
public class NameLocator extends AbstractLocator {

  public NameLocator(Value locator) {
    super(locator);
  }

  public String getName() {
    return "name";
  }

  @Override
  public String transform() throws IOException {
    return Rythm.render("By.name(@locator)", locator.transform());
  }

}
