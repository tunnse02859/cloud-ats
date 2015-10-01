/**
 * 
 */
package org.ats.services.iaas.openstack;

import org.ats.jenkins.JenkinsMaster;
import org.ats.jenkins.JenkinsMavenJob;
import org.ats.services.OrganizationServiceModule;
import org.ats.services.data.DatabaseModule;
import org.ats.services.data.MongoDBService;
import org.ats.services.event.EventModule;
import org.ats.services.event.EventService;
import org.ats.services.iaas.IaaSService;
import org.ats.services.iaas.IaaSServiceProvider;
import org.ats.services.iaas.VMachineServiceModule;
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
 * Jul 6, 2015
 */
public class OpenStackServiceTestCase extends AbstractEventTestCase {

  private IaaSService openstackService;
  
  private IaaSServiceProvider iaasProvider;
  
  private VMachineService vmachineService;
  
  @BeforeClass
  public void init() throws Exception {
    
    System.setProperty("jenkins.slave.credential", "965a0c50-868c-48b1-8f3e-b0179bf40666");
    System.setProperty(EventModule.EVENT_CONF, "src/test/resources/event.conf");
    
    VMachineServiceModule vmModule = new VMachineServiceModule("src/test/resources/iaas.conf");
    vmModule.setProperty("org.ats.cloud.iaas", "org.ats.services.iaas.OpenStackService");
    
    this.injector = Guice.createInjector(
        new DatabaseModule(), 
        new EventModule(),
        new OrganizationServiceModule(),
        vmModule);
    
    this.iaasProvider = injector.getInstance(IaaSServiceProvider.class);
    this.openstackService = iaasProvider.get();
    
    this.vmachineService = injector.getInstance(VMachineService.class);
    this.mongoService = injector.getInstance(MongoDBService.class);
    this.mongoService.dropDatabase();

    //start event service
    this.eventService = injector.getInstance(EventService.class);
    this.eventService.setInjector(injector);
    this.eventService.start();

    initService();
    openstackService.addCredential("admin", "admin",  "ADMIN_PASS");
    
    Tenant fsoft = tenantFactory.create("fsoft-testonly");
    tenantService.create(fsoft);
    
    TenantReference tenantRef = tenantRefFactory.create("fsoft-testonly");
    openstackService.initTenant(tenantRef);
  }
  
  @AfterClass
  public void shutdown() throws Exception {
    TenantReference tenantRef = tenantRefFactory.create("fsoft-testonly");
    openstackService.destroyTenant(tenantRef);
    
    this.eventService.stop();
    this.mongoService.dropDatabase();
  }
  
  @Test
  public void testInitAndDestroyTenant() throws Exception {
    TenantReference tenantRef = tenantRefFactory.create("fsoft-testonly");
    Tenant fsoft = tenantService.get(tenantRef.getId(), "tenant_id", "user_id", "network_id", "subnet_id", "router_id");
    
    Assert.assertNotNull(fsoft.get("tenant_id"));
    Assert.assertNotNull(fsoft.get("user_id"));
    Assert.assertNotNull(fsoft.get("network_id"));
    Assert.assertNotNull(fsoft.get("subnet_id"));
    Assert.assertNotNull(fsoft.get("router_id"));
  }
  
  @Test
  public void testVMAction() throws Exception {
    TenantReference tenantRef = tenantRefFactory.create("fsoft-testonly");
    
    VMachine vm = openstackService.createTestVM(tenantRef, null, false);
    openstackService.deallocateFloatingIp(vm);
    
    Assert.assertEquals(vm.getStatus(), VMachine.Status.Started);
    Assert.assertFalse(vm.isSystem());
    Assert.assertFalse(vm.hasUI());
    Assert.assertNull(vm.getPublicIp());
    
    vm = openstackService.stop(vm);
    Assert.assertEquals(vm.getStatus(), VMachine.Status.Stopped);
    
    vm = openstackService.start(vm);
    Assert.assertEquals(vm.getStatus(), VMachine.Status.Started);
    Assert.assertNull(vm.getPublicIp());
  }
  
  @Test
  public void testRunJenkinsJob() throws Exception {
    TenantReference tenantRef = tenantRefFactory.create("fsoft-testonly");
    
    VMachine jenkins = vmachineService.getSystemVM(tenantRef, null);
    
    VMachine vm = openstackService.createTestVM(tenantRef, null, true);
    openstackService.deallocateFloatingIp(vm);
    
    JenkinsMaster master = new JenkinsMaster(jenkins.getPublicIp(), "http", "jenkins", 8080);

    JenkinsMavenJob job = new JenkinsMavenJob(master, "demo-selenium", vm.getPrivateIp(), "/home/cloudats/projects/demo-selenium/pom.xml", "clean install");
    Assert.assertEquals(job.submit(), 1);

    int start = 0;
    int last = 0;
    byte[] bytes;

    while(job.isBuilding(1, System.currentTimeMillis(), 30*1000)) {
      bytes = job.getConsoleOutput(1, start);
      last = bytes.length;
      byte[] next = new byte[last - start];
      System.arraycopy(bytes, start, next, 0, next.length);
      start += (last - start);

      if (next.length > 0) {
        String output = new String(next);
        System.out.println(output.trim());
        if (output.indexOf("TestNG652Configurator") != -1) break; 
      }
    }
  }
  
  @Test
  public void testCreateNonUIAsync() throws Exception {
    TenantReference tenantRef = tenantRefFactory.create("fsoft-testonly");
    VMachine vm = openstackService.createTestVMAsync(tenantRef, null, false);
    Assert.assertNotNull(vm);
    Assert.assertEquals(vm.getStatus(), VMachine.Status.Initializing);
    while (isVMNotStarted(vm.getId())) {
      Thread.sleep(5000);
    }
    Assert.assertTrue(vm.getPrivateIp().startsWith("192.168.1"));
    Assert.assertNull(vm.getPublicIp());
    Assert.assertEquals(vm.getStatus(), VMachine.Status.Started);
  }
  
  @Test
  public void testCreateUIAsync() throws Exception {
    TenantReference tenantRef = tenantRefFactory.create("fsoft-testonly");
    VMachine vm = openstackService.createTestVMAsync(tenantRef, null, true);
    Assert.assertNotNull(vm);
    Assert.assertEquals(vm.getStatus(), VMachine.Status.Initializing);
    while (isVMNotStarted(vm.getId())) {
      Thread.sleep(5000);
    }
    Assert.assertTrue(vm.getPrivateIp().startsWith("192.168.1"));
    Assert.assertNotNull(vm.getPublicIp());
    Assert.assertEquals(vm.getStatus(), VMachine.Status.Started);
  }
  
  private boolean isVMNotStarted(String vmId) throws InterruptedException {
    VMachine vm = vmachineService.get(vmId);
    if (vm.getStatus() == VMachine.Status.Started) return false;
    return true;
  }
}
