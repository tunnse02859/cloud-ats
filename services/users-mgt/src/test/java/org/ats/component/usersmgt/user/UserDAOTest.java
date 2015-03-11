/**
 * 
 */
package org.ats.component.usersmgt.user;

import java.util.Collection;

import junit.framework.Assert;

import org.ats.component.usersmgt.DataFactory;
import org.ats.component.usersmgt.Event;
import org.ats.component.usersmgt.EventExecutor;
import org.ats.component.usersmgt.UserManagementException;
import org.ats.component.usersmgt.feature.Feature;
import org.ats.component.usersmgt.feature.FeatureDAO;
import org.ats.component.usersmgt.feature.Operation;
import org.ats.component.usersmgt.feature.OperationDAO;
import org.ats.component.usersmgt.group.Group;
import org.ats.component.usersmgt.group.GroupDAO;
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
public class UserDAOTest {
  
  /** .*/
  private static final String dbName = "cloud-ats-test";
  
  /** .*/
  protected User user;
  
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
    
    Group manual = new Group(dbName, "Manual");
    Role role1 = new Role(dbName, "Testing", manual.getId());
    manual.addRole(role1);
    
    sme.addFeature(f);
    sme.addRole(role);
    

    RoleDAO.getInstance(dbName).create(role, role1);
    
    User user = new User(dbName, "HaiNT", "haint21@fsoft.com");
    
    user.addRole(role);
    role.addUser(user);
    
    user.addRole(role1);
    role1.addUser(user);
    
    user.joinGroup(sme);
    sme.addUser(user);
    
    user.joinGroup(cloud);
    cloud.addUser(user);

    user.joinGroup(manual);
    manual.addUser(user);
    
    UserDAO.getInstance(dbName).create(user);
    GroupDAO.getInstance(dbName).create(sme, cloud, mobile, manual);

    this.group = sme;
    this.user = user;
  }
  
  @Test
  public void testUserLeaveGroup() throws UserManagementException {
    Group cloud = GroupDAO.getInstance(dbName).find(new BasicDBObject("name", "Cloud")).iterator().next();
    Group manual = GroupDAO.getInstance(dbName).find(new BasicDBObject("name", "Manual")).iterator().next();
    
    Assert.assertTrue(cloud.getUsers().contains(this.user));
    Assert.assertTrue(this.group.getUsers().contains(this.user));
    
    this.user.leaveGroup(this.group);
    Event event = new Event(this.user, dbName) {
      @Override
      public String getType() {
        return "leave-group";
      }
    };
    event.broadcast();
    
    while(EventExecutor.getInstance(dbName).isInProgress()) {
    }
    
    this.user = UserDAO.getInstance(dbName).findOne(this.user.getId());
    this.group = GroupDAO.getInstance(dbName).findOne(this.group.getId());
    cloud = GroupDAO.getInstance(dbName).findOne(cloud.getId());
    
    Assert.assertEquals(manual, this.user.getGroups().iterator().next()); 
    Assert.assertTrue(!cloud.getUsers().contains(this.user));
    Assert.assertTrue(!this.group.getUsers().contains(this.user));
    
    Role role = RoleDAO.getInstance(dbName).find(new BasicDBObject("name", "Readonly")).iterator().next();
    Assert.assertTrue(!role.getUsers().contains(this.user));
    
    role = RoleDAO.getInstance(dbName).find(new BasicDBObject("name", "Testing")).iterator().next();
    Assert.assertEquals(role, this.user.getRoles().iterator().next());
  }
  
  @Test
  public void tetRemoveUser() throws UserManagementException {
    Role role = this.user.getRoles().get(0);
    UserDAO.getInstance(dbName).delete(this.user);
    while(EventExecutor.getInstance(dbName).isInProgress()) {
    }
    role = RoleDAO.getInstance(dbName).findOne(role.getId());
    Assert.assertTrue(role.getUsers().isEmpty());
  }

  @Test
  public void testFindUser() throws UserManagementException {
    User user = new User(dbName, "HaiNT fake", "fake@mail.com");
    UserDAO.getInstance(dbName).create(user);
    
    Collection<User> users = UserDAO.getInstance(dbName).find(new BasicDBObject("name", "HaiNT"));
    Assert.assertEquals(1, users.size());
    Assert.assertEquals(this.user, users.iterator().next());

    user.put("name", "HaiNTfake");
    UserDAO.getInstance(dbName).update(user);
    
    users = UserDAO.getInstance(dbName).find(new BasicDBObject("name", "HaiNT"));
    Assert.assertEquals(1, users.size());
    Assert.assertEquals(this.user, users.iterator().next());
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
