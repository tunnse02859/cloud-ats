/**
 * 
 */
package org.ats.services.organization;

import com.google.inject.AbstractModule;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Mar 9, 2015
 */
public class OrganizationServiceModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(UserService.class);
    bind(TenantService.class);
  }

}
