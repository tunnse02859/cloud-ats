/**
 * 
 */
package org.ats.services.organization;

import org.ats.services.organization.entity.Feature;
import org.ats.services.organization.entity.Feature.Action;
import org.ats.services.organization.entity.Role;
import org.ats.services.organization.entity.Role.Permission;
import org.ats.services.organization.entity.fatory.PermissionFactory;
import org.ats.services.organization.entity.fatory.ReferenceFactory;
import org.ats.services.organization.entity.fatory.RoleFactory;
import org.ats.services.organization.entity.reference.SpaceReference;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.inject.Key;
import com.google.inject.TypeLiteral;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Mar 16, 2015
 */
public class RoleServiceTestCase extends AbstractTestCase {
  
  private RoleService service;
  
  private RoleFactory factory;
  
  private PermissionFactory permFactory;
  
  private ReferenceFactory<SpaceReference> spaceFactory;

  @Override
  @BeforeMethod
  public void init() throws Exception {
    super.init();
    this.service = injector.getInstance(RoleService.class);
    this.factory = injector.getInstance(RoleFactory.class);
    this.permFactory = injector.getInstance(PermissionFactory.class);
    this.spaceFactory = injector.getInstance(Key.get(new TypeLiteral<ReferenceFactory<SpaceReference>>(){}));
  }
  
  @Test
  public void testCRUD() {
    Role role = factory.create("Role 1");
    try {
      role.addPermission(permFactory.create("feature1:action1@tenant:space"), 
          permFactory.create("*:*@tenant:space"), permFactory.create("feature1:action1@tenant:*"));
      Assert.fail();
    } catch (IllegalStateException e) {
      
      role.setSpace(spaceFactory.create("space"));
      
      role.addPermission(permFactory.create("feature1:action1@tenant:space"), 
          permFactory.create("*:*@tenant:space"), permFactory.create("feature1:action1@tenant:*"));
    }
    service.create(role);
    
    role = service.get(role.getId());
    Assert.assertEquals(3, role.getPermissions().size());
    
    role.removePermission(permFactory.create("feature1:action1@tenant:space"));
    service.update(role);
    role = service.get(role.getId());
    Assert.assertEquals(2, role.getPermissions().size());
    
    Permission perm = permFactory.create("*:*@tenant:space");
    Assert.assertEquals(Feature.ANY, perm.getFeature());
    Assert.assertEquals(Action.ANY, perm.getAction());
    Assert.assertEquals("tenant", perm.getTenant().getId());
    Assert.assertEquals("space", perm.getSpace().getId());
    Assert.assertTrue(role.getPermissions().contains(perm));

    service.delete(role.getId());
    Assert.assertEquals(0, service.count());
    Assert.assertNull(service.get(role.getId()));
  }
}
