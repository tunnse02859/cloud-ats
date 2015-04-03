/**
 * 
 */
package org.ats.services.organization.event;

import java.util.logging.Logger;

import org.ats.services.event.EventService;
import org.ats.services.organization.AbstractTestCase;
import org.ats.services.organization.ActivationService;
import org.ats.services.organization.FeatureService;
import org.ats.services.organization.RoleService;
import org.ats.services.organization.SpaceService;
import org.ats.services.organization.TenantService;
import org.ats.services.organization.UserService;
import org.ats.services.organization.entity.Role;
import org.ats.services.organization.entity.Space;
import org.ats.services.organization.entity.Tenant;
import org.ats.services.organization.entity.User;
import org.ats.services.organization.entity.fatory.FeatureFactory;
import org.ats.services.organization.entity.fatory.PermissionFactory;
import org.ats.services.organization.entity.fatory.ReferenceFactory;
import org.ats.services.organization.entity.fatory.RoleFactory;
import org.ats.services.organization.entity.fatory.SpaceFactory;
import org.ats.services.organization.entity.fatory.TenantFactory;
import org.ats.services.organization.entity.fatory.UserFactory;
import org.ats.services.organization.entity.reference.FeatureReference;
import org.ats.services.organization.entity.reference.RoleReference;
import org.ats.services.organization.entity.reference.SpaceReference;
import org.ats.services.organization.entity.reference.TenantReference;
import org.ats.services.organization.entity.reference.UserReference;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import akka.actor.UntypedActor;

import com.google.inject.Inject;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Mar 25, 2015
 */
public class EventTestCase extends AbstractTestCase {
  
  private TenantService tenantService;
  private TenantFactory tenantFactory;
  private ReferenceFactory<TenantReference> tenantRefFactory;
  
  private SpaceService spaceService;
  private SpaceFactory spaceFactory;
  private ReferenceFactory<SpaceReference> spaceRefFactory;
  
  private RoleService roleService;
  private RoleFactory roleFactory;
  private ReferenceFactory<RoleReference> roleRefFactory;
  private PermissionFactory permFactory;
  
  private UserService userService;
  private UserFactory userFactory;

  private FeatureFactory featureFactory;
  private ReferenceFactory<FeatureReference> featureRefFactory;
  private FeatureService featureService;
  
  private ActivationService activationService;
  
  @Override @BeforeMethod
  public void init() throws Exception {
    super.init();
    this.tenantService = injector.getInstance(TenantService.class);
    this.tenantFactory = injector.getInstance(TenantFactory.class);
    this.tenantRefFactory = injector.getInstance(Key.get(new TypeLiteral<ReferenceFactory<TenantReference>>(){}));
    
    this.spaceService = injector.getInstance(SpaceService.class);
    this.spaceFactory = injector.getInstance(SpaceFactory.class);
    this.spaceRefFactory = injector.getInstance(Key.get(new TypeLiteral<ReferenceFactory<SpaceReference>>(){}));
    
    this.roleService = injector.getInstance(RoleService.class);
    this.roleFactory = injector.getInstance(RoleFactory.class);
    this.roleRefFactory = injector.getInstance(Key.get(new TypeLiteral<ReferenceFactory<RoleReference>>(){}));
    this.permFactory = injector.getInstance(PermissionFactory.class);
    
    this.userService = injector.getInstance(UserService.class);
    this.userFactory = injector.getInstance(UserFactory.class);
    
    this.featureFactory = injector.getInstance(FeatureFactory.class);
    this.featureService = injector.getInstance(FeatureService.class);
    this.featureRefFactory = injector.getInstance(Key.get(new TypeLiteral<ReferenceFactory<FeatureReference>>(){}));
    this.activationService = injector.getInstance(ActivationService.class);
    initData();
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

    @Override
    public void onReceive(Object message) throws Exception {
      if (message instanceof RoleReference) {
        RoleReference ref = (RoleReference) message;
        logger.info("processed delete role reference " + ref.toJSon());
        
        Assert.assertEquals(spaceService.findIn("roles", ref).count(), 0);
        Assert.assertEquals(userService.findIn("roles", ref).count(), 0);
      }
    }
    
  }
  
  static class DeleteSpaceListener extends UntypedActor {
    
    @Inject Logger logger;
    
    @Inject RoleService roleService;
    
    @Inject UserService userService;
    
    @Override
    public void onReceive(Object message) throws Exception {
      if (message instanceof SpaceReference) {
        SpaceReference ref = (SpaceReference) message;
        logger.info("processed delete space reference " + ref.toJSon());
        
        Assert.assertEquals(roleService.count(), 0);
        Assert.assertEquals(userService.get("haint@cloud-ats.net").getSpaces().size(), 0);
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
    
    @Override
    public void onReceive(Object message) throws Exception {
      if (message instanceof FeatureReference) {
        FeatureReference ref = (FeatureReference) message;
        logger.info("processed delete feature reference " + ref.toJSon());
        Assert.assertEquals(tenantService.findIn("features", ref).count(), 0);
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
    @Override
    public void onReceive(Object message) throws Exception {
      
      if (message instanceof TenantReference) {
        
        TenantReference ref = (TenantReference) message;
        
        logger.info("processed delete tenant reference "+ ref.toJSon());
        Assert.assertEquals(userService.findUserInTenant(ref).count(), 0);
        Assert.assertEquals(spaceService.findSpaceInTenant(ref).count(), 0);
      }
      
    }
    
  }
  
  @Test
  public void testInActiveUser() {
    Assert.assertEquals(activationService.countInActiveUser(), 0);
    
    eventService.setListener(InActiveUserListener.class);
    activationService.inActiveUser("haint@cloud-ats.net");
    
  }
  
  static class InActiveUserListener extends UntypedActor {

    @Inject
    private Logger logger;
    
    @Inject
    private ActivationService activationService;
    
    @Override
    public void onReceive(Object message) throws Exception {
      
      if (message instanceof UserReference) {
        UserReference ref = (UserReference) message;
        logger.info("processed move user "+ ref.toJSon());
        
        Assert.assertEquals(activationService.countInActiveUser(), 1);
        
      }
      
    }
    
    
  }
  private Role admin;
  private Role tester;
  
  private void initData() {
    Tenant tenant = tenantFactory.create("Fsoft");
    
    FeatureReference feature1 = featureRefFactory.create("performace");
    FeatureReference feature2 = featureRefFactory.create("functional");
    FeatureReference feature3 = featureRefFactory.create("organization");
    
    featureService.create(featureFactory.create(feature1.getId()), featureFactory.create(feature2.getId()), featureFactory.create(feature3.getId()));
    tenant.addFeature(feature1, feature2, feature3);
    tenantService.create(tenant);
    
    Tenant tenant1 = tenantFactory.create("Fsoft1");
    tenant1.addFeature(feature1, feature2, feature3);
    tenantService.create(tenant1);
    
    Tenant tenant2 = tenantFactory.create("Fsoft2");
    tenant2.addFeature(feature1, feature2, feature3);
    tenantService.create(tenant2);
    
    Space space = spaceFactory.create("FSU1.BU11");
    space.setTenant(tenantRefFactory.create(tenant.getId()));
    
    Space space1 = spaceFactory.create("FSU1.Bu12");
    space1.setTenant(tenantRefFactory.create(tenant.getId()));
    
    admin = roleFactory.create("admin");
    admin.setSpace(spaceRefFactory.create(space.getId()));
    admin.addPermission(permFactory.create("*:*@Fsoft:*"));
    
    tester = roleFactory.create("tester");
    tester.setSpace(spaceRefFactory.create(space.getId()));
    tester.addPermission(permFactory.create("test:*@Fsoft:" + space.getId()));
    
    space.addRole(roleRefFactory.create(admin.getId()), roleRefFactory.create(tester.getId()));
    spaceService.create(space, space1);
    roleService.create(admin, tester);
    
    User user = userFactory.create("haint@cloud-ats.net", "Hai", "Nguyen");
    user.setTenant(tenantRefFactory.create(tenant.getId()));
    user.joinSpace(spaceRefFactory.create(space.getId()));
    user.addRole(roleRefFactory.create(admin.getId()), roleRefFactory.create(tester.getId()));
    
    userService.create(user);
  }
  
}
