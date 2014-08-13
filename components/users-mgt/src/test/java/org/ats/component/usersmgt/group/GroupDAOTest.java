/**
 * 
 */
package org.ats.component.usersmgt.group;

import java.util.LinkedList;

import junit.framework.Assert;

import org.ats.component.usersmgt.DataFactory;
import org.ats.component.usersmgt.EventExecutor;
import org.ats.component.usersmgt.UserManagementException;
import org.ats.component.usersmgt.feature.Feature;
import org.ats.component.usersmgt.feature.FeatureDAO;
import org.ats.component.usersmgt.feature.Operation;
import org.ats.component.usersmgt.feature.OperationDAO;
import org.ats.component.usersmgt.role.Permission;
import org.ats.component.usersmgt.role.PermissionDAO;
import org.ats.component.usersmgt.role.Role;
import org.ats.component.usersmgt.role.RoleDAO;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.mongodb.BasicDBObject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Jul 31, 2014
 */
public class GroupDAOTest {

  protected Group group;
  
  @BeforeClass
  public static void initialize() {
    EventExecutor.INSTANCE.start();
  }

  @Before
  public void setUp() throws UserManagementException {
    Feature f = new Feature("Automation Test");
    Operation o1 = new Operation("create");
    f.addOperation(o1);
    Operation o2 = new Operation("delete");
    f.addOperation(o2);
    Operation o3 = new Operation("view");
    f.addOperation(o3);
    
    FeatureDAO.INSTANCE.create(f);
    OperationDAO.INSANCE.create(o1);
    OperationDAO.INSANCE.create(o2);
    OperationDAO.INSANCE.create(o3);
    
    Group sme = new Group("SME");

    Role role = new Role("Readonly", sme.getId());
    role.addPermission(new Permission(f.getId(), o3.getId()));
    
    Group cloud = new Group("Cloud");
    sme.addGroupChild(cloud);
    
    Group mobile = new Group("Mobile");
    sme.addGroupChild(mobile);
    
    sme.addFeature(f);
    sme.addRole(role);
    
    RoleDAO.INSTANCE.create(role);
    GroupDAO.INSTANCE.create(sme, cloud, mobile);
    this.group = sme;
  }
  
  @Test
  public void testGroup() throws UserManagementException {
    Assert.assertEquals(2, this.group.getGroupChildren().size());
    Assert.assertEquals(1, this.group.getFeatures().size());
    Assert.assertEquals(1, this.group.getRoles().size());
    Assert.assertTrue(this.group.getUsers().isEmpty());
  }
  
  @Test
  public void testDeleteRole() throws UserManagementException {
    Role role = this.group.getRoles().iterator().next();
    RoleDAO.INSTANCE.delete(role);

    //Wait until finish processing whole events
    while(EventExecutor.INSTANCE.isInProgress()) {
    }
    
    Group actual = GroupDAO.INSTANCE.findOne(this.group.getId());
    Assert.assertTrue(actual.getRoles().isEmpty());
  }
  
  @Test
  public void testDeleteFeature() throws UserManagementException {
    Feature feature = this.group.getFeatures().iterator().next();
    FeatureDAO.INSTANCE.delete(feature);
    
    //Wait until finish processing whole events
    while(EventExecutor.INSTANCE.isInProgress()) {
    }
    
    Group actual = GroupDAO.INSTANCE.findOne(this.group.getId());
    Assert.assertTrue(actual.getFeatures().isEmpty());
    
    Role role = this.group.getRoles().iterator().next();
    Assert.assertTrue(role.getPermissions().isEmpty());
  }
  
  @Test
  public void testDeleteGroup() throws UserManagementException {
    
    Group cloud = GroupDAO.INSTANCE.find(new BasicDBObject("name", "Cloud")).iterator().next();
    Role role1 = new Role("Role1", cloud.getId());
    RoleDAO.INSTANCE.create(role1);
    cloud.addRole(role1);
    GroupDAO.INSTANCE.update(cloud);
    
    Group mobile = GroupDAO.INSTANCE.find(new BasicDBObject("name", "Mobile")).iterator().next();
    Role role2 = new Role("Role2", mobile.getId());
    RoleDAO.INSTANCE.create(role2);
    mobile.addRole(role2);
    GroupDAO.INSTANCE.update(mobile);
    
    //Delete group parent
    GroupDAO.INSTANCE.delete(this.group);
  
    //Wait until finish processing whole events
    while(EventExecutor.INSTANCE.isInProgress()) {
    }
    
    Group actual = GroupDAO.INSTANCE.findOne(this.group.getId());
    Assert.assertNull(actual);
    
    Assert.assertNull(GroupDAO.INSTANCE.findOne(cloud.getId()));
    Assert.assertNull(RoleDAO.INSTANCE.findOne(role1.getId()));
    
    Assert.assertNull(GroupDAO.INSTANCE.findOne(mobile.getId()));
    Assert.assertNull(RoleDAO.INSTANCE.findOne(role2.getId()));
  }
  
  @Test
  public void testParentTree() throws UserManagementException {
    Group g1  = new Group("g1");
    
    Group g1_1 = new Group("g1_1");
    Group g1_2 = new Group("g1_2");
    Group g1_3 = new Group("g1_3");
    
    g1.addGroupChild(g1_1);
    g1.addGroupChild(g1_2);
    g1.addGroupChild(g1_3);
    
    Group g1_1_1 = new Group("g1_1_1");
    g1_1.addGroupChild(g1_1_1);
    
    Group g1_2_1 = new Group("g1_2_1");
    g1_2.addGroupChild(g1_2_1);
    
    Group g1_3_1 = new Group("g1_3_1");
    g1_3.addGroupChild(g1_3_1);
    
    GroupDAO.INSTANCE.create(g1, g1_1, g1_1_1, g1_2, g1_2_1, g1_3, g1_3_1);

    LinkedList<Group> tree = g1_1_1.buildParentTree();
    Assert.assertEquals(g1, tree.get(0));
    Assert.assertEquals(g1_1, tree.get(1));
    
    tree = g1_2_1.buildParentTree();
    Assert.assertEquals(g1, tree.get(0));
    Assert.assertEquals(g1_2, tree.get(1));
    
    tree = g1_3_1.buildParentTree();
    Assert.assertEquals(g1, tree.get(0));
    Assert.assertEquals(g1_3, tree.get(1));
  }
  
  @After
  public void tearDown() {
    DataFactory.dropDatabase("cloud-ats");
  }
  
  @AfterClass
  public static void destroy() {
    EventExecutor.INSTANCE.stop();
  }
}
