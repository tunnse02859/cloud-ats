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

  /** .*/
  private final static String dbName = "cloud-ats-test";
  
  /** .*/
  protected Group group;
  
  @BeforeClass
  public static void initialize() {
    EventExecutor.getInstance(dbName).start();
  }

  @Before
  public void setUp() throws UserManagementException {
    Feature f = new Feature(dbName, "Automation Test");
    Operation o1 = new Operation(dbName, "create");
    f.addOperation(o1);
    Operation o2 = new Operation(dbName, "delete");
    f.addOperation(o2);
    Operation o3 = new Operation(dbName, "view");
    f.addOperation(o3);
    
    FeatureDAO.getInstance(dbName).create(f);
    OperationDAO.getInstance(dbName).create(o1);
    OperationDAO.getInstance(dbName).create(o2);
    OperationDAO.getInstance(dbName).create(o3);
    
    Group sme = new Group(dbName, "SME");

    Role role = new Role(dbName, "Readonly", sme.getId());
    role.addPermission(new Permission(dbName, f.getId(), o3.getId()));
    
    Group cloud = new Group(dbName, "Cloud");
    sme.addGroupChild(cloud);
    
    Group mobile = new Group(dbName, "Mobile");
    sme.addGroupChild(mobile);
    
    sme.addFeature(f);
    sme.addRole(role);
    
    RoleDAO.getInstance(dbName).create(role);
    GroupDAO.getInstance(dbName).create(sme, cloud, mobile);
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
    RoleDAO.getInstance(dbName).delete(role);

    //Wait until finish processing whole events
    while(EventExecutor.getInstance(dbName).isInProgress()) {
    }
    
    Group actual = GroupDAO.getInstance(dbName).findOne(this.group.getId());
    Assert.assertTrue(actual.getRoles().isEmpty());
  }
  
  @Test
  public void testDeleteFeature() throws UserManagementException {
    Feature feature = this.group.getFeatures().iterator().next();
    FeatureDAO.getInstance(dbName).delete(feature);
    
    //Wait until finish processing whole events
    while(EventExecutor.getInstance(dbName).isInProgress()) {
    }
    
    Group actual = GroupDAO.getInstance(dbName).findOne(this.group.getId());
    Assert.assertTrue(actual.getFeatures().isEmpty());
    
    Role role = this.group.getRoles().iterator().next();
    Assert.assertTrue(role.getPermissions().isEmpty());
  }
  
  @Test
  public void testDeleteGroup() throws UserManagementException {
    
    Group cloud = GroupDAO.getInstance(dbName).find(new BasicDBObject("name", "Cloud")).iterator().next();
    Role role1 = new Role(dbName, "Role1", cloud.getId());
    RoleDAO.getInstance(dbName).create(role1);
    cloud.addRole(role1);
    GroupDAO.getInstance(dbName).update(cloud);
    
    Group mobile = GroupDAO.getInstance(dbName).find(new BasicDBObject("name", "Mobile")).iterator().next();
    Role role2 = new Role(dbName, "Role2", mobile.getId());
    RoleDAO.getInstance(dbName).create(role2);
    mobile.addRole(role2);
    GroupDAO.getInstance(dbName).update(mobile);
    
    //Delete child
    GroupDAO.getInstance(dbName).delete(mobile);
    //Wait until finish processing whole events
    while(EventExecutor.getInstance(dbName).isInProgress()) {
    }
    this.group = GroupDAO.getInstance(dbName).findOne(this.group.getId());
    
    Assert.assertEquals(1, this.group.getAllChildren().size());
    
    //Delete group parent
    GroupDAO.getInstance(dbName).delete(this.group);
  
    //Wait until finish processing whole events
    while(EventExecutor.getInstance(dbName).isInProgress()) {
    }
    
    Group actual = GroupDAO.getInstance(dbName).findOne(this.group.getId());
    Assert.assertNull(actual);
    
    Assert.assertNull(GroupDAO.getInstance(dbName).findOne(cloud.getId()));
    Assert.assertNull(RoleDAO.getInstance(dbName).findOne(role1.getId()));
    
    Assert.assertNull(GroupDAO.getInstance(dbName).findOne(mobile.getId()));
    Assert.assertNull(RoleDAO.getInstance(dbName).findOne(role2.getId()));
  }
  
  @Test
  public void testParentTree() throws UserManagementException {
    Group g1  = new Group(dbName, "g1");
    
    Group g1_1 = new Group(dbName, "g1_1");
    Group g1_2 = new Group(dbName, "g1_2");
    Group g1_3 = new Group(dbName, "g1_3");
    
    g1.addGroupChild(g1_1);
    g1.addGroupChild(g1_2);
    g1.addGroupChild(g1_3);
    
    Group g1_1_1 = new Group(dbName, "g1_1_1");
    g1_1.addGroupChild(g1_1_1);
    
    Group g1_2_1 = new Group(dbName, "g1_2_1");
    g1_2.addGroupChild(g1_2_1);
    
    Group g1_3_1 = new Group(dbName, "g1_3_1");
    g1_3.addGroupChild(g1_3_1);
    
    GroupDAO.getInstance(dbName).create(g1, g1_1, g1_1_1, g1_2, g1_2_1, g1_3, g1_3_1);

    LinkedList<Group> tree = GroupDAO.getInstance(dbName).buildParentTree(g1_1_1);
    Assert.assertEquals(g1, tree.get(0));
    Assert.assertEquals(g1_1, tree.get(1));
    
    tree = GroupDAO.getInstance(dbName).buildParentTree(g1_2_1);
    Assert.assertEquals(g1, tree.get(0));
    Assert.assertEquals(g1_2, tree.get(1));
    
    tree = GroupDAO.getInstance(dbName).buildParentTree(g1_3_1);
    Assert.assertEquals(g1, tree.get(0));
    Assert.assertEquals(g1_3, tree.get(1));
    
    Assert.assertEquals(6, g1.getAllChildren().size());
  }
  
  @After
  public void tearDown() {
    DataFactory.dropDatabase(dbName);
  }
  
  @AfterClass
  public static void destroy() {
    EventExecutor.getInstance(dbName).stop();
  }
}
