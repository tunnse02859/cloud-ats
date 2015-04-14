/**
 * 
 */
package org.ats.services.organization.event;

import org.ats.services.organization.SpaceService;
import org.ats.services.organization.UserService;
import org.ats.services.organization.entity.reference.TenantReference;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import akka.actor.UntypedActor;

import com.google.inject.Inject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Mar 25, 2015
 */
public class DeleteTenantTestCase extends AbstractEventTestCase {
  
  @BeforeClass
  public void init() throws Exception {
    super.init(this.getClass().getSimpleName());
  }
  
  @AfterClass
  public void shutdown() throws Exception {
    eventService.stop();
  }
  
  @BeforeMethod
  public void setup()  throws Exception {
    super.initData();
  }
  
  @AfterMethod
  public void tearDown() throws Exception {
    this.mongoService.dropDatabase();
  }
  
  @Test
  public void testDeleteTenant() throws InterruptedException {
    
    Assert.assertEquals(tenantService.count(), 1);
    Assert.assertEquals(userService.count(), 1);
    
    eventService.setListener(DeleteTenantListenter.class);
    tenantService.delete("Fsoft");
    Assert.assertEquals(tenantService.count(), 0);
    
    //
    waitToFinishDeleteTenant(tenantRefFactory.create("Fsoft"));
  }
  
  static class DeleteTenantListenter extends UntypedActor {

    @Inject UserService userService;
    
    @Inject SpaceService spaceService;
    
    @Override
    public void onReceive(Object message) throws Exception {
      
      if (message instanceof TenantReference) {
          TenantReference ref = (TenantReference) message;
          Assert.assertEquals(userService.findUserInTenant(ref).count(), 0);
          Assert.assertEquals(spaceService.findSpaceInTenant(ref).count(), 0);
      }
    }
  }
}
