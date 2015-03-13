/**
 * 
 */
package org.ats.services.organization.entity.fatory;

import org.ats.services.organization.entity.reference.TenantReference;

import com.google.inject.assistedinject.Assisted;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Mar 13, 2015
 */
public interface TenantReferenceFactory {

  public TenantReference create(@Assisted("id") String id);
  
}
