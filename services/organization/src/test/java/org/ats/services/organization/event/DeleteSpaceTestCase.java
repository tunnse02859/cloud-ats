/**
 * 
 */
package org.ats.services.organization.event;

import org.ats.services.organization.RoleService;
import org.ats.services.organization.UserService;
import org.ats.services.organization.entity.reference.SpaceReference;
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
public class DeleteSpaceTestCase extends AbstractEventTestCase {
  
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
  public void testDeleteSpace() throws InterruptedException {
    
    Assert.assertEquals(spaceService.list().next().size(), 2);
    Assert.assertEquals(space1.getName(), "FSU1.BU11");
    Assert.assertEquals(space1.getRoles().size(), 2);
    
    eventService.setListener(DeleteSpaceListener.class);
    spaceService.delete(space1);
    Assert.assertEquals(spaceService.list().next().size(), 1);
    
    //wait to finish
    waitToFinishDeleteSpace(spaceRefFactory.create(space1.getId()));
  }

  static class DeleteSpaceListener extends UntypedActor {
    
    @Inject RoleService roleService;
    
    @Inject UserService userService;

    @Override
    public void onReceive(Object message) throws Exception {
      if (message instanceof SpaceReference) {
          SpaceReference ref = (SpaceReference) message;
          Assert.assertEquals(roleService.findIn("space", ref).count(), 0);
          Assert.assertEquals(userService.findIn("spaces", ref).count(), 0);
      }
    }
  }
  
}
