/**
 * 
 */
package org.ats.services.iaas.azure;

import org.ats.services.OrganizationServiceModule;
import org.ats.services.data.DatabaseModule;
import org.ats.services.data.MongoDBService;
import org.ats.services.event.EventModule;
import org.ats.services.event.EventService;
import org.ats.services.iaas.IaaSService;
import org.ats.services.iaas.IaaSServiceProvider;
import org.ats.services.iaas.VMachineServiceModule;
import org.ats.services.iaas.exception.CreateVMException;
import org.ats.services.organization.entity.Tenant;
import org.ats.services.organization.entity.reference.TenantReference;
import org.ats.services.organization.event.AbstractEventTestCase;
import org.ats.services.vmachine.VMachine;
import org.ats.services.vmachine.VMachineService;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.inject.Guice;


/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Feb 15, 2016
 */
public class AzureTestCase extends AbstractEventTestCase {
  
  private VMachineService vmachineService;
  
  private IaaSServiceProvider iaasProvider;
  
  private IaaSService azureService;
  
  @BeforeClass
  public void init() throws Exception {
    
    System.setProperty("jenkins.slave.credential", "57ee2290-2c8e-49de-b750-0ca1a9488dac");
    System.setProperty(EventModule.EVENT_CONF, "src/test/resources/event.conf");
    
    VMachineServiceModule vmModule = new VMachineServiceModule("src/test/resources/iaas.conf");
    vmModule.setProperty("org.ats.cloud.iaas", "org.ats.services.iaas.AzureService");
    
    this.injector = Guice.createInjector(
        new DatabaseModule(), 
        new EventModule(),
        new OrganizationServiceModule(),
        vmModule);
    
    this.vmachineService = injector.getInstance(VMachineService.class);
    this.iaasProvider = injector.getInstance(IaaSServiceProvider.class);
    this.azureService = this.iaasProvider.get();
    
    this.mongoService = injector.getInstance(MongoDBService.class);
    this.mongoService.dropDatabase();

    //start event service
    this.eventService = injector.getInstance(EventService.class);
    this.eventService.setInjector(injector);
    this.eventService.start();

    initService();
    
    Tenant fsoft = tenantFactory.create("fsoft-testonly");
    tenantService.create(fsoft);
    
    TenantReference tenantRef = tenantRefFactory.create("fsoft-testonly");
    azureService.initTenant(tenantRef);
  }
  
  @AfterClass
  public void shutdown() throws Exception {
    TenantReference tenantRef = tenantRefFactory.create("fsoft-testonly");
    azureService.destroyTenant(tenantRef);
    
    this.eventService.stop();
    this.mongoService.dropDatabase();
  }
  
  @Test
  public void testInit() {
    TenantReference tenantRef = tenantRefFactory.create("fsoft-testonly");
    VMachine vm = vmachineService.getSystemVM(tenantRef, null);
    Assert.assertNotNull(vm);
    Assert.assertTrue(vm.getPrivateIp().startsWith("172.16.1"));
    Assert.assertNotNull(vm.getPublicIp());
  }
  
  @Test
  public void testCreateNonUI() throws CreateVMException {
    TenantReference tenantRef = tenantRefFactory.create("fsoft-testonly");
    VMachine vm = azureService.createTestVM(tenantRef, null, false, false);
    Assert.assertNotNull(vm);
    Assert.assertTrue(vm.getPrivateIp().startsWith("172.16.1"));
    Assert.assertNotNull(vm.getPublicIp());
  }
  
  @Test
  public void testCreateUI() throws CreateVMException {
    TenantReference tenantRef = tenantRefFactory.create("fsoft-testonly");
    VMachine vm = azureService.createTestVM(tenantRef, null, true, false);
    Assert.assertNotNull(vm);
    Assert.assertTrue(vm.getPrivateIp().startsWith("172.16.1"));
    Assert.assertNotNull(vm.getPublicIp());
  }
  
  @Test
  public void testCreateNonUIAsync() throws Exception {
    TenantReference tenantRef = tenantRefFactory.create("fsoft-testonly");
    VMachine vm = azureService.createTestVMAsync(tenantRef, null, false, false);
    Assert.assertNotNull(vm);
    Assert.assertEquals(vm.getStatus(), VMachine.Status.Initializing);
    while (isVMNotStarted(vm.getId())) {
      Thread.sleep(5000);
    }
    Assert.assertTrue(vm.getPrivateIp().startsWith("172.16.1"));
    Assert.assertNotNull(vm.getPublicIp());
    Assert.assertEquals(vm.getStatus(), VMachine.Status.Started);
  }
  
  @Test
  public void testCreateUIAsync() throws Exception {
    TenantReference tenantRef = tenantRefFactory.create("fsoft-testonly");
    VMachine vm = azureService.createTestVMAsync(tenantRef, null, true, false);
    Assert.assertNotNull(vm);
    Assert.assertEquals(vm.getStatus(), VMachine.Status.Initializing);
    while (isVMNotStarted(vm.getId())) {
      Thread.sleep(5000);
    }
    Assert.assertTrue(vm.getPrivateIp().startsWith("172.16.1"));
    Assert.assertNotNull(vm.getPublicIp());
    Assert.assertEquals(vm.getStatus(), VMachine.Status.Started);
  }
  
  private boolean isVMNotStarted(String vmId) throws InterruptedException {
    VMachine vm = vmachineService.get(vmId);
    if (vm.getStatus() == VMachine.Status.Started) return false;
    return true;
  }
}
