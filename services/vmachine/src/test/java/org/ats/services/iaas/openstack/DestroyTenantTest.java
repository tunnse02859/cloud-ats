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
import org.ats.services.organization.entity.reference.TenantReference;
import org.ats.services.organization.event.AbstractEventTestCase;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.inject.Guice;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Jul 17, 2015
 */
public class DestroyTenantTest extends AbstractEventTestCase {
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
    this.openstackService.addCredential("fsoft-testonly");
  }
  
  @Test
  public void testDestroy() {
    
  }
  
  @AfterClass
  public void shutdown() throws Exception {
    TenantReference tenantRef = tenantRefFactory.create("fsoft-testonly");
    openstackService.destroyTenant(tenantRef);
    
    this.eventService.stop();
    this.mongoService.dropDatabase();
  }
}
