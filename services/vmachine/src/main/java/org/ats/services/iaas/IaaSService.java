/**
 * 
 */
package org.ats.services.iaas;

import org.ats.services.iaas.exception.CreateVMException;
import org.ats.services.iaas.exception.DestroyTenantException;
import org.ats.services.iaas.exception.DestroyVMException;
import org.ats.services.iaas.exception.InitializeTenantException;
import org.ats.services.iaas.exception.RebuildVMException;
import org.ats.services.iaas.exception.StartVMException;
import org.ats.services.iaas.exception.StopVMException;
import org.ats.services.organization.entity.reference.SpaceReference;
import org.ats.services.organization.entity.reference.TenantReference;
import org.ats.services.vmachine.VMachine;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Jul 2, 2015
 */
public interface IaaSService {
  
  public void initTenant(TenantReference tenant) throws InitializeTenantException, CreateVMException;
  
  public void destroyTenant(TenantReference tenant) throws DestroyTenantException, DestroyVMException;

  public VMachine createSystemVM(TenantReference tenant, SpaceReference space) throws CreateVMException;
  
  public VMachine createTestVM(TenantReference tenant, SpaceReference space, boolean hasUI) throws CreateVMException;
  
  public VMachine start(VMachine machine) throws StartVMException;
  
  public VMachine stop(VMachine machine) throws StopVMException;
  
  public VMachine rebuild(VMachine machine) throws RebuildVMException;
  
  public void destroy(VMachine machine) throws DestroyVMException;
  
  public VMachine allocateFloatingIp(VMachine vm);
  
  public VMachine deallocateFloatingIp(VMachine vm);
  
  public void addCredential(String tenant, String username, String password);
  
  public void addCredential(String tenant);
}
