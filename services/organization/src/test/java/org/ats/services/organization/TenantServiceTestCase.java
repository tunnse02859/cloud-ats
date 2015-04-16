/**
 * 
 */
package org.ats.services.organization;


import org.ats.common.PageList;
import org.ats.services.organization.entity.Tenant;
import org.ats.services.organization.entity.reference.FeatureReference;
import org.ats.services.organization.entity.reference.TenantReference;
import org.ats.services.organization.event.AbstractEventTestCase;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Mar 13, 2015
 */
public class TenantServiceTestCase extends AbstractEventTestCase {

  @BeforeMethod
  public void init() throws Exception {
    super.init(this.getClass().getSimpleName());
    initService();
  }
  
  @AfterClass
  public void shutdown() throws Exception {
    this.eventService.stop();
  }
  
  @AfterMethod
  public void tearDown() throws Exception {
    this.mongoService.dropDatabase();
  }
  
  @Test
  public void testCRUD() {
    Tenant fsoft = tenantFactory.create("fsoft");
    fsoft.addFeature(featureRefFactory.create("Dashboard"), featureRefFactory.create("Organization"), featureRefFactory.create("Test"));
    tenantService.create(fsoft);
    
    fsoft = tenantService.get("fsoft");
    Assert.assertEquals(fsoft.getFeatures().size(), 3);
    
    fsoft.removeFeature(featureRefFactory.create("Dashboard"));
    tenantService.update(fsoft);
    fsoft = tenantService.get("fsoft");
    Assert.assertEquals(fsoft.getFeatures().size(), 2);
    
    TenantReference ref = tenantRefFactory.create("fsoft");
    fsoft = ref.get();
    Assert.assertEquals(fsoft.getFeatures().size(), 2);
    
    tenantService.delete(fsoft);
    Assert.assertEquals(tenantService.count(), 0);
    waitToFinishDeleteTenant(tenantRefFactory.create(fsoft.getId()));
  }
  
  @Test
  public void testList() {
    initTenant(53);
    Assert.assertEquals(tenantService.count(), 53);
    
    Tenant newTenant = tenantFactory.create("newTenant");
    tenantService.create(newTenant);
    Assert.assertEquals(tenantService.count(), 54);
    
    String id = newTenant.getId();
    tenantService.delete(id);
    Assert.assertEquals(tenantService.count(), 53);
    waitToFinishDeleteTenant(tenantRefFactory.create(id));
    
  }
  
  @Test
  public void testSearch() {
    initTenant(53);
    PageList<Tenant> list = tenantService.search("Tenant2");
    Assert.assertEquals(list.count(), 1);
    
    tenantService.delete("Tenant3");
    waitToFinishDeleteTenant(tenantRefFactory.create("Tenant3"));
    
    list = tenantService.search("Tenant3");
    Assert.assertEquals(list.count(), 0);
    
  }
  
  @Test
  public void testFindTenantInFeature() {
    
    initTenant(53);
    PageList<Tenant> list = tenantService.findIn("features", featureRefFactory.create("performance"));
    
    Assert.assertEquals(list.count(), 53);
    
    Tenant tenant = list.next().get(0);
    Assert.assertEquals(tenant.getFeatures().size(), 3);
  }
  
  @Test
  public void testReference() {
    
    Tenant newTenant = tenantFactory.create("newTenant");
    tenantService.create(newTenant);
    
    TenantReference ref = tenantRefFactory.create(newTenant.getId());
    newTenant = ref.get();
    
    Assert.assertEquals(newTenant.getId(), "newTenant");
  }
  
  private void initTenant(int total) {
    
    for (int i = 1; i <= total; i ++) {
      
      Tenant tenant = tenantFactory.create("Tenant"+ i);
      FeatureReference refFeature1 = featureRefFactory.create("performance");
      FeatureReference refFeature2 = featureRefFactory.create("functional");
      FeatureReference refFeature3 = featureRefFactory.create("organization");
      
      tenant.addFeature(refFeature1, refFeature2, refFeature3);
      
      tenantService.create(tenant);
    }
  }
}
