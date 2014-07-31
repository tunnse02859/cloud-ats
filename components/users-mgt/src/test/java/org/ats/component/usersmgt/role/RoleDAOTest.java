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
  
  private Role role;
  
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
    
    Permission perm1 = new Permission(f.getId(), o3.getId());
    role = new Role("Readonly", "fake");
    role.addPermission(perm1);
    PermissionDAO.INSTANCE.create(perm1);
    RoleDAO.INSTANCE.create(role);
  }
  
  @Test
  public void testRole() throws UserManagementException {
    
    Role actual = RoleDAO.INSTANCE.findOne(role.getId());
    Assert.assertEquals(role, actual);
    
    Assert.assertEquals(1, actual.getPermissions().size());
    
    Permission perm1 = role.getPermissions().iterator().next();
    Permission perm2 = actual.getPermissions().iterator().next();
    
    Assert.assertEquals(perm1, perm2);
  }
  
  @Test
  public void testDeleteOperation() throws UserManagementException {
    Role actual = RoleDAO.INSTANCE.findOne(role.getId());
    Assert.assertEquals(1, actual.getPermissions().size());
    
    Permission perm = role.getPermissions().iterator().next();
    String operation_id = perm.getString("operation_id");
    OperationDAO.INSANCE.delete(operation_id);
    
  //wait until process whole events
    while (EventExecutor.INSTANCE.isInProgress()) {
    }
    
    long eventCount = EventExecutor.INSTANCE.getQueue().size();
    Assert.assertEquals(0, eventCount);
    
    eventCount = DataFactory.getDatabase("cloud-ats").getCollection("event").count();
    Assert.assertEquals(0, eventCount);
    
    actual = RoleDAO.INSTANCE.findOne(role.getId());
    Assert.assertTrue(actual.getPermissions().isEmpty());
  }
  
  @Test
  public void testDeleteFeature() throws UserManagementException {
    Assert.assertEquals(1, role.getPermissions().size());
    
    Permission perm = role.getPermissions().iterator().next();
    String feature_id = perm.getString("feature_id");
    FeatureDAO.INSTANCE.delete(feature_id);
    
  //wait until process whole events
    while (EventExecutor.INSTANCE.isInProgress()) {
    }

    Role actual = RoleDAO.INSTANCE.findOne(role.getId());
    Assert.assertTrue(actual.getPermissions().isEmpty());
  }
  
  @Test
  public void testDeleteRole() throws UserManagementException {
    Permission perm = role.getPermissions().iterator().next();
    RoleDAO.INSTANCE.delete(role);
    
  //wait until process whole events
    while (EventExecutor.INSTANCE.isInProgress()) {
    }
    Assert.assertNull(PermissionDAO.INSTANCE.findOne(perm.getId()));
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
