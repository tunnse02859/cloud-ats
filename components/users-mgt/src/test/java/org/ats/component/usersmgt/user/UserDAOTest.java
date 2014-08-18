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
public class UserDAOTest {
  
  protected User user;
  
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
    
    Group manual = new Group("Manual");
    Role role1 = new Role("Testing", manual.getId());
    manual.addRole(role1);
    
    sme.addFeature(f);
    sme.addRole(role);
    

    RoleDAO.INSTANCE.create(role, role1);
    
    User user = new User("HaiNT", "haint21@fsoft.com");
    
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
    
    UserDAO.INSTANCE.create(user);
    GroupDAO.INSTANCE.create(sme, cloud, mobile, manual);

    this.group = sme;
    this.user = user;
  }
  
  @Test
  public void testUserLeaveGroup() throws UserManagementException {
    Group cloud = GroupDAO.INSTANCE.find(new BasicDBObject("name", "Cloud")).iterator().next();
    Group manual = GroupDAO.INSTANCE.find(new BasicDBObject("name", "Manual")).iterator().next();
    
    Assert.assertTrue(cloud.getUsers().contains(this.user));
    Assert.assertTrue(this.group.getUsers().contains(this.user));
    
    this.user.leaveGroup(this.group);
    Event event = new Event(this.user) {
      @Override
      public String getType() {
        return "leave-group";
      }
    };
    event.broadcast();
    
    while(EventExecutor.INSTANCE.isInProgress()) {
    }
    
    this.user = UserDAO.INSTANCE.findOne(this.user.getId());
    this.group = GroupDAO.INSTANCE.findOne(this.group.getId());
    cloud = GroupDAO.INSTANCE.findOne(cloud.getId());
    
    Assert.assertEquals(manual, this.user.getGroups().iterator().next()); 
    Assert.assertTrue(!cloud.getUsers().contains(this.user));
    Assert.assertTrue(!this.group.getUsers().contains(this.user));
    
    Role role = RoleDAO.INSTANCE.find(new BasicDBObject("name", "Readonly")).iterator().next();
    Assert.assertTrue(!role.getUsers().contains(this.user));
    
    role = RoleDAO.INSTANCE.find(new BasicDBObject("name", "Testing")).iterator().next();
    Assert.assertEquals(role, this.user.getRoles().iterator().next());
  }

  @Test
  public void testFindUser() throws UserManagementException {
    User user = new User("HaiNT fake", "fake@mail.com");
    UserDAO.INSTANCE.create(user);
    
    Collection<User> users = UserDAO.INSTANCE.find(new BasicDBObject("name", "HaiNT"));
    Assert.assertEquals(1, users.size());
    Assert.assertEquals(this.user, users.iterator().next());

    user.put("name", "HaiNTfake");
    UserDAO.INSTANCE.update(user);
    
    users = UserDAO.INSTANCE.find(new BasicDBObject("name", "HaiNT"));
    Assert.assertEquals(1, users.size());
    Assert.assertEquals(this.user, users.iterator().next());
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
