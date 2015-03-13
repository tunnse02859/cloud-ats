/**
 * 
 */
package org.ats.services.organization.entity.reference;

import org.ats.services.data.common.Reference;
import org.ats.services.organization.RoleService;
import org.ats.services.organization.entity.Role;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Mar 13, 2015
 */
public class RoleReference extends Reference<Role> {
  
  /** .*/
  private RoleService service;
  
  @Inject
  RoleReference(RoleService service, @Assisted String id) {
    super(id);
    this.service = service;
  }

  @Override
  public Role get() {
    return service.get(id);
  }

}
