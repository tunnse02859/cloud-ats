/**
 * 
 */
package org.ats.services.organization.event;

import org.ats.services.organization.SpaceService;
import org.ats.services.organization.UserService;
import org.ats.services.organization.entity.Space;
import org.ats.services.organization.entity.User;
import org.ats.services.organization.entity.reference.RoleReference;
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
 * Apr 14, 2015
 */
public class DeleteRoleTestCase extends AbstractEventTestCase {

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
  public void testDeleteRole() throws InterruptedException {
    Space space = spaceService.list().next().get(0);
    Assert.assertEquals(space.getRoles().size(), 2);
    
    User user = userService.list().next().get(0);
    Assert.assertEquals(user.getRoles().size(), 2);
    
    eventService.setListener(DeleteRoleListener.class);
    
    RoleReference ref = roleRefFactory.create(admin.getId());
    roleService.delete(admin);
    Assert.assertEquals(roleService.count(), 1);
    
    //wait to finish event processing
    waitToFinishDeleteRole(ref);

    ref = roleRefFactory.create(tester.getId());
    roleService.delete(tester);
    Assert.assertEquals(roleService.count(), 0);
    
    //wait to finish event processing
    waitToFinishDeleteRole(ref);
  }
  
  static class DeleteRoleListener extends UntypedActor {
    
    @Inject UserService userService;
    
    @Inject SpaceService spaceService;
    
    @Override
    public void onReceive(Object message) throws Exception {
      if (message instanceof RoleReference) {
          RoleReference ref = (RoleReference) message;
          Assert.assertEquals(spaceService.findIn("roles", ref).count(), 0);
          Assert.assertEquals(userService.findIn("roles", ref).count(), 0);
      }
    }
  }
}
