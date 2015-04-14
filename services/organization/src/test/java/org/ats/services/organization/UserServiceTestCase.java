/**
 * 
 */
package org.ats.services.organization;

import java.util.List;

import org.ats.common.MapBuilder;
import org.ats.common.PageList;
import org.ats.services.organization.entity.User;
import org.ats.services.organization.entity.fatory.ReferenceFactory;
import org.ats.services.organization.entity.fatory.UserFactory;
import org.ats.services.organization.entity.reference.RoleReference;
import org.ats.services.organization.entity.reference.SpaceReference;
import org.ats.services.organization.entity.reference.TenantReference;
import org.ats.services.organization.entity.reference.UserReference;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.mongodb.BasicDBObject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Mar 10, 2015
 */
public class UserServiceTestCase extends AbstractTestCase {
  
  /** .*/
  private UserService userService;
  
  /** .*/
  private UserFactory userFactory;
  
  /** .*/
  private ReferenceFactory<UserReference> userRefFactory;
  
  /** .*/
  private ReferenceFactory<TenantReference> tenantRefFactory;
  
  /** .*/
  private ReferenceFactory<SpaceReference> spaceRefFactory;
  
  /** .*/
  private ReferenceFactory<RoleReference> roleRefFactory;
  
  @BeforeMethod
  public void init() throws Exception {
    super.init(this.getClass().getSimpleName());
    
  //
  this.userService = injector.getInstance(UserService.class);
  this.userFactory = injector.getInstance(UserFactory.class);

  this.userRefFactory = injector.getInstance(Key.get(new TypeLiteral<ReferenceFactory<UserReference>>(){}));
  this.tenantRefFactory = injector.getInstance(Key.get(new TypeLiteral<ReferenceFactory<TenantReference>>(){}));
  this.spaceRefFactory = injector.getInstance(Key.get(new TypeLiteral<ReferenceFactory<SpaceReference>>(){}));
  this.roleRefFactory = injector.getInstance(Key.get(new TypeLiteral<ReferenceFactory<RoleReference>>(){}));
  }
  
  @AfterMethod
  public void tearDown() throws Exception {
    super.tearDown();
  }
  
  @AfterClass
  public void dropDB() throws Exception {
    super.tearDown();
  }
  
  @Test
  public void testCRUD() throws Exception {
    
    User user = this.userFactory.create("cloud@fsoft.com.vn", "fsu1", "bu11");
    userService.create(user);
    Assert.assertEquals(userService.count(), 1);
    
    user.setFirstName("fsoft.fsu1");
    userService.update(user);
    
    User updatedUser = userService.get(user.getEmail());
    Assert.assertEquals(updatedUser.getFirstName(), "fsoft.fsu1");
    
    userService.delete(user);
    Assert.assertEquals(userService.count(), 0);
  }
  
  @Test
  public void testList() throws Exception {
    initUser(123);
    Assert.assertEquals(userService.count(), 123);
    
    PageList<User> list = userService.list();
    Assert.assertEquals(list.count(), 123);
    Assert.assertEquals(list.totalPage(), 13);
    Assert.assertTrue(list.hasNext());
    Assert.assertNull(list.previous());
    
    List<User> page1 = list.next();
    Assert.assertEquals(page1.size(), 10);
    Assert.assertEquals(page1.get(0).getEmail(), "user1@fsoft.com.vn");
    Assert.assertEquals(page1.get(9).getEmail(), "user10@fsoft.com.vn");
    
    userService.create(userFactory.create("haint@cloud-ats.net", "Hai", "Nguyen"));
    Assert.assertEquals(userService.count(), 124);
    
    //test sort by created_date
    list.setSortable(new MapBuilder<String, Boolean>("created_date", true).build());
    
    List<User> page13 = list.getPage(13);
    Assert.assertEquals(page13.size(), 4);
    
    Assert.assertEquals(page13.get(0).getEmail(), "user121@fsoft.com.vn");
    Assert.assertEquals(page13.get(3).getEmail(), "haint@cloud-ats.net");
    
    User user = userService.get("user1@fsoft.com.vn");
    Assert.assertNotNull(user.getTanent());
    Assert.assertEquals(user.getTanent().getId(), "Fsoft");
    Assert.assertEquals(user.getSpaces().size(), 100);
    Assert.assertEquals(user.getRoles().size(), 2);
  }
  
  @Test
  public void testFindUserInSpace() {
    initUser(100);
    PageList<User> pages = userService.list();
    Assert.assertEquals(pages.totalPage(), 10);
    
    pages = userService.findUsersInSpace(spaceRefFactory.create("space0"));
    Assert.assertEquals(pages.count(), 100);
    
    pages = userService.findUsersInSpace(spaceRefFactory.create("foo"));
    Assert.assertEquals(pages.count(), 0);
  }
  
  @Test
  public void testFindUserInRole() {
    initUser(100);
    
    PageList<User> pages = userService.findIn("roles", roleRefFactory.create("role1"));
    Assert.assertEquals(pages.count(), 100);
    User user = pages.next().get(0);
    
    Assert.assertEquals(user.getRoles().size(), 2);
  }
  
  @Test
  public void testSearch() {
    initUser(100);
    
    PageList<User> pages = userService.search("user1");
    Assert.assertEquals(pages.count(), 1);
    
    pages = userService.search("fsoft");
    Assert.assertEquals(pages.count(), 100);
  }
  
  @Test
  public void testReference() {
    User user = userFactory.create("haint@cloud-ats.net", "Hai", "Nguyen");
    userService.create(user);
    
    UserReference ref = userRefFactory.create("haint@cloud-ats.net");
    user = ref.get();
    Assert.assertEquals(user.getEmail(), "haint@cloud-ats.net");
    Assert.assertEquals(user.getFirstName(), "Hai");
    Assert.assertEquals(user.getLastName(), "Nguyen");
  }
  
  @Test
  public void testDeleteByQuery() {
    initUser(100);
    
    userService.deleteBy(new BasicDBObject("first_name", "user1"));
    Assert.assertEquals(userService.count(), 99);
    
    userService.deleteBy(new BasicDBObject("last_name", "fsoft"));
    Assert.assertEquals(userService.count(), 0);
  }
  
  private void initUser(int total) {
    for (int i = 1; i <= total; i++) {
      String email = "user" + i + "@fsoft.com.vn";
      String firstName = "user" + i;
      String lastName = "fsoft";
      User user = userFactory.create(email, firstName, lastName);
      
      TenantReference tenant = tenantRefFactory.create("Fsoft");
      RoleReference role1 = roleRefFactory.create("role1");
      RoleReference role2 = roleRefFactory.create("role2");
      
      for (int j = 0; j < 100; j++) {
        user.joinSpace(spaceRefFactory.create("space" + j));
      }
      
      user.setTenant(tenant);
      user.addRole(role1, role2);
      userService.create(user);
    }
  }
}
