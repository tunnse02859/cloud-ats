/**
 * 
 */
package org.ats.services.iaas.openstack;

import org.ats.services.OrganizationServiceModule;
import org.ats.services.VMachineServiceModule;
import org.ats.services.data.DatabaseModule;
import org.ats.services.data.MongoDBService;
import org.ats.services.event.EventModule;
import org.ats.services.event.EventService;
import org.ats.services.iaas.CreateVMException;
import org.ats.services.organization.entity.Tenant;
import org.ats.services.organization.entity.reference.TenantReference;
import org.ats.services.organization.event.AbstractEventTestCase;
import org.ats.services.vmachine.VMachine;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.inject.Guice;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Jul 17, 2015
 */
public class InitTenant extends AbstractEventTestCase {

  private OpenStackService openstackService;
  
  @BeforeClass
  public void init() throws Exception {
    this.injector = Guice.createInjector(
        new DatabaseModule(), 
        new EventModule(),
        new OrganizationServiceModule(),
        new VMachineServiceModule("src/test/resources/iaas.conf"));
    
    this.openstackService = injector.getInstance(OpenStackService.class);
    this.mongoService = injector.getInstance(MongoDBService.class);

    //start event service
    this.eventService = injector.getInstance(EventService.class);
    this.eventService.setInjector(injector);
    this.eventService.start();

    initService();
    this.openstackService.addCredential("admin", "admin",  "ADMIN_PASS");
    
    Tenant fsoft = tenantFactory.create("fsoft-testonly");
    tenantService.create(fsoft);
    
    TenantReference tenantRef = tenantRefFactory.create("fsoft-testonly");
    openstackService.initTenant(tenantRef);
  }
  
  @Test
  public void testInit() throws CreateVMException {
    TenantReference tenantRef = tenantRefFactory.create("fsoft-testonly");
    VMachine vm = openstackService.createTestVM(tenantRef, null, false);
    Assert.assertEquals(vm.getStatus(), VMachine.Status.Started);
    Assert.assertFalse(vm.isSystem());
    Assert.assertFalse(vm.hasUI());
    Assert.assertNull(vm.getPublicIp());
    
    vm = openstackService.createTestVM(tenantRef, null, true);
    Assert.assertEquals(vm.getStatus(), VMachine.Status.Started);
    Assert.assertFalse(vm.isSystem());
    Assert.assertTrue(vm.hasUI());
    Assert.assertNull(vm.getPublicIp());
  }
  
  @AfterClass
  public void shutdown() throws Exception {
    this.eventService.stop();
  }
}
