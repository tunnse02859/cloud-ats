/**
 * 
 */
package org.ats.services.organization.event;

import java.util.logging.Logger;

import org.ats.services.organization.AbstractTestCase;
import org.ats.services.organization.RoleService;
import org.ats.services.organization.SpaceService;
import org.ats.services.organization.TenantService;
import org.ats.services.organization.UserService;
import org.ats.services.organization.entity.Role;
import org.ats.services.organization.entity.Space;
import org.ats.services.organization.entity.Tenant;
import org.ats.services.organization.entity.User;
import org.ats.services.organization.entity.fatory.PermissionFactory;
import org.ats.services.organization.entity.fatory.ReferenceFactory;
import org.ats.services.organization.entity.fatory.RoleFactory;
import org.ats.services.organization.entity.fatory.SpaceFactory;
import org.ats.services.organization.entity.fatory.TenantFactory;
import org.ats.services.organization.entity.fatory.UserFactory;
import org.ats.services.organization.entity.reference.RoleReference;
import org.ats.services.organization.entity.reference.SpaceReference;
import org.ats.services.organization.entity.reference.TenantReference;
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

  private Role admin;
  private Role tester;
  
  private void initData() {
    Tenant tenant = tenantFactory.create("Fsoft");
    tenantService.create(tenant);
    
    Space space = spaceFactory.create("FSU1.BU11");
    space.setTenant(tenantRefFactory.create(tenant.getId()));
    
    admin = roleFactory.create("admin");
    admin.setSpace(spaceRefFactory.create(space.getId()));
    admin.addPermission(permFactory.create("*:*@Fsoft:*"));
    
    tester = roleFactory.create("tester");
    tester.setSpace(spaceRefFactory.create(space.getId()));
    tester.addPermission(permFactory.create("test:*@Fsoft:" + space.getId()));
    
    space.addRole(roleRefFactory.create(admin.getId()), roleRefFactory.create(tester.getId()));
    spaceService.create(space);
    roleService.create(admin, tester);
    
    User user = userFactory.create("haint@cloud-ats.net", "Hai", "Nguyen");
    user.setTenant(tenantRefFactory.create(tenant.getId()));
    user.joinSpace(spaceRefFactory.create(space.getId()));
    user.addRole(roleRefFactory.create(admin.getId()), roleRefFactory.create(tester.getId()));
    userService.create(user);
  }
}
