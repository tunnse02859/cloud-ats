/**
 * 
 */
package org.ats.services.organization.entity.reference;

import org.ats.services.data.common.Reference;
import org.ats.services.organization.TenantService;
import org.ats.services.organization.entity.Tenant;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Mar 13, 2015
 */
public class TenantReference extends Reference<Tenant> {

  /** .*/
  private TenantService service;

  @Inject
  TenantReference(TenantService service, @Assisted("id") String id) {
    super(id);
    this.service = service;
  }

  @Override
  public Tenant get() {
    return service.get(id);
  }

  @Override
  public Tenant get(String... mixins) {
    return service.get(id, mixins);
  }
}
