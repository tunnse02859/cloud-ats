/**
 * 
 */
package org.ats.services.organization.acl;

import org.ats.services.OrganizationContext;
import org.ats.services.OrganizationServiceModule;
import org.ats.services.data.DatabaseModule;
import org.ats.services.data.MongoDBService;
import org.ats.services.organization.FeatureService;
import org.ats.services.organization.RoleService;
import org.ats.services.organization.SpaceService;
import org.ats.services.organization.TenantService;
import org.ats.services.organization.UserService;
import org.ats.services.organization.base.AuthenticationService;
import org.ats.services.organization.entity.Feature;
import org.ats.services.organization.entity.Feature.Action;
import org.ats.services.organization.entity.Role;
import org.ats.services.organization.entity.Space;
import org.ats.services.organization.entity.Tenant;
import org.ats.services.organization.entity.User;
import org.ats.services.organization.entity.fatory.FeatureFactory;
import org.ats.services.organization.entity.fatory.FeatureReferenceFactory;
import org.ats.services.organization.entity.fatory.PermissionFactory;
import org.ats.services.organization.entity.fatory.RoleFactory;
import org.ats.services.organization.entity.fatory.RoleReferenceFactory;
import org.ats.services.organization.entity.fatory.SpaceFactory;
import org.ats.services.organization.entity.fatory.SpaceReferenceFactory;
import org.ats.services.organization.entity.fatory.TenantFactory;
import org.ats.services.organization.entity.fatory.TenantReferenceFactory;
import org.ats.services.organization.entity.fatory.UserFactory;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Mar 16, 2015
 */
public class InterceptorTestCase {
  
  /** .*/
  private FeatureService featureService;
  private FeatureFactory featureFactory;
  private FeatureReferenceFactory featureRefFactory;
  
  /** .*/
  private TenantService tenantService;
  private TenantFactory tenantFactory;
  private TenantReferenceFactory tenantRefFactory;
  
  /** .*/
  private SpaceService spaceService;
  private SpaceFactory spaceFactory;
  private SpaceReferenceFactory spaceRefFactory;
  
  /** .*/
  private UserService userService;
  private UserFactory userFactory;
  
  /** .*/
  private RoleService roleService;
  private RoleFactory roleFactory;
  private RoleReferenceFactory roleRefFactory;
  private PermissionFactory permFactory;
  
  /** .*/
  OrganizationContext context;
  
  /** .*/
  private Tenant tenant;
  
  /** .*/
  private Space space;
  
  /** .*/
  private User user;
  
  /** .*/
  private AuthenticationService authService;
  
  /** .*/
  private MongoDBService mongoService;
  
  /** .*/
  protected Injector injector;
  
  /** .*/
  private MockService service;

  @BeforeMethod
  public void init() throws Exception {
    Injector injector = Guice.createInjector(new MockModule(), new DatabaseModule(), new OrganizationServiceModule());
    this.injector = injector;
    
    this.mongoService = injector.getInstance(MongoDBService.class);
    this.mongoService.dropDatabase();
    
    this.service = injector.getInstance(MockService.class);
    
    this.featureService = this.injector.getInstance(FeatureService.class);
    this.featureFactory = this.injector.getInstance(FeatureFactory.class);
    this.featureRefFactory = this.injector.getInstance(FeatureReferenceFactory.class);
    
    this.tenantService = this.injector.getInstance(TenantService.class);
    this.tenantFactory = this.injector.getInstance(TenantFactory.class);
    this.tenantRefFactory = this.injector.getInstance(TenantReferenceFactory.class);
    
    this.spaceService = this.injector.getInstance(SpaceService.class);
    this.spaceFactory = this.injector.getInstance(SpaceFactory.class);
    this.spaceRefFactory = this.injector.getInstance(SpaceReferenceFactory.class);
    
    this.roleService = this.injector.getInstance(RoleService.class);
    this.roleFactory = this.injector.getInstance(RoleFactory.class);
    this.roleRefFactory = this.injector.getInstance(RoleReferenceFactory.class);
    this.permFactory = this.injector.getInstance(PermissionFactory.class);
    
    this.userService = this.injector.getInstance(UserService.class);
    this.userFactory = this .injector.getInstance(UserFactory.class);
    
    this.context = this.injector.getInstance(OrganizationContext.class);
    
    this.authService = this.injector.getInstance(Key.get(AuthenticationService.class, Names.named("Mongo")));
    
    Feature foo = featureFactory.create("fooFeature");
    foo.addAction(new Action("fooAction"));
    this.featureService.create(foo);
    
    Feature bar = featureFactory.create("barFeature");
    bar.addAction(new Action("barAction"));
    this.featureService.create(bar);
    
    this.tenant = tenantFactory.create("fsoft");
    this.tenant.addFeature(featureRefFactory.create(foo.getId()), featureRefFactory.create(bar.getId()));
    this.tenantService.create(this.tenant);
    
    this.space = spaceFactory.create("FSU1.BU11");
    this.space.setTenant(tenantRefFactory.create(this.tenant.getId()));
    this.spaceService.create(this.space);
    
    Role fooRole = roleFactory.create("fooRole");
    fooRole.setSpace(spaceRefFactory.create(this.space.getId()));
    fooRole.addPermission(permFactory.create("fooFeature:fooAction@fsoft:" + this.space.getId()));
    this.roleService.create(fooRole);
    
    this.user = userFactory.create("haint@cloud-ats.net", "Hai", "Nguyen");
    this.user.setTenant(tenantRefFactory.create(this.tenant.getId()));
    this.user.joinSpace(spaceRefFactory.create(this.space.getId()));
    this.user.setPassword("12345");
    this.user.addRole(roleRefFactory.create(fooRole.getId()));
    
    this.userService.create(this.user);
  }
  
  @AfterMethod
  public void tearDown() {
    this.authService.logOut();
  }

  @Test
  public void testFoo() throws Exception {
    try {
      this.service.foo();
      Assert.fail();
    } catch (UnAuthorizationException e) {
      Assert.fail();
    } catch (UnAuthenticatedException e) {
    }
    
    this.authService.logIn("haint@cloud-ats.net", "12345");
    this.spaceService.goTo(spaceRefFactory.create(space.getId()));
    Assert.assertEquals("foo", this.service.foo());
  }
  
  @Test
  public void testBar() throws Exception {
    try {
      this.service.bar();
      Assert.fail();
    } catch (UnAuthorizationException e) {
      Assert.fail();
    } catch (UnAuthenticatedException e) {
    }
    
    this.authService.logIn("haint@cloud-ats.net", "12345");
    this.spaceService.goTo(spaceRefFactory.create(space.getId()));
    
    try {
      this.service.bar();
      Assert.fail();
    } catch (UnAuthorizationException e) {
    } catch (UnAuthenticatedException e) {
      Assert.fail();
    }
    
    Role barRole = roleFactory.create("barRole");
    barRole.setSpace(spaceRefFactory.create(this.space.getId()));
    barRole.addPermission(permFactory.create("barFeature:barAction@*:*"));
    roleService.create(barRole);
//    
//    this.user.addRole(roleRefFactory.create(barRole.getId()));
//    this.userService.update(this.user);
//    this.context.setUser(this.user);
//    
//    Assert.assertEquals("bar", this.service.bar());
  }
}
