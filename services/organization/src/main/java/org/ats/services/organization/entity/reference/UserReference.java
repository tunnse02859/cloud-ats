/**
 * 
 */
package org.ats.services.organization.entity.reference;

import org.ats.services.data.common.Reference;
import org.ats.services.organization.UserService;
import org.ats.services.organization.entity.User;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Mar 13, 2015
 */
public class UserReference extends Reference<User> {

  /** .*/
  private UserService service;

  @Inject
  UserReference(UserService service, @Assisted String id) {
    super(id);
    this.service = service;
  }
  
  @Override
  public User get() {
    return service.get(id);
  }
}
