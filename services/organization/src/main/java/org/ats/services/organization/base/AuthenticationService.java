/**
 * 
 */
package org.ats.services.organization.base;

import org.ats.services.organization.entity.User;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Mar 16, 2015
 */
public abstract class AuthenticationService<T> {
  
  /** .*/
  public static final String AUTH_TOKEN_HEADER = "X-AUTH-TOKEN";

  /** .*/
  public static final String SPACE_HEADER = "X-SPACE";
  
  /** .*/
  public static final String AUTH_TOKEN = "authToken";

  public abstract String logIn(String username, String password);
  
  public abstract User logOut();
  
  public abstract String createToken(T user);
  
  public abstract T findByAuthToken(String token);
}
