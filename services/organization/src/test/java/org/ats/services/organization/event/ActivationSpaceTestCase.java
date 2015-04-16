/**
 * 
 */
package org.ats.services.organization.event;

import java.util.ArrayList;
import java.util.List;

import org.ats.services.event.Event;
import org.ats.services.organization.RoleService;
import org.ats.services.organization.UserService;
import org.ats.services.organization.entity.Role;
import org.ats.services.organization.entity.Space;
import org.ats.services.organization.entity.Tenant;
import org.ats.services.organization.entity.User;
import org.ats.services.organization.entity.fatory.ReferenceFactory;
import org.ats.services.organization.entity.reference.SpaceReference;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import akka.actor.UntypedActor;

import com.google.inject.Inject;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Apr 15, 2015
 */
public class ActivationSpaceTestCase extends AbstractEventTestCase {

  @BeforeClass
  public void init() throws Exception {
    super.init(this.getClass().getSimpleName());
    initService();
  }
  
  @AfterClass
  public void shutdown() throws Exception {
    eventService.stop();
  }
  
  private Space space;
  
  @BeforeMethod
  public void setup()  throws Exception {
    Tenant tenant = tenantFactory.create("Fsoft");
    tenantService.create(tenant);
    
    space = spaceFactory.create("FSU1.BU11");
    space.setTenant(tenantRefFactory.create(tenant.getId()));

    List<DBObject> batch = new ArrayList<DBObject>();
    for (int i = 0; i < 1000; i++) {
      Role role = roleFactory.create("role" + i);
      role.setSpace(spaceRefFactory.create(space.getId()));
      space.addRole(roleRefFactory.create(role.getId()));
      batch.add(role);
    }
    
    roleService.create(batch);
    spaceService.create(space);

    batch.clear();
    
    for (int i = 0; i < (100 * 1000); i++) {
      User user = userFactory.create("user" + i + "@fsoft.com.vn", "user" + i, "fsofter");
      user.setTenant(tenantRefFactory.create(tenant.getId()));
      user.joinSpace(spaceRefFactory.create(space.getId()));
      batch.add(user);
      if (batch.size() == 1000) {
        userService.create(batch);
        batch.clear();
      }
    }
  }
  
  @AfterMethod
  public void tearDown() throws Exception {
    this.mongoService.dropDatabase();
  }
  
  @Test
  public void test() throws Exception {
    SpaceReference spaceRef = spaceRefFactory.create(space.getId());
    Assert.assertEquals(userService.query(new BasicDBObject("active", false)).count(), 0);
    Assert.assertEquals(userService.query(new BasicDBObject("active", true)).count(), 100 * 1000);
    
    Assert.assertEquals(roleService.query(new BasicDBObject("active", false)).count(), 0);
    Assert.assertEquals(roleService.query(new BasicDBObject("active", true)).count(), 1000);
    
    eventService.setListener(ActivationListener.class);
    spaceService.inActive(space);
    
    waitToFinishActivationSpace(spaceRef, true);
    
    spaceService.active(space);
    
    waitToFinishActivationSpace(spaceRef, false);
  }
  
  static class ActivationListener extends UntypedActor {
    
    @Inject UserService userService;
    
    @Inject RoleService roleService;
    
    @Inject ReferenceFactory<SpaceReference> spaceRefFactory;

    @Override
    public void onReceive(Object obj) throws Exception {
      if (obj instanceof Event) {
        Event event = (Event) obj;
        if ("in-active-space".equals(event.getName())) {
          Space space = (Space) event.getSource();
          SpaceReference spaceRef = spaceRefFactory.create(space.getId());
          assertActivation(spaceRef, true);
        } else if ("active-space".equals(event.getName())) {
          Space space = (Space) event.getSource();
          SpaceReference spaceRef = spaceRefFactory.create(space.getId());
          assertActivation(spaceRef, false);
        }
      }
    }
    
    private void assertActivation(SpaceReference spaceRef, boolean active) {
      BasicDBObject query = new BasicDBObject("spaces", new BasicDBObject("$elemMatch", spaceRef.toJSon()));
      query.append("active", active);
      Assert.assertEquals(userService.query(query).count(), 0);
      
      query.append("active", !active);
      Assert.assertEquals(userService.query(query).count(), 100 * 1000);
      
      query = new BasicDBObject("space", spaceRef.toJSon()).append("active", active);
      Assert.assertEquals(roleService.query(query).count(), 0);
      
      query.append("active", !active);
      Assert.assertEquals(roleService.query(query).count(), 1000);
    }
  }
}
