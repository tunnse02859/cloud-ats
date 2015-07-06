/**
 * 
 */
package org.ats.services.vmachine;

import org.ats.services.organization.entity.reference.SpaceReference;
import org.ats.services.organization.entity.reference.TenantReference;
import org.ats.services.vmachine.VMachine.Status;

import com.google.inject.assistedinject.Assisted;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Jul 2, 2015
 */
public interface VMachineFactory {
  
  public VMachine create(@Assisted("_id") String id,
      @Assisted("tenant") TenantReference tenant, 
      @Assisted("space") SpaceReference space, 
      @Assisted("isSystem") boolean isSystem, 
      @Assisted("hasUI") boolean hasUI,
      @Assisted("public_ip") String publicIp,
      @Assisted("private_ip") String privateIp,
      @Assisted("status") Status status);
}
