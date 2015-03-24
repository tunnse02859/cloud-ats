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
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.inject.Key;
import com.google.inject.TypeLiteral;

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
  
  @Override
  @BeforeMethod
  public void init() throws Exception {
    super.init();
    
  //
  this.userService = injector.getInstance(UserService.class);
  this.userFactory = injector.getInstance(UserFactory.class);

  this.userRefFactory = injector.getInstance(Key.get(new TypeLiteral<ReferenceFactory<UserReference>>(){}));
  this.tenantRefFactory = injector.getInstance(Key.get(new TypeLiteral<ReferenceFactory<TenantReference>>(){}));
  this.spaceRefFactory = injector.getInstance(Key.get(new TypeLiteral<ReferenceFactory<SpaceReference>>(){}));
  this.roleRefFactory = injector.getInstance(Key.get(new TypeLiteral<ReferenceFactory<RoleReference>>(){}));
  }
  
  @Test
  public void testCRUD() throws Exception {
    
    User user = this.userFactory.create("cloud@fsoft.com.vn", "fsu1", "bu11");
    userService.create(user);
    Assert.assertEquals(1, userService.count());
    
    user.setFirstName("fsoft.fsu1");
    userService.update(user);
    
    User updatedUser = userService.get(user.getEmail());
    Assert.assertEquals("fsoft.fsu1", updatedUser.getFirstName());
    
    userService.delete(user);
    Assert.assertEquals(0, userService.count());
  }
  
  @Test
  public void testList() throws Exception {
    initUser(123);
    Assert.assertEquals(123, userService.count());
    
    PageList<User> list = userService.list();
    Assert.assertEquals(123, list.count());
    Assert.assertEquals(13, list.totalPage());
    Assert.assertTrue(list.hasNext());
    Assert.assertNull(list.previous());
    
    List<User> page1 = list.next();
    Assert.assertEquals(10, page1.size());
    Assert.assertEquals("user1@fsoft.com.vn", page1.get(0).getEmail());
    Assert.assertEquals("user10@fsoft.com.vn", page1.get(9).getEmail());
    
    userService.create(userFactory.create("haint@cloud-ats.net", "Hai", "Nguyen"));
    Assert.assertEquals(124, userService.count());
    
    //test sort by created_date
    list.setSortable(new MapBuilder<String, Boolean>("created_date", true).build());
    
    List<User> page13 = list.getPage(13);
    Assert.assertEquals(4, page13.size());
    
    Assert.assertEquals("user121@fsoft.com.vn", page13.get(0).getEmail());
    Assert.assertEquals("haint@cloud-ats.net", page13.get(3).getEmail());
    
    User user = userService.get("user1@fsoft.com.vn");
    Assert.assertNotNull(user.getTanent());
    Assert.assertEquals("Fsoft", user.getTanent().getId());
    Assert.assertEquals(100, user.getSpaces().size());
    Assert.assertEquals(2, user.getRoles().size());
  }
  
  @Test
  public void testFindUserInSpace() {
    initUser(100);
    PageList<User> pages = userService.list();
    Assert.assertEquals(10, pages.totalPage());
    
    pages = userService.findUsersInSpace(spaceRefFactory.create("space0"));
    Assert.assertEquals(100, pages.count());
    
    pages = userService.findUsersInSpace(spaceRefFactory.create("foo"));
    Assert.assertEquals(0, pages.count());
  }
  
  @Test
  public void testFindUserInRole() {
    initUser(100);
    
    PageList<User> pages = userService.findIn("roles", roleRefFactory.create("role1"));
    Assert.assertEquals(100, pages.count());
    User user = pages.next().get(0);
    
    Assert.assertEquals(2, user.getRoles().size());
  }
  
  @Test
  public void testSearch() {
    initUser(100);
    
    PageList<User> pages = userService.search("user1");
    Assert.assertEquals(1, pages.count());
    
    pages = userService.search("fsoft");
    Assert.assertEquals(100, pages.count());
  }
  
  @Test
  public void testReference() {
    User user = userFactory.create("haint@cloud-ats.net", "Hai", "Nguyen");
    userService.create(user);
    
    UserReference ref = userRefFactory.create("haint@cloud-ats.net");
    user = ref.get();
    Assert.assertEquals("haint@cloud-ats.net", user.getEmail());
    Assert.assertEquals("Hai", user.getFirstName());
    Assert.assertEquals("Nguyen", user.getLastName());
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
