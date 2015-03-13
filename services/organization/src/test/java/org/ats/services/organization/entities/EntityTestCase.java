/**
 * 
 */
package org.ats.services.organization.entities;

import java.util.List;

import org.ats.services.organization.entities.Feature.Action;
import org.ats.services.organization.entities.Feature.FeatureRef;
import org.ats.services.organization.entities.Role;
import org.ats.services.organization.entities.Role.Permission;
import org.ats.services.organization.entities.Role.RoleRef;
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
    
    Assert.assertTrue(role.hasPermisison(new Permission("feature1:action1@tenant:*")));
    Assert.assertTrue(role.hasPermisison(new Permission("feature2:action2@tenant:*")));
    Assert.assertFalse(role.hasPermisison(new Permission("foo")));
    
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
    
    role.removePermission(perm2);
    Assert.assertFalse(role.hasPermisison(new Permission("feature2:action2@tenant:*")));
    Assert.assertTrue(role.hasPermisison(new Permission("feature1:action1@tenant:*")));
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
    
    user.leaveSpace(new SpaceRef("space0"));
    Assert.assertEquals(99, user.getSpaces().size());
  }
  
  @Test
  public void testFeature() {
    Feature feature = new Feature("DAO", "create", "read", "update", "delete");
    
    Assert.assertEquals(4, feature.getActions().size());
    Assert.assertTrue(feature.hasAction(new Action("create")));
    Assert.assertTrue(feature.hasAction(new Action("read")));
    Assert.assertTrue(feature.hasAction(new Action("update")));
    Assert.assertTrue(feature.hasAction(new Action("delete")));
    Assert.assertFalse(feature.hasAction(new Action("foo")));
    Assert.assertTrue(feature.hasAction(Action.ANY));
    
    feature.removeAction(new Action("create"));
    Assert.assertEquals(3, feature.getActions().size());
    Assert.assertFalse(feature.hasAction(new Action("create")));
  }
  
  @Test
  public void testSpace() {
    Space space = new Space("space", null);
    space.addRole(new RoleRef("role1"), new RoleRef("role2"), new RoleRef("role3"));
    Assert.assertEquals(3, space.getRoles().size());
    
    space.removeRole(new RoleRef("role1"));
    Assert.assertEquals(2, space.getRoles().size());
    Assert.assertFalse(space.hasRole(new RoleRef("role1")));
    Assert.assertTrue(space.hasRole(new RoleRef("role2")));
  }
  
  @Test
  public void testTenant() {
    Tenant tenant = new Tenant("tenant");
    tenant.addFeature(new FeatureRef("DAO1"), new FeatureRef("DAO2"), new FeatureRef("DAO3"));
    Assert.assertEquals(3, tenant.getFeatures().size());
    
    tenant.removeFeature(new FeatureRef("DAO1"));
    Assert.assertEquals(2, tenant.getFeatures().size());
    Assert.assertFalse(tenant.hasFeature(new FeatureRef("DAO1")));
    Assert.assertTrue(tenant.hasFeature(new FeatureRef("DAO2")));
  }
}
