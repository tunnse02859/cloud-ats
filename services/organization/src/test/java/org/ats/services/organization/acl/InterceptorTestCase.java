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
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Mar 16, 2015
 */
public class InterceptorTestCase {
  
  /** .*/
  private FeatureService featureService;
  private FeatureFactory featureFactory;
  private ReferenceFactory<FeatureReference> featureRefFactory;
  
  /** .*/
  private TenantService tenantService;
  private TenantFactory tenantFactory;
  private ReferenceFactory<TenantReference> tenantRefFactory;
  
  /** .*/
  private SpaceService spaceService;
  private SpaceFactory spaceFactory;
  private ReferenceFactory<SpaceReference> spaceRefFactory;
  
  /** .*/
  private UserService userService;
  private UserFactory userFactory;
  
  /** .*/
  private RoleService roleService;
  private RoleFactory roleFactory;
  private ReferenceFactory<RoleReference> roleRefFactory;
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
  private AuthenticationService<User> authService;
  
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
    this.featureRefFactory = this.injector.getInstance(Key.get(new TypeLiteral<ReferenceFactory<FeatureReference>>(){}));
    
    this.tenantService = this.injector.getInstance(TenantService.class);
    this.tenantFactory = this.injector.getInstance(TenantFactory.class);
    this.tenantRefFactory = this.injector.getInstance(Key.get(new TypeLiteral<ReferenceFactory<TenantReference>>(){}));
    
    this.spaceService = this.injector.getInstance(SpaceService.class);
    this.spaceFactory = this.injector.getInstance(SpaceFactory.class);
    this.spaceRefFactory = this.injector.getInstance(Key.get(new TypeLiteral<ReferenceFactory<SpaceReference>>(){}));
    
    this.roleService = this.injector.getInstance(RoleService.class);
    this.roleFactory = this.injector.getInstance(RoleFactory.class);
    this.roleRefFactory = this.injector.getInstance(Key.get(new TypeLiteral<ReferenceFactory<RoleReference>>(){}));
    this.permFactory = this.injector.getInstance(PermissionFactory.class);
    
    this.userService = this.injector.getInstance(UserService.class);
    this.userFactory = this .injector.getInstance(UserFactory.class);
    
    this.context = this.injector.getInstance(OrganizationContext.class);
    
    this.authService = this.injector.getInstance(Key.get(new TypeLiteral<AuthenticationService<User>>(){}));
    
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
      //
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
      //
    } catch (UnAuthenticatedException e) {
      Assert.fail();
    }
    
    Role barRole = roleFactory.create("barRole");
    barRole.setSpace(spaceRefFactory.create(this.space.getId()));
    barRole.addPermission(permFactory.create("barFeature:barAction@*:*"));
    roleService.create(barRole);
//    
    this.user.addRole(roleRefFactory.create(barRole.getId()));
    this.userService.update(this.user);
    this.context.setUser(this.user);
    
    Assert.assertEquals("bar", this.service.bar());
  }
  
  /**
   * testPublic to test with user has no roles
   */
  
  @Test
  public void testPublic () {
    
    try {
      this.service.publicMethod();
      Assert.fail();
    } catch (UnAuthorizationException e) {
      Assert.fail();
    } catch (UnAuthenticatedException e) {
      
    }
    
    this.authService.logIn("haint@cloud-ats.net", "12345");
    this.spaceService.goTo(spaceRefFactory.create(space.getId()));
    
    Assert.assertEquals("public", this.service.publicMethod());
    
    Tenant tenant = tenantFactory.create("cloudTeam");
    
    this.user.setTenant(tenantRefFactory.create(tenant.getId()));
    
    userService.update(this.user);
    
    Assert.assertEquals("public", this.service.publicMethod());
    
    
  }
  
  @Test
  public void testTenant() {
    
    System.out.println("Test Tenant ");
    createUser("viettel", "dev", "featureViettel", "actionViettel", "roleViettel", "featureViettel:barAction@*:*", "tuanhq_vt@viettel", "Tuan", "Hoang", "tuanhq");
   
    try {
      
      this.service.bar();
      Assert.fail();
    } catch (UnAuthorizationException e) {
      Assert.fail();
    } catch (UnAuthenticatedException e) {
      
    }
    
    this.authService.logIn("tuanhq_vt@viettel", "tuanhq");
    
    try {
      this.service.bar();
      Assert.fail();
    } catch (UnAuthorizationException e) {
      
    } catch (UnAuthenticatedException e) {
      Assert.fail();
    }
    
    Assert.assertEquals("viettel", this.service.viettel());
    Assert.assertEquals("public", this.service.publicMethod());
  }
  
  
  @Test
  public void testFeature() {
    
    System.out.println("Test Feature ");
    createUser("viettel", "dev", "featureViettel", "actionViettel", "roleViettel", "featureViettel:barAction@*:*", "trinhtv3@viettel", "trinh", "tran", "trinhtran");
    
    try {
      
      this.service.viettel();
      Assert.fail();
    } catch (UnAuthorizationException e) {
      Assert.fail();
    } catch (UnAuthenticatedException e) {
      
    }
    
    this.authService.logIn("trinhtv3@viettel", "trinhtran");
    
    try {
      this.service.viettel1();
      Assert.fail();
    } catch (UnAuthorizationException e) {
      
    } catch (UnAuthenticatedException e) {
      Assert.fail();
    }
    
    Assert.assertEquals("public", this.service.publicMethod());
  }
  
  @Test
  public void testDefaultTenant() {
    
    createUser("viettel", "dev", "featureViettel", "actionViettel", "roleViettel", "featureViettel:actionViettel@*:*", "trinhtv3@viettel", "trinh", "tran", "trinhtran");
    this.authService.logIn("trinhtv3@viettel", "trinhtran");
    
    Assert.assertEquals("viettelDefault", this.service.viettelDefaultTenant());
    
    this.user.setTenant(tenantRefFactory.create("mobi"));
    this.userService.update(this.user);
    
    Assert.assertEquals("viettelDefault", this.service.viettelDefaultTenant());
    
  }
  
  @Test
  public void testDefaultAction() {
    
    createUser("viettel", "dev", "featureViettel", "actionViettel", "roleViettel", "featureViettel:barAction@*:*", "tuanhq_vt@viettel", "Tuan", "Hoang", "tuanhq");
    
    this.authService.logIn("tuanhq_vt@viettel", "tuanhq");
    
    Assert.assertEquals("DefaultAction", this.service.viettelDefaultAction());
    User user = this.userService.get("tuanhq_vt@viettel");

    user.setTenant(tenantRefFactory.create("mobi"));
    this.userService.update(user);
    try {
      this.service.viettelDefaultAction();
      Assert.fail();
    } catch (UnAuthorizationException e) {
      
    } catch (UnAuthenticatedException e) {
      Assert.fail();
    }
   
  }
  
  @Test
  public void testDefaultFeature() {
    
    createUser("viettel", "dev", "featureViettel", "actionViettel", "roleViettel", "featureViettel:actionViettel@*:*", "tuanhq_vt@viettel", "Tuan", "Hoang", "tuanhq");
    
    this.authService.logIn("tuanhq_vt@viettel", "tuanhq");
    Assert.assertEquals("DefaultFeature", this.service.viettelDefaultFeature());
    
    Tenant  tenant = this.tenantService.get("viettel");
    
    tenant.addFeature(featureRefFactory.create("featureMobi"));
    tenant.removeFeature(featureRefFactory.create("featureViettel"));
    this.tenantService.update(tenant);
    
    Assert.assertEquals("DefaultFeature", this.service.viettelDefaultFeature());
    
  }
  
  @Test
  public void testSpace() {
    
    createUser("viettel", "dev", "featureViettel", "barAction", "roleViettel", "featureViettel:barAction@*:*", "tuanhq_vt@viettel", "Tuan", "Hoang", "tuanhq");
    this.authService.logIn("tuanhq_vt@viettel", "tuanhq");
    
    Assert.assertEquals("space", this.service.defaultspace());
    User user = userService.get("tuanhq_vt@viettel");
    user.leaveSpace(spaceRefFactory.create("dev"));
    user.joinSpace(spaceRefFactory.create("test"));
    
    userService.update(user);
    System.out.println(user.getSpaces().size());
    Assert.assertEquals("space", this.service.defaultspace());
  }
  
  @Test
  public void testAction() {
    
    System.out.println("Test Action");
    createUser("viettel", "dev", "featureViettel", "actionViettel", "roleViettel", "featureViettel:barAction@*:*", "tuanhq_vt@viettel", "Tuan", "Hoang", "tuanhq");
    
    try {
      
      this.service.viettel3();
      Assert.fail();
    } catch (UnAuthorizationException e) {
      Assert.fail();
    } catch (UnAuthenticatedException e) {
      
    }
    
    this.authService.logIn("tuanhq_vt@viettel", "tuanhq");
    
    Assert.assertEquals("DefaultAction", this.service.viettelDefaultAction());
  }
  
  public void createUser(String tenantId, String spaceId, String featureName, String actionName, String roleName, String perm, String userName,String firstName, String lastName, String pass) {
    
    Feature foo = featureFactory.create(featureName);
    foo.addAction(new Action(actionName),new Action("fooAction"), new Action("barAction"));
    this.featureService.create(foo);
    
    Tenant tenant = tenantFactory.create(tenantId);
    tenant.addFeature(featureRefFactory.create(foo.getId()));
    this.tenantService.create(tenant);
    
    Space space = spaceFactory.create(spaceId);
    space.setTenant(tenantRefFactory.create(tenant.getId()));
    this.spaceService.create(space);
    
    Role fooRole = roleFactory.create(roleName);
    fooRole.setSpace(spaceRefFactory.create(space.getId()));
    fooRole.addPermission(permFactory.create(perm));
    this.roleService.create(fooRole);
    
    User user = userFactory.create(userName, firstName, lastName);
    user.setTenant(tenantRefFactory.create(tenant.getId()));
    user.joinSpace(spaceRefFactory.create(space.getId()));
    user.setPassword(pass);
    user.addRole(roleRefFactory.create(fooRole.getId()));
    
    this.userService.create(user);
    
  }
  
}
