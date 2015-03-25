/**
 * 
 */
package org.ats.services.organization;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.ats.common.MD5;
import org.ats.services.OrganizationContext;
import org.ats.services.organization.base.AuthenticationService;
import org.ats.services.organization.entity.User;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Mar 16, 2015
 */
@Singleton
public class MongoAuthenticationService extends AuthenticationService<User> {
  
  /** .*/
  @Inject
  private UserService service;
  
  /** .*/
  @Inject
  private OrganizationContext context;
  
  /** .*/
  @Inject
  private Logger logger;
  
  /** .*/
  private final Map<String, User> userAuthenticated = new HashMap<String, User>();
  
  @Override
  public String logIn(String username, String password) {
    User user = service.get(username);
    if (user == null) {
      logger.info("The user " + username + " does not exist");
      return null;
    }
    if (password.equals(user.getPassword())) {
      context.setUser(user);
      context.setTenant(user.getTanent().get());
      logger.info("The user: " + username + " has logged successfully");
      String token = createToken(user);
      userAuthenticated.put(token, user);
      return token;
    }
    logger.info("Login failed to user: " + username);
    return null;
  }

  @Override
  public User logOut() {
    User user = context.getUser();
    context.setUser(null);
    context.setSpace(null);
    context.setTenant(null);
    userAuthenticated.remove(MD5.digest(user.getEmail()).toString());
    return user;
  }
  
  @Override
  public String createToken(User user) {
    return MD5.digest(user.getEmail()).toString();
  }

  @Override
  public User findByAuthToken(String token) {
    return userAuthenticated.get(token);
  }
}
