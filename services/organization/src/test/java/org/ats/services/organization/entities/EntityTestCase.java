/**
 * 
 */
package org.ats.services.organization.entities;

import java.util.List;

import org.ats.services.organization.AbstractTestCase;
import org.ats.services.organization.entity.Feature;
import org.ats.services.organization.entity.Feature.Action;
import org.ats.services.organization.entity.Role;
import org.ats.services.organization.entity.Role.Permission;
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
import org.ats.services.organization.entity.reference.SpaceReference;
import org.ats.services.organization.entity.reference.TenantReference;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Mar 9, 2015
 */
public class EntityTestCase extends AbstractTestCase {

  /** .*/
  private UserFactory userFactory;
//  private UserReferenceFactory userRefFactory;

  /** .*/
  private TenantFactory tenantFactory;
  private TenantReferenceFactory tenantRefFactory;

  /** .*/
  private SpaceFactory spaceFactory;
  private SpaceReferenceFactory spaceRefFactory;

  /** .*/
  private RoleFactory roleFactory;
  private RoleReferenceFactory roleRefFactory;

  /** .*/
  private FeatureFactory featureFactory;
  private FeatureReferenceFactory featureRefFactory;
  
  /** .*/
  private PermissionFactory permFactory;

  @Override
  @BeforeMethod
  public void init() throws Exception {
    super.init();

    //
    this.userFactory = injector.getInstance(UserFactory.class);
//    this.userRefFactory = injector.getInstance(UserReferenceFactory.class);

    //
    this.tenantFactory = injector.getInstance(TenantFactory.class);
    this.tenantRefFactory = injector.getInstance(TenantReferenceFactory.class);

    //
    this.spaceFactory = injector.getInstance(SpaceFactory.class);
    this.spaceRefFactory = injector.getInstance(SpaceReferenceFactory.class);

    //
    this.roleFactory = injector.getInstance(RoleFactory.class);
    this.roleRefFactory = injector.getInstance(RoleReferenceFactory.class);

    //
    this.featureFactory = injector.getInstance(FeatureFactory.class);
    this.featureRefFactory = injector.getInstance(FeatureReferenceFactory.class);
    
    //
    this.permFactory = injector.getInstance(PermissionFactory.class);
  }

  @Test
  public void testRole() {
    Role role = roleFactory.create("role1");
    Permission perm1 = permFactory.create(new Permission.Builder().feature("feature1").action("action1").tenant("tenant").space("*").build());
    Permission perm2 = permFactory.create(new Permission.Builder().feature("feature2").action("action2").tenant("tenant").space("*").build());
    role.addPermission(perm1, perm2);

    Assert.assertTrue(role.hasPermisison(permFactory.create("feature1:action1@tenant:*")));
    Assert.assertTrue(role.hasPermisison(permFactory.create("feature2:action2@tenant:*")));
    Assert.assertFalse(role.hasPermisison(permFactory.create("foo")));

    System.out.println(perm1);
    Assert.assertEquals("feature1", perm1.getFeature().getId());
    Assert.assertEquals("action1", perm1.getAction().getId());
    Assert.assertEquals("tenant", perm1.getTenant().getId());
    Assert.assertEquals(Space.ANY, perm1.getSpace());

    SpaceReference space = spaceRefFactory.create("space");
    role.setSpace(space);
    space = role.getSpace();
    Assert.assertEquals("space", space.getId());

    List<Permission> list = role.getPermissions();
    Assert.assertEquals(2, list.size());
    Assert.assertTrue(list.contains(perm1));
    Assert.assertTrue(list.contains(perm2));

    Assert.assertEquals("[ { \"rule\" : \"feature1:action1@tenant:*\"} , { \"rule\" : \"feature2:action2@tenant:*\"}]", role.get("permissions").toString());

    role.removePermission(perm2);
    Assert.assertFalse(role.hasPermisison(permFactory.create("feature2:action2@tenant:*")));
    Assert.assertTrue(role.hasPermisison(permFactory.create("feature1:action1@tenant:*")));
    Assert.assertEquals("[ { \"rule\" : \"feature1:action1@tenant:*\"}]", role.get("permissions").toString());
  }

  @Test
  public void testUser() {
    User user = userFactory.create("haint@cloud-ats.net", "Hai", "Nguyen");
    TenantReference tenant = tenantRefFactory.create("admin");
    user.setTenant(tenant);

    TenantReference ref = user.getTanent();
    Assert.assertEquals(tenant.getId(), ref.getId());

    for (int i = 0; i < 100; i++) {
      user.joinSpace(spaceRefFactory.create("space" + i));
    }

    List<SpaceReference> spaces = user.getSpaces();
    Assert.assertEquals(100, spaces.size());
    Assert.assertEquals("space0", spaces.get(0).getId());
    Assert.assertEquals("space99", spaces.get(99).getId());

    user.leaveSpace(spaceRefFactory.create("space0"));
    Assert.assertEquals(99, user.getSpaces().size());
  }

  @Test
  public void testFeature() {
    Feature feature = featureFactory.create("DAO");
    feature.addAction(new Action("create"), new Action("read"), new Action("update"), new Action("delete"));

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
    Space space = spaceFactory.create("space");
    space.addRole(roleRefFactory.create("role1"), roleRefFactory.create("role2"), roleRefFactory.create("role3"));
    Assert.assertEquals(3, space.getRoles().size());

    space.removeRole(roleRefFactory.create("role1"));
    Assert.assertEquals(2, space.getRoles().size());
    Assert.assertFalse(space.hasRole(roleRefFactory.create("role1")));
    Assert.assertTrue(space.hasRole(roleRefFactory.create("role2")));
  }

  @Test
  public void testTenant() {
    Tenant tenant = tenantFactory.create("tenant");
    tenant.addFeature(featureRefFactory.create("DAO1"), featureRefFactory.create("DAO2"), featureRefFactory.create("DAO3"));
    Assert.assertEquals(3, tenant.getFeatures().size());

    tenant.removeFeature(featureRefFactory.create("DAO1"));
    Assert.assertEquals(2, tenant.getFeatures().size());
    Assert.assertFalse(tenant.hasFeature(featureRefFactory.create("DAO1")));
    Assert.assertTrue(tenant.hasFeature(featureRefFactory.create("DAO2")));
  }
}
