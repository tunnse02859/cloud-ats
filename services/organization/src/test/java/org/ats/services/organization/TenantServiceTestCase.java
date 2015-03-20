/**
 * 
 */
package org.ats.services.organization;


import org.ats.common.PageList;
import org.ats.services.organization.entity.Tenant;
import org.ats.services.organization.entity.fatory.ReferenceFactory;
import org.ats.services.organization.entity.fatory.TenantFactory;
import org.ats.services.organization.entity.reference.FeatureReference;
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
public class TenantServiceTestCase extends AbstractTestCase {

  /** .*/
  private TenantService service;
  
  /** .*/
  private TenantFactory factory;
  
  /** .*/
  private ReferenceFactory<FeatureReference> featureRefFactory;
  
  /** .*/
  private ReferenceFactory<TenantReference> tenantRefFactory;
  
  /** .*/ 
  private TenantFactory tenantFactory;
  
  @Override
  @BeforeMethod
  public void init() throws Exception {
    super.init();
    this.service = this.injector.getInstance(TenantService.class);
    this.factory = this.injector.getInstance(TenantFactory.class);
    this.featureRefFactory = this.injector.getInstance(Key.get(new TypeLiteral<ReferenceFactory<FeatureReference>>(){}));
    this.tenantRefFactory = this.injector.getInstance(Key.get(new TypeLiteral<ReferenceFactory<TenantReference>>(){}));
    this.tenantFactory = this.injector.getInstance(TenantFactory.class);
  }
  
  @Test
  public void testCRUD() {
    Tenant fsoft = factory.create("fsoft");
    fsoft.addFeature(featureRefFactory.create("Dashboard"), featureRefFactory.create("Organization"), featureRefFactory.create("Test"));
    service.create(fsoft);
    
    fsoft = service.get("fsoft");
    Assert.assertEquals(fsoft.getFeatures().size(), 3);
    
    fsoft.removeFeature(featureRefFactory.create("Dashboard"));
    service.update(fsoft);
    fsoft = service.get("fsoft");
    Assert.assertEquals(fsoft.getFeatures().size(), 2);
    
    TenantReference ref = tenantRefFactory.create("fsoft");
    fsoft = ref.get();
    Assert.assertEquals(fsoft.getFeatures().size(), 2);
    
    service.delete(fsoft);
    Assert.assertEquals(service.count(), 0);
  }
  
  @Test
  public void testList() {
    initTenant(53);
    Assert.assertEquals(service.count(), 53);
    
    Tenant newTenant = tenantFactory.create("newTenant");
    service.create(newTenant);
    Assert.assertEquals(service.count(), 54);
    
    String id = newTenant.getId();
    service.delete(id);
    Assert.assertEquals(service.count(), 53);
    
  }
  
  @Test
  public void testSearch() {
    initTenant(53);
    PageList<Tenant> list = service.search("Tenant2");
    Assert.assertEquals(list.count(), 1);
    
    service.delete("Tenant3");
    
    list = service.search("Tenant3");
    Assert.assertEquals(list.count(), 0);
    
  }
  
  @Test
  public void testFindTenantInFeature() {
    
    initTenant(53);
    PageList<Tenant> list = service.findIn("features", featureRefFactory.create("performance"));
    
    Assert.assertEquals(list.count(), 53);
    
    Tenant tenant = list.next().get(0);
    Assert.assertEquals(tenant.getFeatures().size(), 3);
  }
  
  @Test
  public void testReference() {
    
    Tenant newTenant = tenantFactory.create("newTenant");
    service.create(newTenant);
    
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
      
      service.create(tenant);
    }
  }
}
