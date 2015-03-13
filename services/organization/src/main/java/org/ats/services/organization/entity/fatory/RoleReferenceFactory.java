/**
 * 
 */
package org.ats.services.organization.entity.fatory;

import org.ats.services.organization.entity.reference.RoleReference;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Mar 13, 2015
 */
public interface RoleReferenceFactory {

  public RoleReference create(String id);
  
}
