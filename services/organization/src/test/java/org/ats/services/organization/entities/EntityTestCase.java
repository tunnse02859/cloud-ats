/**
 * 
 */
package org.ats.services.organization.entities;

import java.util.List;

import org.ats.services.organization.entities.Role;
import org.ats.services.organization.entities.Role.Permission;
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
    Permission perm1 = new Permission("*:perm1@*");
    Permission perm2 = new Permission("*:perm2@*");
    role.addPermission(perm1, perm2);
    
    Assert.assertTrue(role.hasPermisison("*:perm1@*"));
    Assert.assertTrue(role.hasPermisison("*:perm2@*"));
    Assert.assertFalse(role.hasPermisison("foo"));
    
    List<Permission> list = role.getPermissions();
    Assert.assertEquals(2, list.size());
    Assert.assertTrue(list.contains(perm1));
    Assert.assertTrue(list.contains(perm2));
    
    String json ="{ \"_id\" : \"" + role.getId() + "\" , \"name\" : \"role1\" , \"permissions\" : [ { \"rule\" : \"*:perm1@*\"} , { \"rule\" : \"*:perm2@*\"}]}";
    Assert.assertEquals(json, role.toString());
    Assert.assertEquals("[ { \"rule\" : \"*:perm1@*\"} , { \"rule\" : \"*:perm2@*\"}]", role.get("permissions").toString());
    
    role.removePermission("*:perm1@*");
    Assert.assertFalse(role.hasPermisison("*:perm1@*"));
    Assert.assertTrue(role.hasPermisison("*:perm2@*"));
    Assert.assertEquals("[ { \"rule\" : \"*:perm2@*\"}]", role.get("permissions").toString());
  }
  
  @Test
  public void testUser() {
    User user = new User("haint@cloud-ats.net", "Hai", "Nguyen");
    Tenant.Reference tenant = new Tenant.Reference("admin", "Admin");
    user.setTenant(tenant);
    Tenant.Reference ref = user.getTanent();
    Assert.assertEquals(tenant, ref);
    
    for (int i = 0; i < 100; i++) {
      user.joinSpace(new Space.Reference("space" + i, "space" + i));
    }
    
    List<Space.Reference> spaces = user.getSpaces();
    Assert.assertEquals(100, spaces.size());
    Assert.assertEquals("space0", spaces.get(0).getName());
    Assert.assertEquals("space99", spaces.get(99).getName());
  }
}
