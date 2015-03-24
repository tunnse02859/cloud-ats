/**
 * 
 */
package org.ats.services.organization;

import org.ats.services.OrganizationContext;
import org.ats.services.organization.base.AuthenticationService;
import org.ats.services.organization.entity.Space;
import org.ats.services.organization.entity.Tenant;
import org.ats.services.organization.entity.User;
import org.ats.services.organization.entity.fatory.ReferenceFactory;
import org.ats.services.organization.entity.fatory.SpaceFactory;
import org.ats.services.organization.entity.fatory.TenantFactory;
import org.ats.services.organization.entity.fatory.UserFactory;
import org.ats.services.organization.entity.reference.SpaceReference;
import org.ats.services.organization.entity.reference.TenantReference;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.inject.Key;
import com.google.inject.TypeLiteral;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Mar 16, 2015
 */
public class OrganizationContextTestCase extends AbstractTestCase {
  
  private TenantService tenantService;
  
  private TenantFactory tenantFactory;
  
  private ReferenceFactory<TenantReference> tenantRefFactory;
  
  private SpaceService spaceService;
  
  private SpaceFactory spaceFactory;
  
  private ReferenceFactory<SpaceReference> spaceRefFactory;
  
  private UserService userService;
  
  private UserFactory userFactory;
  
  private OrganizationContext context;
  
  private Tenant tenant;
  
  private Space space;
  
  private User user;
  
  private AuthenticationService<User> authService;
  
  @Override
  @BeforeClass
  public void init() throws Exception {
    super.init();
    this.tenantService = this.injector.getInstance(TenantService.class);
    this.tenantFactory = this.injector.getInstance(TenantFactory.class);
    this.tenantRefFactory = this.injector.getInstance(Key.get(new TypeLiteral<ReferenceFactory<TenantReference>>(){}));
    
    this.spaceService = this.injector.getInstance(SpaceService.class);
    this.spaceFactory = this.injector.getInstance(SpaceFactory.class);
    this.spaceRefFactory = this.injector.getInstance(Key.get(new TypeLiteral<ReferenceFactory<SpaceReference>>(){}));
    
    this.userService = this.injector.getInstance(UserService.class);
    this.userFactory = this .injector.getInstance(UserFactory.class);
    
    this.context = this.injector.getInstance(OrganizationContext.class);
    
    this.authService = this.injector.getInstance(Key.get(new TypeLiteral<AuthenticationService<User>>(){}));
    
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
  
  @Test(threadPoolSize = 2, invocationCount = 2, timeOut = 1000)
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
}
