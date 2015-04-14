/**
 * 
 */
package org.ats.services.organization.event;

import java.util.logging.Logger;

import org.ats.services.data.MongoDBService;
import org.ats.services.event.Event;
import org.ats.services.organization.ActivationService;
import org.ats.services.organization.RoleService;
import org.ats.services.organization.SpaceService;
import org.ats.services.organization.TenantService;
import org.ats.services.organization.UserService;
import org.ats.services.organization.entity.Tenant;
import org.ats.services.organization.entity.reference.TenantReference;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import akka.actor.UntypedActor;

import com.google.inject.Inject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Apr 14, 2015
 */
public class ActivationTenantTestCase extends AbstractEventTestCase {

  @BeforeMethod
  public void init() throws Exception {
    super.init(this.getClass().getSimpleName());
  }
  
  @Test
  public void testInActivationTenant() {
    
    Tenant tenant = tenantService.get("Fsoft");
    
    Assert.assertEquals(activationService.countInActiveTenant(), 0);
    Assert.assertEquals(activationService.countRole(), 0);
    Assert.assertEquals(activationService.countSpace(), 0);
    Assert.assertEquals(activationService.countUser(), 0);
    Assert.assertEquals(tenantService.count(), 3);
    Assert.assertEquals(spaceService.findSpaceInTenant(tenantRefFactory.create(tenant.getId())).count(), 2);
    Assert.assertEquals(roleService.count(), 2);
    Assert.assertEquals(userService.count(), 1);

    eventService.setListener(InActivationTenantListener.class);
    activationService.inActiveTenant("Fsoft");
  }
  
  @Test
  public void testActivationTenant() {
    
    activationService.inActiveTenant("Fsoft");
    while (tenantService.count() != 2) {
    }
    
    eventService.setListener(ActivationTenantListener.class);
    activationService.activeTenant("Fsoft");
  }
  
  static class InActivationTenantListener extends UntypedActor {

    @Inject ActivationService activationService;
    
    @Inject TenantService tenantService;
    
    @Inject UserService userService;
    
    @Inject RoleService roleService;
    
    @Inject SpaceService spaceService;
    
    @Inject Logger logger;
    
    @Inject MongoDBService mongoService;
    
    @Override
    public void onReceive(Object message) throws Exception {
      
      if (message instanceof Event) {
        try {
          Event event = (Event) message;
          if ("inactive-tenant-ref".equals(event.getName())) {
            Assert.assertEquals(activationService.countInActiveTenant(), 1);
            Assert.assertEquals(activationService.countSpace(), 2);
            Assert.assertEquals(activationService.countRole(), 2);
            Assert.assertEquals(activationService.countUser(), 1);
            Assert.assertEquals(tenantService.count(), 2);
            Assert.assertEquals(userService.count(), 0);
            Assert.assertEquals(roleService.count(), 0);
            Assert.assertEquals(spaceService.count(), 0);
          }
        } finally {
          mongoService.dropDatabase();
        }
      }
    }
  }
  
  static class ActivationTenantListener extends UntypedActor {

    @Inject Logger logger;
    
    @Inject private TenantService tenantService;
    
    @Inject private MongoDBService mongoService;
    
    @Override
    public void onReceive(Object message) throws Exception {
      if (message instanceof Event) {

        Event event = (Event) message;

        if ("active-tenant-ref".equals(event.getName())) {

          TenantReference ref = (TenantReference) event.getSource();

          logger.info("processed active tenant " + ref.toJSon());
          Assert.assertEquals(tenantService.count(), 3);

          mongoService.dropDatabase();
        }
      }
    }
  }
}
