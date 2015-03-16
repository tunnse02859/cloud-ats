/**
 * 
 */
package org.ats.services.organization;

import org.ats.services.organization.entity.Space;
import org.ats.services.organization.entity.Tenant;
import org.ats.services.organization.entity.User;
import org.ats.services.organization.entity.fatory.SpaceFactory;
import org.ats.services.organization.entity.fatory.SpaceReferenceFactory;
import org.ats.services.organization.entity.fatory.TenantFactory;
import org.ats.services.organization.entity.fatory.TenantReferenceFactory;
import org.ats.services.organization.entity.fatory.UserFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Mar 16, 2015
 */
public class OrganizationContextTestCase extends AbstractTestCase {
  
  private TenantService tenantService;
  
  private TenantFactory tenantFactory;
  
  private TenantReferenceFactory tenantRefFactory;
  
  private SpaceService spaceService;
  
  private SpaceFactory spaceFactory;
  
  private SpaceReferenceFactory spaceRefFactory;
  
  private UserService userService;
  
  private UserFactory userFactory;
  
  private OrganizationContext context;
  
  private Tenant tenant;
  
  private Space space;
  
  private User user;
  
  @Override
  @BeforeClass
  public void init() throws Exception {
    super.init();
    this.tenantService = this.injector.getInstance(TenantService.class);
    this.tenantFactory = this.injector.getInstance(TenantFactory.class);
    this.tenantRefFactory = this.injector.getInstance(TenantReferenceFactory.class);
    
    this.spaceService = this.injector.getInstance(SpaceService.class);
    this.spaceFactory = this.injector.getInstance(SpaceFactory.class);
    this.spaceRefFactory = this.injector.getInstance(SpaceReferenceFactory.class);
    
    this.userService = this.injector.getInstance(UserService.class);
    this.userFactory = this .injector.getInstance(UserFactory.class);
    
    this.context = this.injector.getInstance(OrganizationContext.class);
    
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
  
  @Test(threadPoolSize = 10, invocationCount = 10, timeOut = 1000)
  public void testLoginAndLogout() {
    
    Assert.assertNull(this.context.getUser());
    Assert.assertNull(this.context.getSpace());
    Assert.assertNull(this.context.getTenant());
    
    this.userService.logIn("haint@cloud-ats.net", "12345");
    this.spaceService.goTo(this.space);
    
    Assert.assertNotNull(this.context.getUser());
    Assert.assertNotNull(this.context.getSpace());
    Assert.assertNotNull(this.context.getTenant());
    
    User user = this.userService.logOut();
    
    Assert.assertNull(this.context.getUser());
    Assert.assertNull(this.context.getSpace());
    Assert.assertNull(this.context.getTenant());
    Assert.assertEquals("haint@cloud-ats.net", user.getEmail());
  }
}
