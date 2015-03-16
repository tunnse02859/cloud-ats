/**
 * 
 */
package org.ats.services.organization;

import java.util.logging.Logger;

import org.ats.services.OrganizationContext;
import org.ats.services.organization.base.AuthenticationService;
import org.ats.services.organization.entity.User;

import com.google.inject.Inject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Mar 16, 2015
 */
public class MongoAuthenticationService implements AuthenticationService {
  
  private UserService service;
  
  private OrganizationContext context;
  
  private Logger logger;
  
  @Inject
  MongoAuthenticationService(UserService service, OrganizationContext context, Logger logger) {
    this.service = service;
    this.context = context;
    this.logger = logger;
  }

  public OrganizationContext logIn(String username, String password) {
    User user = service.get(username);
    if (user == null) {
      logger.info("The user " + username + " does not exist");
      return null;
    }
    if (password.equals(user.getPassword())) {
      context.setUser(user);
      context.setTenant(user.getTanent().get());
      logger.info("The user: " + username + " has logged successfully");
      return context;
    }
    logger.info("Login failed to user: " + username);
    return null;
  }

  public User logOut() {
    User user = context.getUser();
    context.setUser(null);
    context.setSpace(null);
    context.setTenant(null);
    return user;
  }

}
