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
public class LinkTextLocator extends AbstractLocator {

  public LinkTextLocator(Value locator) {
    super(locator);
  }

  public String getName() {
    return "link text";
  }

  @Override
  public String transform() throws IOException {
    return Rythm.render("By.linkText(@locator)", locator.transform());
  }

}
