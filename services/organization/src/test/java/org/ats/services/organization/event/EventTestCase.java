/**
 * 
 */
package org.ats.services.organization.event;

import java.util.logging.Logger;

import org.ats.services.data.MongoDBService;
import org.ats.services.organization.RoleService;
import org.ats.services.organization.SpaceService;
import org.ats.services.organization.TenantService;
import org.ats.services.organization.UserService;
import org.ats.services.organization.entity.Space;
import org.ats.services.organization.entity.Tenant;
import org.ats.services.organization.entity.User;
import org.ats.services.organization.entity.reference.FeatureReference;
import org.ats.services.organization.entity.reference.RoleReference;
import org.ats.services.organization.entity.reference.SpaceReference;
import org.ats.services.organization.entity.reference.TenantReference;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import akka.actor.UntypedActor;

import com.google.inject.Inject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Mar 25, 2015
 */
public class EventTestCase extends AbstractEventTestCase {
  
  @Override @BeforeMethod
  public void init() throws Exception {
    super.init();
  }
  
  @Test
  public void testDeleteRole() throws InterruptedException {
    Space space = spaceService.list().next().get(0);
    Assert.assertEquals(space.getRoles().size(), 2);
    
    User user = userService.list().next().get(0);
    Assert.assertEquals(user.getRoles().size(), 2);
    
    eventService.setListener(DeleteRoleListener.class);
    
    roleService.delete(admin);
    Assert.assertEquals(roleService.count(), 1);

    roleService.delete(tester);
    Assert.assertEquals(roleService.count(), 0);
  }
  
  @Test
  public void testDeleteSpace() throws InterruptedException {
    Tenant tenant = tenantService.get("Fsoft");
    
    Assert.assertEquals(spaceService.list().next().size(), 2);
    
    Space space = null;
    space = spaceFactory.create("FSU1.Z8");
    space.setTenant(tenantRefFactory.create(tenant.getId()));
    spaceService.create(space);
    
    Assert.assertEquals(spaceService.list().next().size(), 3);
    
    space = spaceFactory.create("FHO");
    space.setTenant(tenantRefFactory.create(tenant.getId()));
    spaceService.create(space);
    
    Assert.assertEquals(spaceService.list().next().size(), 4);
    
    space = spaceService.list().next().get(0);
    Assert.assertEquals(space.getName(), "FSU1.BU11");
    Assert.assertEquals(space.getRoles().size(), 2);
    
    eventService.setListener(DeleteSpaceListener.class);
    spaceService.delete(space);
    Assert.assertEquals(spaceService.list().next().size(), 3);
    
  }
  
  static class DeleteRoleListener extends UntypedActor {
    
    @Inject Logger logger;
    
    @Inject UserService userService;
    
    @Inject SpaceService spaceService;
    
    @Inject MongoDBService mongoService;

    @Override
    public void onReceive(Object message) throws Exception {
      if (message instanceof RoleReference) {
        RoleReference ref = (RoleReference) message;
        logger.info("processed delete role reference " + ref.toJSon());
        
        Assert.assertEquals(spaceService.findIn("roles", ref).count(), 0);
        Assert.assertEquals(userService.findIn("roles", ref).count(), 0);
        mongoService.dropDatabase();
      }
    }
    
  }
  
  static class DeleteSpaceListener extends UntypedActor {
    
    @Inject Logger logger;
    
    @Inject RoleService roleService;
    
    @Inject UserService userService;
    
    @Inject MongoDBService mongoService;
    
    @Override
    public void onReceive(Object message) throws Exception {
      if (message instanceof SpaceReference) {
        SpaceReference ref = (SpaceReference) message;
        logger.info("processed delete space reference " + ref.toJSon());
        
        Assert.assertEquals(roleService.count(), 0);
        Assert.assertEquals(userService.get("haint@cloud-ats.net").getSpaces().size(), 0);
        mongoService.dropDatabase();
      }
      
    }
    
  }

  @Test
  public void testDeleteFeature() throws InterruptedException {
    
    Tenant tenant = tenantService.list().next().get(0);
    Assert.assertEquals(tenant.getFeatures().size(), 3);
    
    eventService.setListener(DeleteFeatureListener.class);
    
    featureService.delete("functional");
    
    Assert.assertEquals(featureService.count(), 2);
    
    featureService.delete("performace");
    
    Assert.assertEquals(featureService.count(), 1);
  }
  
  static class DeleteFeatureListener extends UntypedActor {
    
    @Inject Logger logger;
    
    @Inject UserService featureService;
    
    @Inject TenantService tenantService;
    
    @Inject MongoDBService mongoService;
    
    @Override
    public void onReceive(Object message) throws Exception {
      if (message instanceof FeatureReference) {
        FeatureReference ref = (FeatureReference) message;
        logger.info("processed delete feature reference " + ref.toJSon());
        Assert.assertEquals(tenantService.findIn("features", ref).count(), 0);
        
        mongoService.dropDatabase();
      }
    }
    
  }
  
  @Test
  public void testDeleteTenant() throws InterruptedException {
    
    Assert.assertEquals(tenantService.count(), 3);
    
    Assert.assertEquals(userService.count(), 1);
    
    eventService.setListener(DeleteTenantListenter.class);
    tenantService.delete("Fsoft");
    Assert.assertEquals(tenantService.count(), 2);
    
  }
  
  static class DeleteTenantListenter extends UntypedActor {

    @Inject Logger logger;
    
    @Inject private UserService userService;
    
    @Inject private SpaceService spaceService;
    
    @Inject MongoDBService mongoService;
    
    @Override
    public void onReceive(Object message) throws Exception {
      
      if (message instanceof TenantReference) {
        
        TenantReference ref = (TenantReference) message;
        
        logger.info("processed delete tenant reference "+ ref.toJSon());
        Assert.assertEquals(userService.findUserInTenant(ref).count(), 0);
        Assert.assertEquals(spaceService.findSpaceInTenant(ref).count(), 0);
        
        mongoService.dropDatabase();
      }
      
    }
    
  }
}
