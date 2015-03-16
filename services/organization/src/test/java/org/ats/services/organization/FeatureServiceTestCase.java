/**
 * 
 */
package org.ats.services.organization;

import org.ats.services.organization.entity.Feature;
import org.ats.services.organization.entity.Feature.Action;
import org.ats.services.organization.entity.fatory.FeatureFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Mar 16, 2015
 */
public class FeatureServiceTestCase extends AbstractTestCase {
  
  private FeatureService service;
  
  private FeatureFactory factory;

  @Override
  @BeforeMethod
  public void init() throws Exception {
    super.init();
    this.service = this.injector.getInstance(FeatureService.class);
    this.factory = this.injector.getInstance(FeatureFactory.class);
  }
  
  @Test
  public void testCRUD() {
    Feature feature = factory.create("Test Feature");
    feature.addAction(new Action("create"), new Action("read"), new Action("update"), new Action("delete"));
    service.create(feature);
    
    feature = service.get(feature.getId());
    Assert.assertEquals(4, feature.getActions().size());
    
    feature.removeAction(new Action("create"));
    service.update(feature);
    
    feature = service.get(feature.getId());
    Assert.assertEquals(3, feature.getActions().size());
    
    service.delete(feature);
    Assert.assertEquals(0, service.count());
    Assert.assertNull(service.get(feature.getId()));
  }
}
