/**
 * 
 */
package org.ats.services;

import org.ats.services.organization.entity.Space;
import org.ats.services.organization.entity.Tenant;
import org.ats.services.organization.entity.User;

import com.google.inject.Singleton;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Mar 16, 2015
 */
@Singleton
public class OrganizationContext {

  private static final ThreadLocal<Tenant> currentTenant = new ThreadLocal<Tenant>();
  
  private static final ThreadLocal<Space> currentSpace = new ThreadLocal<Space>();
  
  private static final ThreadLocal<User> currentUser = new ThreadLocal<User>();
  
  public void setTenant(Tenant tenant) {
    currentTenant.set(tenant);
  }
  
  public Tenant getTenant() {
    return currentTenant.get();
  }
  
  public void setSpace(Space space) {
    currentSpace.set(space);
  }
  
  public Space getSpace() {
    return currentSpace.get();
  }
  
  public void setUser(User user) {
    currentUser.set(user);
  }
  
  public User getUser() {
    return currentUser.get();
  }
}
