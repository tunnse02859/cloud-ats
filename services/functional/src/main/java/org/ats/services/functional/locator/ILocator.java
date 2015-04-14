/**
 * 
 */
package org.ats.services.functional.locator;

import org.ats.services.functional.ITemplate;
import org.ats.services.functional.Value;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Apr 8, 2015
 */
public interface ILocator extends ITemplate {
  
  public Value getLocator();

  public String getName();
}
