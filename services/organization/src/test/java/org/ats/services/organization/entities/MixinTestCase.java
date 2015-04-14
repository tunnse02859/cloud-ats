/**
 * 
 */
package org.ats.services.organization.entities;

import java.util.Date;

import org.ats.services.organization.AbstractTestCase;
import org.ats.services.organization.UserService;
import org.ats.services.organization.entity.User;
import org.ats.services.organization.entity.fatory.UserFactory;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Mar 23, 2015
 */
public class MixinTestCase extends AbstractTestCase {
  
  private UserService userService;
  
  private UserFactory userFactory;
  
  @BeforeMethod
  public void init() throws Exception {
    super.init(this.getClass().getSimpleName());
    this.userService  = injector.getInstance(UserService.class);
    this.userFactory = injector.getInstance(UserFactory.class);
  }

  @AfterMethod
  public void tearDown() throws Exception {
    super.tearDown();
  }
  
  @SuppressWarnings("deprecation")
  @Test
  public void test() {
    User user = userFactory.create("haint@cloud-ats.net", "Hai", "Nguyen");
    user.put("dob", new Date(1986, 9, 8));
    userService.create(user);
    
    Assert.assertEquals(userService.count(), 1);
    user = userService.get(user.getEmail());
    Assert.assertNull(user.get("dob"));
    
    user = userService.get(user.getEmail(), "dob");
    Assert.assertEquals(new Date(1986, 9, 8), user.get("dob"));
  }
}
