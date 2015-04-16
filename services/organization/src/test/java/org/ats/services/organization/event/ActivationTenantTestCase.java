/**
 * 
 */
package org.ats.services.organization.event;

import java.util.ArrayList;
import java.util.List;

import org.ats.services.event.Event;
import org.ats.services.organization.RoleService;
import org.ats.services.organization.SpaceService;
import org.ats.services.organization.UserService;
import org.ats.services.organization.entity.Role;
import org.ats.services.organization.entity.Space;
import org.ats.services.organization.entity.Tenant;
import org.ats.services.organization.entity.User;
import org.ats.services.organization.entity.fatory.ReferenceFactory;
import org.ats.services.organization.entity.reference.TenantReference;
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
public class ActivationTenantTestCase extends AbstractEventTestCase {

  @BeforeClass
  public void init() throws Exception {
    super.init(this.getClass().getSimpleName());
    initService();
  }
  
  @AfterClass
  public void shutdown() throws Exception {
    eventService.stop();
  }
  
  private Tenant tenant;
  
  @BeforeMethod
  public void setup()  throws Exception {
    tenant = tenantFactory.create("Fsoft");
    tenantService.create(tenant);
    
    List<DBObject> spaceBatch = new ArrayList<DBObject>();
    for (int i = 0; i < 100; i++) {
      Space space = spaceFactory.create("FSU1.BU" + i);
      space.setTenant(tenantRefFactory.create(tenant.getId()));

      List<DBObject> batch = new ArrayList<DBObject>();
      for (int j = 0; j < 100; j++) {
        Role role = roleFactory.create("role" + j);
        role.setSpace(spaceRefFactory.create(space.getId()));
        space.addRole(roleRefFactory.create(role.getId()));
        batch.add(role);
      }
      roleService.create(batch);
      spaceBatch.add(space);
    }
    spaceService.create(spaceBatch);
    
    List<DBObject> batch = new ArrayList<DBObject>();
    for (int i = 0; i < (10 * 1000); i++) {
      User user = userFactory.create("user" + i + "@fsoft.com.vn", "user" + i, "fsofter");
      for (int j = 0; j < 100; j++) {
        user.joinSpace(spaceRefFactory.create("FSU1.BU" + j));
      }
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
  public void test() {
    
    Assert.assertEquals(spaceService.query(new BasicDBObject("active", false)).count(), 0);
    Assert.assertEquals(spaceService.query(new BasicDBObject("active", true)).count(), 100);
    
    Assert.assertEquals(userService.query(new BasicDBObject("active", false)).count(), 0);
    Assert.assertEquals(userService.query(new BasicDBObject("active", true)).count(), 10 * 1000);
    
    Assert.assertEquals(roleService.query(new BasicDBObject("active", false)).count(), 0);
    Assert.assertEquals(roleService.query(new BasicDBObject("active", true)).count(), 100 * 100);
    
    eventService.setListener(ActivationListener.class);
    tenantService.inActive(tenant);
    waitToFinishActivationTenant(tenantRefFactory.create(tenant.getId()), true);
    tenantService.active(tenant);
    waitToFinishActivationTenant(tenantRefFactory.create(tenant.getId()), false);
  }
  
  static class ActivationListener extends UntypedActor {
    
    @Inject UserService userService;
    
    @Inject SpaceService spaceService;
    
    @Inject RoleService roleService;
    
    @Inject ReferenceFactory<TenantReference> tenantRefFactory;

    @Override
    public void onReceive(Object obj) throws Exception {
      if (obj instanceof Event) {
        Event event = (Event) obj;
        if ("in-active-tenant".equals(event.getName())) {
          Tenant tenant = (Tenant) event.getSource();
          TenantReference ref = tenantRefFactory.create(tenant.getId());
          assertActivation(ref, true);
        } else if("active-tenant".equals(event.getName())) {
          Tenant tenant = (Tenant) event.getSource();
          TenantReference ref = tenantRefFactory.create(tenant.getId());
          assertActivation(ref, false);
        }
      }
    }
    
    private void assertActivation(TenantReference tenantRef, boolean active) {
      BasicDBObject query = new BasicDBObject("tenant", tenantRef.toJSon());
      query.append("active", active);
      Assert.assertEquals(userService.query(query).count(), 0);
      Assert.assertEquals(spaceService.query(query).count(), 0);
      
      query.append("active", !active);
      Assert.assertEquals(userService.query(query).count(), 10 * 1000);
      Assert.assertEquals(spaceService.query(query).count(), 100);
      
      Assert.assertEquals(roleService.query(new BasicDBObject("active", active)).count(), 0);
      Assert.assertEquals(roleService.query(new BasicDBObject("active", !active)).count(), 100 * 100);
    }
  }
}
