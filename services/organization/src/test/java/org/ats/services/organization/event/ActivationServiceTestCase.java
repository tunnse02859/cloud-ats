/**
 * 
 */
package org.ats.services.organization.event;

import java.util.logging.Logger;

import org.ats.services.organization.ActivationService;
import org.ats.services.organization.FeatureService;
import org.ats.services.organization.RoleService;
import org.ats.services.organization.SpaceService;
import org.ats.services.organization.TenantService;
import org.ats.services.organization.UserService;
import org.ats.services.organization.entity.Tenant;
import org.ats.services.organization.entity.reference.FeatureReference;
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
public class ActivationServiceTestCase extends EventTestCase {

  @Override @BeforeMethod
  public void init() throws Exception {
    super.init();
    
  }
  
  @Test
  public void testActivationUser() {
    Assert.assertEquals(activationService.countInActiveUser(), 0);
    Assert.assertEquals(userService.count(), 1);
    
    activationService.inActiveUser("haint@cloud-ats.net");
    
    Assert.assertEquals(userService.count(), 0);
    Assert.assertEquals(activationService.countInActiveUser(), 1);
    
    activationService.activeUser("haint@cloud-ats.net");
    
    Assert.assertEquals(activationService.countInActiveUser(), 0);
    Assert.assertEquals(userService.count(), 1);
  }
  
  @Test
  public void testInActivationTenant() {
    
    Tenant tenant = tenantService.get("Fsoft");
    
    Assert.assertEquals(activationService.countInActiveTenant(), 0);
    Assert.assertEquals(activationService.countRoleIntoInActiveTenant(), 0);
    Assert.assertEquals(activationService.countSpaceIntoInActiveTenant(), 0);
    Assert.assertEquals(activationService.countInActiveUser(), 0);
    Assert.assertEquals(tenantService.count(), 3);
    Assert.assertEquals(spaceService.findSpaceInTenant(tenantRefFactory.create(tenant.getId())).count(), 2);
    Assert.assertEquals(roleService.count(), 2);
    Assert.assertEquals(userService.count(), 1);
    eventService.setListener(InActivationTenantListener.class);
    
    activationService.inActiveTenant("Fsoft");
    
  }
  
  static class InActivationTenantListener extends UntypedActor {

    @Inject
    private ActivationService activationService;
    
    @Inject private TenantService tenantService;
    
    @Inject private UserService userService;
    
    @Inject private RoleService roleService;
    
    @Inject private SpaceService spaceService;
    
    @Inject private Logger logger;
    
    @Override
    public void onReceive(Object message) throws Exception {
      
      if (message instanceof TenantReference) {
        TenantReference ref = (TenantReference) message;
        
        logger.info("processed inactive tenant : "+ ref.toJSon());
        Assert.assertEquals(activationService.countInActiveTenant(), 1);
        
        Assert.assertEquals(activationService.countSpaceIntoInActiveTenant(), 2);
        
        Assert.assertEquals(activationService.countRoleIntoInActiveTenant(), 2);
        
        Assert.assertEquals(activationService.countInActiveUser(), 1);
        
        Assert.assertEquals(tenantService.count(), 2);
        
        Assert.assertEquals(userService.count(), 0);
        
        Assert.assertEquals(roleService.count(), 0);
        
        Assert.assertEquals(spaceService.count(), 0);
      }
    }
   
  }
  @Test
  public void testActivationTenant() {
    
    activationService.inActiveTenant("Fsoft");
    eventService.setListener(ActivationTenantListener.class);
    while (tenantService.count() != 2) {
      
    }
    activationService.activeTenant("Fsoft");
  }
  
  static class ActivationTenantListener extends UntypedActor {

    @Inject Logger logger;
    
    @Inject private TenantService tenantService;
    
    @Override
    public void onReceive(Object message) throws Exception {
      
      if (message instanceof TenantReference) {
        TenantReference ref = (TenantReference) message;
        
        logger.info("actived tenant "+ ref.toJSon());
        
        Assert.assertEquals(tenantService.count(), 3);
        
      }
      
    }
    
  }
  
  public void testInactiveFeature() throws InterruptedException {
    Tenant tenant = tenantService.get("Fsoft");
    
    eventService.setListener(inactiveFeatureListener.class);
    activationService.inActiveFeature(tenant.getFeatures().get(0).get());
    
  }
  
  static class inactiveFeatureListener extends UntypedActor {

    @Inject Logger logger;
    
    @Inject 
    private TenantService tenantService;
    
    @Inject
    private FeatureService featureService;
    
    @Override
    public void onReceive(Object message) throws Exception {
      
      if (message instanceof FeatureReference) {
        FeatureReference ref = (FeatureReference) message;
        logger.info("processed inactive feature reference "+ ref.toJSon());
        
        Assert.assertEquals(featureService.count(), 2);
        Assert.assertEquals(tenantService.findIn("features", ref).count(),0);
      }
      
    }
    
  }
  
  @Test
  public void testActiveFeature() throws InterruptedException {
    Tenant tenant = tenantService.get("Fsoft");
    eventService.setListener(ActiveFeatureListener.class);
    activationService.inActiveFeature(tenant.getFeatures().get(1).get());
    activationService.activeFeature("performace");
    
  }
  
  static class ActiveFeatureListener extends UntypedActor {

    @Inject Logger logger;
    
    @Inject private TenantService tenantService;
    
    @Inject private FeatureService featureService;
    
    public void onReceive(Object message) throws Exception {
      if (message instanceof FeatureReference) {
        FeatureReference ref = (FeatureReference) message;
        if(featureService.get(ref.getId()) != null) {
          Assert.assertEquals(tenantService.findIn("features", ref).count(), 3);
        }
      }
      
    }
    
  }
}
