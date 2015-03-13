/**
 * 
 */
package org.ats.services.organization.entity.fatory;

import org.ats.services.organization.entity.User;

import com.google.inject.assistedinject.Assisted;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Mar 13, 2015
 */
public interface UserFactory {

  public User create(@Assisted("email") String email, @Assisted("firstName") String firstName,  @Assisted("lastName") String lastName);
}
