/**
 * 
 */
package org.ats.services.iaas.openstack;

import java.util.List;

import org.ats.common.PageList;
import org.ats.services.OrganizationServiceModule;
import org.ats.services.VMachineServiceModule;
import org.ats.services.data.DatabaseModule;
import org.ats.services.data.MongoDBService;
import org.ats.services.event.EventModule;
import org.ats.services.event.EventService;
import org.ats.services.organization.entity.Tenant;
import org.ats.services.organization.entity.reference.TenantReference;
import org.ats.services.organization.event.AbstractEventTestCase;
import org.ats.services.vmachine.VMachine;
import org.ats.services.vmachine.VMachineService;
import org.jclouds.openstack.neutron.v2.NeutronApi;
import org.jclouds.openstack.neutron.v2.domain.Rule;
import org.jclouds.openstack.neutron.v2.domain.Rule.CreateBuilder;
import org.jclouds.openstack.neutron.v2.domain.Rule.CreateRule;
import org.jclouds.openstack.neutron.v2.domain.RuleDirection;
import org.jclouds.openstack.neutron.v2.domain.RuleEthertype;
import org.jclouds.openstack.neutron.v2.domain.RuleProtocol;
import org.jclouds.openstack.neutron.v2.domain.SecurityGroup;
import org.jclouds.openstack.neutron.v2.extensions.SecurityGroupApi;
import org.jclouds.openstack.nova.v2_0.NovaApi;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.common.base.Optional;
import com.google.inject.Guice;
import com.mongodb.BasicDBObject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Jul 6, 2015
 */
public class OpenStackServiceTestCase extends AbstractEventTestCase {

  private OpenStackService openstackService;
  
  private VMachineService vmachineService;
  
  @BeforeClass
  public void init() throws Exception {
    this.injector = Guice.createInjector(
        new DatabaseModule(), 
        new EventModule(),
        new OrganizationServiceModule(),
        new VMachineServiceModule("src/test/resources/iaas.conf"));
    
    this.openstackService = injector.getInstance(OpenStackService.class);
    this.vmachineService = injector.getInstance(VMachineService.class);
    this.mongoService = injector.getInstance(MongoDBService.class);
    this.mongoService.dropDatabase();

    //start event service
    this.eventService = injector.getInstance(EventService.class);
    this.eventService.setInjector(injector);
    this.eventService.start();

    initService();
    this.openstackService.addCredential("admin", "admin",  "ADMIN_PASS");
  }
  
  @AfterMethod
  public void tearDown() throws Exception {
    this.mongoService.dropDatabase();
  }
  
  @AfterClass
  public void shutdown() throws Exception {
    this.eventService.stop();
    this.mongoService.dropDatabase();
  }
  
  //@Test
  public void testInitAndDestroyTenant() throws Exception {
    Tenant fsoft = tenantFactory.create("fsoft-testonly");
    tenantService.create(fsoft);
    
    TenantReference tenantRef = tenantRefFactory.create("fsoft-testonly");
    openstackService.initTenant(tenantRef);
    
    fsoft = tenantService.get(tenantRef.getId(), "tenant_id", "user_id", "network_id", "subnet_id", "router_id");
    Assert.assertNotNull(fsoft.get("tenant_id"));
    Assert.assertNotNull(fsoft.get("user_id"));
    Assert.assertNotNull(fsoft.get("network_id"));
    Assert.assertNotNull(fsoft.get("subnet_id"));
    Assert.assertNotNull(fsoft.get("router_id"));
    
    PageList<VMachine> list = vmachineService.query(new BasicDBObject("tenant", tenantRef.toJSon()));
    Assert.assertEquals(list.totalPage(), 1);
    List<VMachine> page = list.getPage(1);
    Assert.assertEquals(page.size(), 1);
    
//    openstackService.destroyTenant(tenantRef);
//    fsoft = tenantService.get(tenantRef.getId(), "tenant_id", "user_id", "network_id", "subnet_id", "router_id");
//    Assert.assertNull(fsoft.get("tenant_id"));
//    Assert.assertNull(fsoft.get("user_id"));
//    Assert.assertNull(fsoft.get("network_id"));
//    Assert.assertNull(fsoft.get("subnet_id"));
//    Assert.assertNull(fsoft.get("router_id"));
  }
  
  //@Test
  public void testVMAction() throws Exception {
    Tenant fsoft = tenantFactory.create("fsoft-testonly");
    tenantService.create(fsoft);
    
    TenantReference tenantRef = tenantRefFactory.create("fsoft-testonly");
    openstackService.initTenant(tenantRef);
    
    PageList<VMachine> list = vmachineService.query(new BasicDBObject("tenant", tenantRef.toJSon()));
    Assert.assertEquals(list.totalPage(), 1);
    List<VMachine> page = list.getPage(1);
    Assert.assertEquals(page.size(), 1);
    
    VMachine vm = page.get(0);
    Assert.assertEquals(vm.getStatus(), VMachine.Status.Started);
    Assert.assertTrue(vm.isSystem());
    Assert.assertFalse(vm.hasUI());
    Assert.assertNull(vm.getPublicIp());
    Assert.assertEquals(vm.getPrivateIp(), "192.168.1.2");
    
    vm = openstackService.stop(vm);
    Assert.assertEquals(vm.getStatus(), VMachine.Status.Stopped);
    
    vm = openstackService.start(vm);
    Assert.assertEquals(vm.getStatus(), VMachine.Status.Started);
    openstackService.destroyTenant(tenantRef);
  }
  
  @Test
  public void test() throws Exception {
    Tenant fsoft = tenantFactory.create("fsoft-testonly");
    tenantService.create(fsoft);
    
    TenantReference tenantRef = tenantRefFactory.create("fsoft-testonly");
    openstackService.addCredential(tenantRef.getId());

    openstackService.createSystemVM(tenantRef, null);
//    openstackService.createSystemVM(tenantRef, null);
//    openstackService.createSystemVM(tenantRef, null);
//    openstackService.createSystemVM(tenantRef, null);
//    openstackService.createSystemVM(tenantRef, null);
    
//    
//    openstackService.createTestVM(tenantRef, null, true);
//    openstackService.createTestVM(tenantRef, null, false);
    
  }
  
}
