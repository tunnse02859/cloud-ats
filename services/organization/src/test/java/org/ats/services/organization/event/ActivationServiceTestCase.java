/**
 * 
 */
package org.ats.services.organization.event;

import java.util.logging.Logger;

import org.ats.services.data.MongoDBService;
import org.ats.services.event.Event;
import org.ats.services.organization.ActivationService;
import org.ats.services.organization.FeatureService;
import org.ats.services.organization.RoleService;
import org.ats.services.organization.SpaceService;
import org.ats.services.organization.TenantService;
import org.ats.services.organization.UserService;
import org.ats.services.organization.entity.Space;
import org.ats.services.organization.entity.Tenant;
import org.ats.services.organization.entity.reference.FeatureReference;
import org.ats.services.organization.entity.reference.SpaceReference;
import org.ats.services.organization.entity.reference.TenantReference;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import akka.actor.UntypedActor;

import com.google.inject.Inject;


/**
 * @author TrinhTV3
 *
 * Email: TrinhTV3@fsoft.com.vn
 */
public class ActivationServiceTestCase extends AbstractEventTestCase {

  
  @Override @BeforeMethod
  public void init() throws Exception {
    super.init();
  }
  
  @Test
  public void testActivationUser() {
    
    Assert.assertEquals(activationService.countTenant(), 0);
    Assert.assertEquals(userService.count(), 1);
    
    activationService.inActiveUser("haint@cloud-ats.net");
    
    Assert.assertEquals(userService.count(), 0);
    Assert.assertEquals(activationService.countUser(), 1);
    
    activationService.activeUser("haint@cloud-ats.net");
    
    Assert.assertEquals(activationService.countUser(), 0);
    Assert.assertEquals(userService.count(), 1);
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
    
    eventService.setListener(ActivationTenantListener.class);
    
    activationService.inActiveTenant("Fsoft");
    while (tenantService.count() != 2) {
    }
    
    activationService.activeTenant("Fsoft");
  }
  
  @Test
  public void testInactiveFeature() throws InterruptedException {
    Tenant tenant = tenantService.get("Fsoft");
    
    eventService.setListener(inactiveFeatureListener.class);
    activationService.inActiveFeature(tenant.getFeatures().get(0).get());
    
  }
  
  
  @Test
  public void testActiveFeature() throws InterruptedException {
    Tenant tenant = tenantService.get("Fsoft");
    eventService.setListener(ActiveFeatureListener.class);
    activationService.inActiveFeature(tenant.getFeatures().get(0).get());
    activationService.activeFeature("performace");
    
  }
  
  static class ActiveFeatureListener extends UntypedActor {

    @Inject Logger logger;
    
    @Inject private TenantService tenantService;
    
    @Inject private FeatureService featureService;
    
    @Inject private MongoDBService mongoService;
    
    public void onReceive(Object message) throws Exception {
      if (message instanceof FeatureReference) {
        FeatureReference ref = (FeatureReference) message;
        if(featureService.get(ref.getId()) != null) {
          Assert.assertEquals(tenantService.findIn("features", ref).count(), 3);
        }
        
        mongoService.dropDatabase();
      }
    }
  }
  
  static class InActivationTenantListener extends UntypedActor {

    @Inject
    private ActivationService activationService;
    
    @Inject private TenantService tenantService;
    
    @Inject private UserService userService;
    
    @Inject private RoleService roleService;
    
    @Inject private SpaceService spaceService;
    
    @Inject private Logger logger;
    
    @Inject private MongoDBService mongoService;
    
    @Override
    public void onReceive(Object message) throws Exception {
      
      if (message instanceof Event) {
        
        Event event = (Event) message;
        
        if ("active-tenant-ref".equals(event.getName())) {
          
          TenantReference ref = (TenantReference) event.getSource();

          logger.info("processed active tenant "+ ref.toJSon());
          Assert.assertEquals(tenantService.count(), 3);
          
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
      
      if (message instanceof TenantReference) {
        TenantReference ref = (TenantReference) message;
        
        logger.info("actived tenant "+ ref.toJSon());
        
        Assert.assertEquals(tenantService.count(), 3);
        
        mongoService.dropDatabase();
      }
    }
    
  }
  
  static class inactiveFeatureListener extends UntypedActor {

    @Inject Logger logger;
    
    @Inject 
    private TenantService tenantService;
    
    @Inject
    private FeatureService featureService;
    
    @Inject private MongoDBService mongoService;

    @Override
    public void onReceive(Object message) throws Exception {
      
      if (message instanceof FeatureReference) {
        FeatureReference ref = (FeatureReference) message;
        logger.info("processed inactive feature reference "+ ref.toJSon());
        
        Assert.assertEquals(featureService.count(), 2);
        //logger.info("-----"+tenantService.transform(tenantService.findIn("features", ref).next().get(0)).getId()+"------------");
        Assert.assertEquals(tenantService.findIn("features", ref).count(),0);
        
        mongoService.dropDatabase();
      }
      
    }
  }
  
  @Test
  public void testInactiveSpace() throws InterruptedException {
    //initData(100);
    Space space = spaceService.list().next().get(0);
    eventService.setListener(InactiveSpaceListener.class);
    activationService.inActiveSpace(space);
    Assert.assertEquals(roleService.count(), 2);
  }
  static class InactiveSpaceListener extends UntypedActor {

    @Inject
    private Logger logger;
    
    @Inject
    private UserService userService;
    
    @Inject
    private RoleService roleService;
    
    @Override
    public void onReceive(Object message) throws Exception {
      if(message instanceof SpaceReference) {
        SpaceReference ref = (SpaceReference) message;
        logger.info("processed inactive space reference "+ ref.toJSon());
        Assert.assertEquals(roleService.count(), 0);
        Assert.assertEquals(userService.get("haint@cloud-ats.net").getSpaces().size(), 0);
      }
      
    }
    
  }
  
  @Test 
  public void testActiveSpace() throws InterruptedException {
    Space space = spaceService.list().next().get(0);
    eventService.setListener(ActiveSpaceListener.class);
    activationService.inActiveSpace(space);
    activationService.activeSpace(space);
  }
  
  static class ActiveSpaceListener extends UntypedActor {

    @Inject
    private Logger logger;
    
    @Inject
    private SpaceService spaceService;
    
    @Inject
    private RoleService roleService;
    
    @Inject
    private UserService userService;
    
    @Override
    public void onReceive(Object message) throws Exception {
      if(message instanceof SpaceReference) {
        SpaceReference ref = (SpaceReference) message;
        
        if(spaceService.get(ref.getId()) != null) {
          logger.info("processed active space reference "+ ref.toJSon());
          Assert.assertEquals(roleService.count(), 2);
          Assert.assertEquals(userService.get("haint@cloud-ats.net").getSpaces().size(), 1);
        }
      }
      
    }
    
  }
  
}
