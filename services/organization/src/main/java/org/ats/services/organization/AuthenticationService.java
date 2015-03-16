/**
 * 
 */
package org.ats.services.organization;

import org.ats.services.organization.entity.User;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Mar 16, 2015
 */
public interface AuthenticationService {

  public OrganizationContext logIn(String username, String password);
  
  public User logOut();
}
