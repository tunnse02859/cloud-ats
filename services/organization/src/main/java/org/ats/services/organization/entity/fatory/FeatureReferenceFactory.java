/**
 * 
 */
package org.ats.services.organization.entity.fatory;

import org.ats.services.organization.entity.reference.FeatureReference;

import com.google.inject.assistedinject.Assisted;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Mar 13, 2015
 */
public interface FeatureReferenceFactory {

  public FeatureReference create(@Assisted("id") String id);
  
}
