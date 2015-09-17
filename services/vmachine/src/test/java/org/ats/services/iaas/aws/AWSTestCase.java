/**
 * 
 */
package org.ats.services.iaas.aws;

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
 * Sep 7, 2015
 */
public class AWSTestCase  extends AbstractEventTestCase {
  
  private VMachineService vmachineService;
  
  private IaaSServiceProvider iaasProvider;
  
  private IaaSService awsService;
  
  @BeforeClass
  public void init() throws Exception {
    
    System.setProperty("jenkins.slave.credential", "b7cf38a7-5b1c-412b-9280-07cad8c952bb");
    
    VMachineServiceModule vmModule = new VMachineServiceModule("src/test/resources/iaas.conf");
    vmModule.setProperty("org.ats.cloud.iaas", "org.ats.services.iaas.AWSService");
    vmModule.setProperty("org.ats.cloud.aws.secret", System.getProperty("aws.secret"));
    vmModule.setProperty("org.ats.cloud.aws.access", System.getProperty("aws.access"));
    
    this.injector = Guice.createInjector(
        new DatabaseModule(), 
        new EventModule(),
        new OrganizationServiceModule(),
        vmModule);
    
    this.vmachineService = injector.getInstance(VMachineService.class);
    this.iaasProvider = injector.getInstance(IaaSServiceProvider.class);
    this.awsService = this.iaasProvider.get();
    
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
    awsService.initTenant(tenantRef);
  }
  
  @AfterClass
  public void shutdown() throws Exception {
    TenantReference tenantRef = tenantRefFactory.create("fsoft-testonly");
    awsService.destroyTenant(tenantRef);
    
    this.eventService.stop();
    this.mongoService.dropDatabase();
  }
  
  @Test
  public void testInit() {
    TenantReference tenantRef = tenantRefFactory.create("fsoft-testonly");
    VMachine vm = vmachineService.getSystemVM(tenantRef, null);
    Assert.assertNotNull(vm);
    Assert.assertTrue(vm.getPrivateIp().startsWith("10.10.13"));
    Assert.assertNotNull(vm.getPublicIp());
  }
  
  @Test
  public void testCreateNonUI() throws CreateVMException {
    TenantReference tenantRef = tenantRefFactory.create("fsoft-testonly");
    VMachine vm = awsService.createTestVM(tenantRef, null, false);
    Assert.assertNotNull(vm);
    Assert.assertTrue(vm.getPrivateIp().startsWith("10.10.13"));
    Assert.assertNotNull(vm.getPublicIp());
  }
  
  @Test
  public void testCreateUI() throws CreateVMException {
    TenantReference tenantRef = tenantRefFactory.create("fsoft-testonly");
    VMachine vm = awsService.createTestVM(tenantRef, null, true);
    Assert.assertNotNull(vm);
    Assert.assertTrue(vm.getPrivateIp().startsWith("10.10.13"));
    Assert.assertNotNull(vm.getPublicIp());
  }
}
