/**
 * 
 */
package org.ats.services.organization;

import java.util.List;

import org.ats.common.PageList;
import org.ats.services.organization.entity.Space;
import org.ats.services.organization.entity.fatory.ReferenceFactory;
import org.ats.services.organization.entity.fatory.SpaceFactory;
import org.ats.services.organization.entity.reference.RoleReference;
import org.ats.services.organization.entity.reference.SpaceReference;
import org.ats.services.organization.entity.reference.TenantReference;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
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
  
  /** .*/
  private ReferenceFactory<SpaceReference> spaceRefFactory;
  
  @BeforeMethod
  public void init() throws Exception {
    super.init(false);
    this.spaceService = this.injector.getInstance(SpaceService.class);
    this.spaceFactory = this.injector.getInstance(SpaceFactory.class);
    this.tenantRefFactory = this.injector.getInstance(Key.get(new TypeLiteral<ReferenceFactory<TenantReference>>(){}));
    this.roleRefFactory = this.injector.getInstance(Key.get(new TypeLiteral<ReferenceFactory<RoleReference>>(){}));
    this.tenantRefFactory = this.injector.getInstance(Key.get(new TypeLiteral<ReferenceFactory<TenantReference>>(){}));
    this.roleRefFactory = this.injector.getInstance(Key.get(new TypeLiteral<ReferenceFactory<RoleReference>>(){}));
    this.spaceRefFactory = this.injector.getInstance(Key.get(new TypeLiteral<ReferenceFactory<SpaceReference>>(){}));
  }
  
  @AfterMethod
  public void tearDown() throws Exception {
    super.tearDown();
  }
  
  @Test
  public void testCRUD() {
    Space space = spaceFactory.create("Test Space");
    space.setTenant(tenantRefFactory.create("fsoft"));
    space.addRole(roleRefFactory.create("guest"), roleRefFactory.create("admin"), roleRefFactory.create("tester"));
    spaceService.create(space);
    
    space = spaceService.get(space.getId());
    Assert.assertEquals(space.getRoles().size(), 3);
    Assert.assertEquals(space.getTenant().getId(), "fsoft");
    
    space.removeRole(roleRefFactory.create("guest"));
    spaceService.update(space);
    space = spaceService.get(space.getId());
    Assert.assertEquals(space.getRoles().size(), 2);
    
    spaceService.delete(space);
    Assert.assertNull(spaceService.get(space.getId()));
  }
  
  @Test
  public void testList() {
    // initiate spaces amount
    initSpace(54);
    
    //compare real spaces with spaces in database
    Assert.assertEquals(spaceService.count(), 54);
    
    PageList<Space> list = spaceService.list();
    Assert.assertEquals(list.count(), 54);
    
    //check total page with page size = 10
    Assert.assertEquals(list.totalPage(), 6);
    
    //check the first space in page 3
    List<Space> page3 = list.getPage(3);
    Assert.assertEquals(page3.get(0).getName(), "Fsu1 space21");
    
    //check total records after creating new space
    Space newSpace = spaceFactory.create("spaceFinal");
    spaceService.create(newSpace);
    Assert.assertEquals(spaceService.count(), 55);
    
    //check total records after deleting above space
    String id = newSpace.getId();
    spaceService.delete(id);
    Assert.assertEquals(spaceService.count(), 54);
    
  }
  
  @Test
  public void testSearch() {
    initSpace(55);
    Space newSpace = spaceFactory.create("Fsu1 Trinh");
    // add role for this space
    RoleReference role1 = roleRefFactory.create("role1");
    newSpace.addRole(role1);
    // save to database
    spaceService.create(newSpace);
    
    PageList<Space> list = spaceService.search("trinh");
    
    Assert.assertEquals(list.count(), 1);
    
    PageList<Space> fullList = spaceService.search("Fsu1");
    
    Assert.assertEquals(fullList.count(), 56);
    
    PageList<Space> emptyList = spaceService.search("Fsu1_Trinh");
    Assert.assertEquals(emptyList.count(), 0);
    
    Space newSpace1 = spaceFactory.create("Fsu1_Trinh");
    list = spaceService.search("Fsu1_Trinh");
    spaceService.create(newSpace1);
    
    Assert.assertEquals(list.count(), 1);
  }
  
  @Test
  public void testFindSpaceInRole() {
    initSpace(54);
    PageList<Space> list = spaceService.findIn("roles", roleRefFactory.create("role1"));
    Assert.assertEquals(list.count(), 54);
    
    Space space = list.next().get(0);
    Assert.assertEquals(space.getRoles().size(), 2);
    
  }
  
  @Test
  public void testReference() {
    
    // create new space
    Space newSpace = spaceFactory.create("Fsu1_Trinh");
    // add role for this space
    RoleReference role1 = roleRefFactory.create("role1");
    newSpace.addRole(role1);
    // save to database
    spaceService.create(newSpace);
    
    //use its Reference class to get itself
    String id = newSpace.getId();
    SpaceReference ref = spaceRefFactory.create(id);
    newSpace = ref.get();
    Assert.assertEquals(newSpace.getName(), "Fsu1_Trinh");
    
    Assert.assertEquals(newSpace.getRoles().get(0).getId(), "role1");
  }
  
  
  private void initSpace(int total) {

  	for (int i = 1; i <= total; i++) {
  		
      String nameSpace = "Fsu1 space" + i;
  
      Space space = spaceFactory.create(nameSpace);
  
      TenantReference tenant = tenantRefFactory.create("Fsoft");
      space.setTenant(tenant);
      RoleReference role1 = roleRefFactory.create("role1");
      RoleReference role2 = roleRefFactory.create("role2");
  
      space.addRole(role1, role2);
      spaceService.create(space);
      
    }
  }
  
}
