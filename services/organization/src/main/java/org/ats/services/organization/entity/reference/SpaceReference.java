/**
 * 
 */
package org.ats.services.organization.entity.reference;

import org.ats.services.data.common.Reference;
import org.ats.services.organization.SpaceService;
import org.ats.services.organization.entity.Space;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Mar 13, 2015
 */
public class SpaceReference extends Reference<Space> {
  
  /** .*/
  private SpaceService service;

  @Inject
  SpaceReference(SpaceService service, @Assisted("id") String id) {
    super(id);
    this.service = service;
  }

  @Override
  public Space get() {
    return service.get(id);
  }

}
