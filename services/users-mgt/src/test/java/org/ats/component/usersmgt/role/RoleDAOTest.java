/**
 * 
 */
package org.ats.component.usersmgt.role;

import junit.framework.Assert;

import org.ats.component.usersmgt.DataFactory;
import org.ats.component.usersmgt.EventExecutor;
import org.ats.component.usersmgt.UserManagementException;
import org.ats.component.usersmgt.feature.Feature;
import org.ats.component.usersmgt.feature.FeatureDAO;
import org.ats.component.usersmgt.feature.Operation;
import org.ats.component.usersmgt.feature.OperationDAO;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Jul 30, 2014
 */
public class RoleDAOTest {
  
  /** .*/
  private final static String dbName = "cloud-ats-test";
  
  /** .*/
  private Role role;
  
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
    
    role = new Role(dbName, "Readonly", "fake");
    role.addPermission(new Permission(dbName, f.getId(), o3.getId()));
    RoleDAO.getInstance(dbName).create(role);
  }
  
  @Test
  public void testRole() throws UserManagementException {
    
    Role actual = RoleDAO.getInstance(dbName).findOne(role.getId());
    Assert.assertEquals(role, actual);
    
    Assert.assertEquals(1, actual.getPermissions().size());
    
    Permission perm1 = role.getPermissions().iterator().next();
    Permission perm2 = actual.getPermissions().iterator().next();
    
    Assert.assertEquals(perm1, perm2);
  }
  
  @Test
  public void testDeleteOperation() throws UserManagementException {
    Role actual = RoleDAO.getInstance(dbName).findOne(role.getId());
    Assert.assertEquals(1, actual.getPermissions().size());
    
    Permission perm = role.getPermissions().iterator().next();
    String operation_id = perm.getString("operation_id");
    OperationDAO.getInstance(dbName).delete(operation_id);
    
  //wait until process whole events
    while (EventExecutor.getInstance(dbName).isInProgress()) {
    }
    
    long eventCount = EventExecutor.getInstance(dbName).getQueue().size();
    Assert.assertEquals(0, eventCount);
    
    eventCount = DataFactory.getDatabase(dbName).getCollection("event").count();
    Assert.assertEquals(0, eventCount);
    
    actual = RoleDAO.getInstance(dbName).findOne(role.getId());
    Assert.assertTrue(actual.getPermissions().isEmpty());
  }
  
  @Test
  public void testDeleteFeature() throws UserManagementException {
    Assert.assertEquals(1, role.getPermissions().size());
    
    Permission perm = role.getPermissions().iterator().next();
    String feature_id = perm.getString("feature_id");
    FeatureDAO.getInstance(dbName).delete(feature_id);
    
  //wait until process whole events
    while (EventExecutor.getInstance(dbName).isInProgress()) {
    }

    Role actual = RoleDAO.getInstance(dbName).findOne(role.getId());
    Assert.assertTrue(actual.getPermissions().isEmpty());
  }
  
  @Test
  public void testDeleteRole() throws UserManagementException {
    Permission perm = role.getPermissions().iterator().next();
    RoleDAO.getInstance(dbName).delete(role);
    
  //wait until process whole events
    while (EventExecutor.getInstance(dbName).isInProgress()) {
    }
    Assert.assertNull(PermissionDAO.getInstance(dbName).findOne(perm.getId()));
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
