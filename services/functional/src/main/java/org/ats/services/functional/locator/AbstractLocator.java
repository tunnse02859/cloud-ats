/**
 * 
 */
package org.ats.services.functional.locator;

import java.io.IOException;

import org.ats.services.functional.Value;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Apr 8, 2015
 */
public abstract class AbstractLocator implements ILocator {

  protected Value locator;
  
  AbstractLocator(Value locator) {
    this.locator = locator;
  }
  
  public abstract String transform() throws IOException;

  public Value getLocator()  {
    return locator;
  }
}
