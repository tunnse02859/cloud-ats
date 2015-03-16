/**
 * 
 */
package org.ats.services.organization;

import junit.framework.Assert;

import org.ats.services.organization.entity.Tenant;
import org.ats.services.organization.entity.fatory.FeatureReferenceFactory;
import org.ats.services.organization.entity.fatory.TenantFactory;
import org.ats.services.organization.entity.fatory.TenantReferenceFactory;
import org.ats.services.organization.entity.reference.TenantReference;
import org.junit.Test;

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
  private FeatureReferenceFactory featureRefFactory;
  
  /** .*/
  private TenantReferenceFactory tenantRefFactory;
  
  @Override
  public void init() throws Exception {
    super.init();
    this.service = this.injector.getInstance(TenantService.class);
    this.factory = this.injector.getInstance(TenantFactory.class);
    this.featureRefFactory = this.injector.getInstance(FeatureReferenceFactory.class);
    this.tenantRefFactory = this.injector.getInstance(TenantReferenceFactory.class);
  }
  
  @Test
  public void testCRUD() {
    Tenant fsoft = factory.create("fsoft");
    fsoft.addFeature(featureRefFactory.create("Dashboard"), featureRefFactory.create("Organization"), featureRefFactory.create("Test"));
    service.create(fsoft);
    
    fsoft = service.get("fsoft");
    Assert.assertEquals(3, fsoft.getFeatures().size());
    
    fsoft.removeFeature(featureRefFactory.create("Dashboard"));
    service.update(fsoft);
    fsoft = service.get("fsoft");
    Assert.assertEquals(2, fsoft.getFeatures().size());
    
    TenantReference ref = tenantRefFactory.create("fsoft");
    fsoft = ref.get();
    Assert.assertEquals(2, fsoft.getFeatures().size());
    
    service.delete(fsoft);
    Assert.assertEquals(0, service.count());
  }
}
