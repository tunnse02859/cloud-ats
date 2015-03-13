/**
 * 
 */
package org.ats.services.organization.entity.reference;

import org.ats.services.data.common.Reference;
import org.ats.services.organization.FeatureService;
import org.ats.services.organization.entity.Feature;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Mar 13, 2015
 */
public class FeatureReference extends Reference<Feature>{

  /** .*/
  private FeatureService service;
  
  @Inject
  FeatureReference(FeatureService service, @Assisted("id") String id) {
    super(id);
    this.service = service;
  }

  @Override
  public Feature get() {
    return service.get(id);
  }

}
