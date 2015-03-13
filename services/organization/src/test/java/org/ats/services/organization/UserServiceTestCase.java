/**
 * 
 */
package org.ats.services.organization;

import java.util.List;

import org.ats.common.MapBuilder;
import org.ats.common.PageList;
import org.ats.services.organization.entities.Role.RoleRef;
import org.ats.services.organization.entities.Space.SpaceRef;
import org.ats.services.organization.entities.Tenant.TenantRef;
import org.ats.services.organization.entities.User;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Mar 10, 2015
 */
public class UserServiceTestCase extends AbstractTestCase {
  
  private UserService userService;
  
  @Override
  public void init() throws Exception {
    super.init();
    this.userService = injector.getInstance(UserService.class);
  }

  @Test
  public void testCRUD() throws Exception {
    
    User user = new User("cloud@fsoft.com.vn", "fsu1", "bu11");
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
    
    userService.create(new User("haint@cloud-ats.net", "Hai", "Nguyen"));
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
    
    pages = userService.findUsersInSpace(new SpaceRef("space0"));
    Assert.assertEquals(100, pages.count());
    
    pages = userService.findUsersInSpace(new SpaceRef("foo"));
    Assert.assertEquals(0, pages.count());
  }
  
  @Test
  public void testFindUserInRole() {
    initUser(100);
    
    PageList<User> pages = userService.findIn("roles", new RoleRef("role1"));
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
  
  private void initUser(int total) {
    for (int i = 1; i <= total; i++) {
      String email = "user" + i + "@fsoft.com.vn";
      String firstName = "user" + i;
      String lastName = "fsoft";
      User user = new User(email, firstName, lastName);
      
      TenantRef tenant = new TenantRef("Fsoft");
      RoleRef role1 = new RoleRef("role1");
      RoleRef role2 = new RoleRef("role2");
      
      for (int j = 0; j < 100; j++) {
        user.joinSpace(new SpaceRef("space" + j));
      }
      
      user.setTenant(tenant);
      user.addRole(role1, role2);
      userService.create(user);
    }
  }
}
