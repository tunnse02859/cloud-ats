/**
 * 
 */
package org.ats.services.organization;

import java.util.List;

import org.ats.common.PageList;
import org.ats.services.organization.entity.Feature;
import org.ats.services.organization.entity.Feature.Action;
import org.ats.services.organization.entity.Role;
import org.ats.services.organization.entity.Role.Permission;
import org.ats.services.organization.entity.fatory.PermissionFactory;
import org.ats.services.organization.entity.fatory.ReferenceFactory;
import org.ats.services.organization.entity.fatory.RoleFactory;
import org.ats.services.organization.entity.reference.RoleReference;
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

  private ReferenceFactory<RoleReference> roleRef; 
  @Override
  @BeforeMethod
  public void init() throws Exception {
    super.init();
    this.service = injector.getInstance(RoleService.class);
    this.factory = injector.getInstance(RoleFactory.class);
    this.permFactory = injector.getInstance(PermissionFactory.class);
    this.spaceFactory = injector.getInstance(Key.get(new TypeLiteral<ReferenceFactory<SpaceReference>>(){}));
    this.roleRef = injector.getInstance(Key.get(new TypeLiteral<ReferenceFactory<RoleReference>>(){}));
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
  
  @Test
  public void testList() {
    initRole(32);
    Assert.assertEquals(32, service.count());
    
    PageList<Role> list = service.list();
    Assert.assertEquals(32, list.count());
    
    Assert.assertEquals(4, list.totalPage());
    
    List<Role> roles = list.getPage(4);
    
    Assert.assertEquals(2, roles.size());
    
    Assert.assertEquals("role31", roles.get(0).getName());
    
    Role role = factory.create("role_test");
    
    service.create(role);
    
    Assert.assertEquals(33, service.count());
    
    Assert.assertTrue(service.get(role.getId()).getPermissions().isEmpty());
    
  }
  
  @Test
  public void testReference() {
    Role role = factory.create("test reference");
    SpaceReference ref = spaceFactory.create("space");
    role.setSpace(ref);
    
    service.create(role);
    
    RoleReference rolRef = roleRef.create(role.getId());
    
    role = rolRef.get();
    Assert.assertEquals("test reference", role.getName());
  }
  
  private void initRole(int total) {
    for (int i =1; i <= total; i ++) {
      
      String roleName = "role" + i;
      Role role = factory.create(roleName);
      
      SpaceReference refSpace1 = spaceFactory.create("space1");
      SpaceReference refSpace2 = spaceFactory.create("space2");
      
      if (i < 20) {
        role.setSpace(refSpace1);
        role.addPermission(permFactory.create("feature1:action1@tenant:space1"), permFactory.create("feature3:action1@tenant:*"));
      } else {
        role.setSpace(refSpace2);
        role.addPermission(permFactory.create("feature2:action2@tenant:space2"), permFactory.create("feature3:action2@tenant:*"));
      }
      
      service.create(role);
    }
  }
  
}
