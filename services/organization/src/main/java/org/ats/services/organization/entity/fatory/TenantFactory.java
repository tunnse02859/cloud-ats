/**
 * 
 */
package org.ats.services.organization.entity.fatory;

import org.ats.services.organization.entity.Tenant;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Mar 13, 2015
 */
public interface TenantFactory {

  public Tenant create(String name);
}
