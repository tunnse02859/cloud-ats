/**
 * 
 */
package org.ats.services.organization.entity.fatory;

import org.ats.services.organization.entity.Space;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Mar 13, 2015
 */
public interface SpaceFactory {

  public Space create(String name);
}
