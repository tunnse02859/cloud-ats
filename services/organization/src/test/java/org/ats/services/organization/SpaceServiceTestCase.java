/**
 * 
 */
package org.ats.services.organization;

import org.ats.services.organization.entity.Space;
import org.ats.services.organization.entity.fatory.RoleReferenceFactory;
import org.ats.services.organization.entity.fatory.SpaceFactory;
import org.ats.services.organization.entity.fatory.TenantReferenceFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Mar 13, 2015
 */
public class SpaceServiceTestCase extends AbstractTestCase {

  /** .*/
  private SpaceService spaceService;
  
  /** .*/
  private SpaceFactory spaceFactory;
  
  /** .*/
  private TenantReferenceFactory tenantRefFactory;
  
  /** .*/
  private RoleReferenceFactory roleRefFactory;
  
  @Override
  @BeforeMethod
  public void init() throws Exception {
    super.init();
    this.spaceService = this.injector.getInstance(SpaceService.class);
    this.spaceFactory = this.injector.getInstance(SpaceFactory.class);
    this.tenantRefFactory = this.injector.getInstance(TenantReferenceFactory.class);
    this.roleRefFactory = this.injector.getInstance(RoleReferenceFactory.class);
  }
  
  @Test
  public void testCRUD() {
    Space space = spaceFactory.create("Test Space");
    space.setTenant(tenantRefFactory.create("fsoft"));
    space.addRole(roleRefFactory.create("guest"), roleRefFactory.create("admin"), roleRefFactory.create("tester"));
    spaceService.create(space);
    
    space = spaceService.get(space.getId());
    Assert.assertEquals(3, space.getRoles().size());
    Assert.assertEquals("fsoft", space.getTenant().getId());
    
    space.removeRole(roleRefFactory.create("guest"));
    spaceService.update(space);
    space = spaceService.get(space.getId());
    Assert.assertEquals(2, space.getRoles().size());
    
    spaceService.delete(space);
    Assert.assertNull(spaceService.get(space.getId()));
  }
}
