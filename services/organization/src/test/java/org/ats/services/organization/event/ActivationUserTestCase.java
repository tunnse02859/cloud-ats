/**
 * 
 */
package org.ats.services.organization.event;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Apr 14, 2015
 */
public class ActivationUserTestCase extends AbstractEventTestCase {

  @BeforeMethod
  public void init() throws Exception {
    super.init(this.getClass().getSimpleName());
  }
  
  @Test
  public void testActivationUser() {
    
    Assert.assertEquals(activationService.countTenant(), 0);
    Assert.assertEquals(userService.count(), 1);
    
    activationService.inActiveUser("haint@cloud-ats.net");
    
    Assert.assertEquals(userService.count(), 0);
    Assert.assertEquals(activationService.countUser(), 1);
    
    activationService.activeUser("haint@cloud-ats.net");
    
    Assert.assertEquals(activationService.countUser(), 0);
    Assert.assertEquals(userService.count(), 1);
    
    mongoService.dropDatabase();
  }
}
