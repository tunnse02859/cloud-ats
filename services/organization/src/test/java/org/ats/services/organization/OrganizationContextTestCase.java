/**
 * 
 */
package org.ats.services.organization;

import org.ats.services.OrganizationContext;
import org.ats.services.organization.base.AuthenticationService;
import org.ats.services.organization.entity.Feature;
import org.ats.services.organization.entity.Role;
import org.ats.services.organization.entity.Space;
import org.ats.services.organization.entity.Tenant;
import org.ats.services.organization.entity.User;
import org.ats.services.organization.entity.Feature.Action;
import org.ats.services.organization.entity.fatory.FeatureFactory;
import org.ats.services.organization.entity.fatory.ReferenceFactory;
import org.ats.services.organization.entity.fatory.RoleFactory;
import org.ats.services.organization.entity.fatory.SpaceFactory;
import org.ats.services.organization.entity.fatory.TenantFactory;
import org.ats.services.organization.entity.fatory.UserFactory;
import org.ats.services.organization.entity.reference.FeatureReference;
import org.ats.services.organization.entity.reference.RoleReference;
import org.ats.services.organization.entity.reference.SpaceReference;
import org.ats.services.organization.entity.reference.TenantReference;
import org.ats.services.organization.event.AbstractEventTestCase;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.inject.Key;
import com.google.inject.TypeLiteral;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Mar 16, 2015
 */
public class OrganizationContextTestCase extends AbstractEventTestCase {
  
  private OrganizationContext context;
  
  private Tenant tenant;
  
  private Space space;
  
  private User user;
  
  private AuthenticationService<User> authService;
  
  @BeforeClass
  public void init() throws Exception {
    super.init(this.getClass().getSimpleName());
    this.tenantService = this.injector.getInstance(TenantService.class);
    this.tenantFactory = this.injector.getInstance(TenantFactory.class);
    this.tenantRefFactory = this.injector.getInstance(Key.get(new TypeLiteral<ReferenceFactory<TenantReference>>(){}));
    this.featureFactory = this.injector.getInstance(FeatureFactory.class);
    this.roleFactory = this.injector.getInstance(RoleFactory.class);
    
    this.spaceService = this.injector.getInstance(SpaceService.class);
    this.featureService = this.injector.getInstance(FeatureService.class);
    
    this.spaceFactory = this.injector.getInstance(SpaceFactory.class);
    this.spaceRefFactory = this.injector.getInstance(Key.get(new TypeLiteral<ReferenceFactory<SpaceReference>>(){}));
    this.featureRefFactory = this.injector.getInstance(Key.get(new TypeLiteral<ReferenceFactory<FeatureReference>>(){}));
    this.roleRefFactory = this.injector.getInstance(Key.get(new TypeLiteral<ReferenceFactory<RoleReference>>(){}));
    
    this.userService = this.injector.getInstance(UserService.class);
    this.userFactory = this .injector.getInstance(UserFactory.class);
    
    this.context = this.injector.getInstance(OrganizationContext.class);
    
    this.authService = this.injector.getInstance(Key.get(new TypeLiteral<AuthenticationService<User>>(){}));
  }
  
  @AfterClass
  public void shutdown() throws Exception {
    this.eventService.stop();
    this.mongoService.dropDatabase();
  }
  
  @BeforeMethod
  public void initData() {
    this.tenant = tenantFactory.create("Fsoft");
    this.tenantService.create(this.tenant);
    
    this.space = spaceFactory.create("FSU1.BU11");
    this.space.setTenant(tenantRefFactory.create(this.tenant.getId()));
    this.spaceService.create(this.space);
    
    this.user = userFactory.create("haint@cloud-ats.net", "Hai", "Nguyen");
    this.user.setTenant(tenantRefFactory.create(this.tenant.getId()));
    this.user.joinSpace(spaceRefFactory.create(this.space.getId()));
    this.user.setPassword("12345");
    this.userService.create(this.user);
  }
  
  @AfterMethod
  public void tearDown() {
    this.authService.logOut();
    this.mongoService.dropDatabase();
  }
  
  @Test
  public void testLoginAndLogout() {
    
    Assert.assertNull(this.context.getUser());
    Assert.assertNull(this.context.getSpace());
    Assert.assertNull(this.context.getTenant());
    
    this.authService.logIn("haint@cloud-ats.net", "12345");
    this.spaceService.goTo(spaceRefFactory.create(space.getId()));
    
    Assert.assertNotNull(this.context.getUser());
    Assert.assertNotNull(this.context.getSpace());
    Assert.assertNotNull(this.context.getTenant());
    
    User user = this.authService.logOut();
    
    Assert.assertNull(this.context.getUser());
    Assert.assertNull(this.context.getSpace());
    Assert.assertNull(this.context.getTenant());
    Assert.assertEquals("haint@cloud-ats.net", user.getEmail());
  }
  
  @Test
  public void testGotoSpace() {
    this.authService.logIn("haint@cloud-ats.net", "12345");
    
    try {
      this.spaceService.goTo(null);
      Assert.fail();
    } catch (NullPointerException e) {
      
    }
    
    Assert.assertNull(this.spaceService.goTo(spaceRefFactory.create("foo")));
    OrganizationContext context = this.spaceService.goTo(spaceRefFactory.create(this.space.getId()));
    Assert.assertEquals(this.context.getSpace().getId(), context.getSpace().getId());
  }
  
  @Test
  public void testUserActivation() {
    Assert.assertNull(this.context.getTenant());
    Assert.assertNull(this.context.getSpace());
    Assert.assertNull(this.context.getUser());
    
    User user = userService.get("haint@cloud-ats.net");
    Assert.assertNotNull(this.authService.logIn("haint@cloud-ats.net", "12345"));
    Assert.assertNotNull(this.context.getTenant());
    Assert.assertNotNull(this.context.getUser());
    
    userService.inActive(user);
    Assert.assertNull(this.context.getUser());
    Assert.assertNull(this.context.getTenant());
    Assert.assertNull(this.authService.logIn("haint@cloud-ats.net", "12345"));
    userService.active(user);
    
    Assert.assertNotNull(this.authService.logIn("haint@cloud-ats.net", "12345"));
    Assert.assertNotNull(this.context.getTenant());
    Assert.assertNotNull(this.context.getUser());
    
    userService.delete(user);
    Assert.assertNull(this.context.getTenant());
    Assert.assertNull(this.context.getSpace());
    Assert.assertNull(this.context.getUser());
  }
  
  @Test
  public void testSpaceActivation() {
    
    Assert.assertNull(context.getSpace());
    this.authService.logIn("haint@cloud-ats.net", "12345");
    this.spaceService.goTo(spaceRefFactory.create(this.space.getId()));
    Assert.assertNotNull(context.getSpace());
    
    spaceService.inActive(this.space);
    Assert.assertNull(context.getSpace());
    Assert.assertNull(this.spaceService.goTo(spaceRefFactory.create(this.space.getId())));
    
    spaceService.active(this.space);
    Assert.assertNotNull(this.spaceService.goTo(spaceRefFactory.create(this.space.getId())));
    Assert.assertNotNull(context.getSpace());
    
    spaceService.delete(this.space);
    Assert.assertNull(context.getSpace());
  }
  
  @Test
  public void testTenantActivation() {
    Assert.assertNull(this.context.getTenant());
    Assert.assertNull(this.context.getSpace());
    Assert.assertNull(this.context.getUser());
    
    Assert.assertNotNull(this.authService.logIn("haint@cloud-ats.net", "12345"));
    Assert.assertNotNull(this.context.getTenant());
    Assert.assertNotNull(this.context.getUser());
    
    tenantService.inActive(tenant);
    Assert.assertNull(this.context.getTenant());
    Assert.assertNull(this.context.getUser());
    
    waitToFinishActivationTenant(tenantRefFactory.create(tenant.getId()), true);
    
    Assert.assertNull(this.authService.logIn("haint@cloud-ats.net", "12345"));
    
    tenantService.active(tenant);
    waitToFinishActivationTenant(tenantRefFactory.create(tenant.getId()), false);
    Assert.assertNotNull(this.authService.logIn("haint@cloud-ats.net", "12345"));
    
    tenantService.delete(tenant);
    Assert.assertNull(this.context.getTenant());
    Assert.assertNull(this.context.getSpace());
    Assert.assertNull(this.context.getUser());
  }
  
  @Test
  public void testUpdateUser() {
    
    this.authService.logIn("haint@cloud-ats.net", "12345");
    this.spaceService.goTo(spaceRefFactory.create(this.space.getId()));
    
    User user = userService.get("haint@cloud-ats.net");
    user.setPassword("trinhtran");
    Assert.assertEquals(context.getUser().getPassword(), "12345");
    userService.update(user);
    
    Assert.assertEquals(context.getUser().getPassword(), "trinhtran");
    Assert.assertEquals(context.getUser().getPassword(), userService.get("haint@cloud-ats.net").getPassword());
  }
  
  @Test
  public void testUpdateTenant() {
    
    this.authService.logIn("haint@cloud-ats.net", "12345");
    this.spaceService.goTo(spaceRefFactory.create(this.space.getId()));
    
    Tenant tenant = tenantService.get("Fsoft");
    Assert.assertEquals(context.getTenant().getFeatures().size(), 0);
    
    Feature feature1 = featureFactory.create("Dev");
    feature1.addAction(new Action("fooAction"), new Action("barAction"));
    featureService.create(feature1);
    tenant.addFeature(featureRefFactory.create(feature1.getId()));
    
    tenantService.update(tenant);
    Assert.assertEquals(context.getTenant().getFeatures().size(), 1);
  }
  
  @Test
  public void testUpdateSpace() {
    this.authService.logIn("haint@cloud-ats.net", "12345");
    this.spaceService.goTo(spaceRefFactory.create(this.space.getId()));
    
    Space space = spaceService.get(this.space.getId());
    Role role1 = roleFactory.create("role1");
    
    Assert.assertEquals(context.getSpace().getRoles().size(), 0);
    space.addRole(roleRefFactory.create(role1.getId()));
    
    spaceService.update(space);
    Assert.assertEquals(context.getSpace().getRoles().size(), 1);
    
  }
}
