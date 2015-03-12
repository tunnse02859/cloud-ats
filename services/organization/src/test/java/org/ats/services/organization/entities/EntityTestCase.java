/**
 * 
 */
package org.ats.services.organization.entities;

import java.util.List;

import org.ats.services.organization.entities.Role;
import org.ats.services.organization.entities.Role.Permission;
import org.ats.services.organization.entities.Space.SpaceRef;
import org.ats.services.organization.entities.Tenant.TenantRef;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Mar 9, 2015
 */
public class EntityTestCase {

  @Test
  public void testRole() {
    Role role = new Role("role1");
    Permission perm1 = new Permission.Builder().feature("feature1").action("action1").tenant("tenant").space("*").build();
    Permission perm2 = new Permission.Builder().feature("feature2").action("action2").tenant("tenant").space("*").build();
    role.addPermission(perm1, perm2);
    
    Assert.assertTrue(role.hasPermisison("feature1:action1@tenant:*"));
    Assert.assertTrue(role.hasPermisison("feature2:action2@tenant:*"));
    Assert.assertFalse(role.hasPermisison("foo"));
    
    Assert.assertEquals("feature1", perm1.getFeature().getId());
    Assert.assertEquals("action1", perm1.getAction().getId());
    Assert.assertEquals("tenant", perm1.getTenant().getId());
    Assert.assertEquals(Space.ANY, perm1.getSpace());
    
    SpaceRef space = new SpaceRef("space");
    role.setSpace(space);
    space = role.getSpace();
    Assert.assertEquals("space", space.getId());
    
    List<Permission> list = role.getPermissions();
    Assert.assertEquals(2, list.size());
    Assert.assertTrue(list.contains(perm1));
    Assert.assertTrue(list.contains(perm2));

    Assert.assertEquals("[ { \"rule\" : \"feature1:action1@tenant:*\"} , { \"rule\" : \"feature2:action2@tenant:*\"}]", role.get("permissions").toString());
    
    role.removePermission("feature2:action2@tenant:*");
    Assert.assertFalse(role.hasPermisison("feature2:action2@tenant:*"));
    Assert.assertTrue(role.hasPermisison("feature1:action1@tenant:*"));
    Assert.assertEquals("[ { \"rule\" : \"feature1:action1@tenant:*\"}]", role.get("permissions").toString());
  }
  
  @Test
  public void testUser() {
    User user = new User("haint@cloud-ats.net", "Hai", "Nguyen");
    Tenant.TenantRef tenant = new TenantRef("admin");
    user.setTenant(tenant);
    
    TenantRef ref = user.getTanent();
    Assert.assertEquals(tenant.getId(), ref.getId());
    
    for (int i = 0; i < 100; i++) {
      user.joinSpace(new SpaceRef("space" + i));
    }
    
    List<Space.SpaceRef> spaces = user.getSpaces();
    Assert.assertEquals(100, spaces.size());
    Assert.assertEquals("space0", spaces.get(0).getId());
    Assert.assertEquals("space99", spaces.get(99).getId());
  }
}
