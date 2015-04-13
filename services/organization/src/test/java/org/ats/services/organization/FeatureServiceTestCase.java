/**
 * 
 */
package org.ats.services.organization;

import org.ats.common.PageList;
import org.ats.services.organization.entity.Feature;
import org.ats.services.organization.entity.Feature.Action;
import org.ats.services.organization.entity.fatory.FeatureFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
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

  @BeforeMethod
  public void init() throws Exception {
    super.init(false);
    this.service = this.injector.getInstance(FeatureService.class);
    this.factory = this.injector.getInstance(FeatureFactory.class);
  }
  
  @AfterMethod
  public void tearDown() throws Exception {
    super.tearDown();
  }
  
  @AfterClass
  public void dropDB() throws Exception {
    super.tearDown();
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
  
  @Test
  public void testList() {
    initFeature(20);
    Assert.assertEquals(20, service.count());
  }
  
  @Test 
  public void searchFeature() {
    initFeature(20);
    PageList<Feature> feature = service.search("feature");
    Assert.assertEquals(feature.count(),20);  
  }
  
  @Test 
  public void findFeature() {
    initFeature(20);
    PageList<Feature> feature = service.search("\"feature 1\"");
    Assert.assertEquals(feature.count(),11);
    
  }
  
  private void initFeature(int total) {
    for(int i = 1; i <= total; i++) {
      String name = "feature " + i;
      Feature feature = factory.create(name);
      feature .addAction(new Action("create"), new Action("read"), new Action("update"), new Action("delete"));
      service.create(feature);
    }
  }
}
