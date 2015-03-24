/**
 * 
 */
package org.ats.services.organization;

import org.ats.services.organization.entity.Space;
import org.ats.services.organization.entity.fatory.ReferenceFactory;
import org.ats.services.organization.entity.fatory.SpaceFactory;
import org.ats.services.organization.entity.reference.RoleReference;
import org.ats.services.organization.entity.reference.TenantReference;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.inject.Key;
import com.google.inject.TypeLiteral;

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
  private ReferenceFactory<TenantReference> tenantRefFactory;
  
  /** .*/
  private ReferenceFactory<RoleReference> roleRefFactory;
  
  @Override
  @BeforeMethod
  public void init() throws Exception {
    super.init();
    this.spaceService = this.injector.getInstance(SpaceService.class);
    this.spaceFactory = this.injector.getInstance(SpaceFactory.class);
    this.tenantRefFactory = this.injector.getInstance(Key.get(new TypeLiteral<ReferenceFactory<TenantReference>>(){}));
    this.roleRefFactory = this.injector.getInstance(Key.get(new TypeLiteral<ReferenceFactory<RoleReference>>(){}));
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
