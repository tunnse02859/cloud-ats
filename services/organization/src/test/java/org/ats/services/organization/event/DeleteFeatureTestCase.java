/**
 * 
 */
package org.ats.services.organization.event;

import org.ats.services.event.Event;
import org.ats.services.organization.TenantService;
import org.ats.services.organization.entity.Feature;
import org.ats.services.organization.entity.Tenant;
import org.ats.services.organization.entity.fatory.ReferenceFactory;
import org.ats.services.organization.entity.reference.FeatureReference;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import akka.actor.UntypedActor;

import com.google.inject.Inject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Apr 14, 2015
 */
public class DeleteFeatureTestCase extends AbstractEventTestCase {

  @BeforeClass
  public void init() throws Exception {
    super.init(this.getClass().getSimpleName());
    initService();
  }
  
  @AfterClass
  public void shutdown() throws Exception {
    eventService.stop();
  }
  
  @BeforeMethod
  public void setup()  throws Exception {
    super.initData();
  }
  
  @AfterMethod
  public void tearDown() throws Exception {
    this.mongoService.dropDatabase();
  }
  
  @Test
  public void testDeleteFeature() throws InterruptedException {
    
    Tenant tenant = tenantService.list().next().get(0);
    Assert.assertEquals(tenant.getFeatures().size(), 3);
    
    eventService.setListener(DeleteFeatureListener.class);
    
    FeatureReference ref = featureRefFactory.create("functional");
    featureService.delete(ref.getId());
    Assert.assertEquals(featureService.count(), 2);

    //wait for finish process event
    waitToFinishDeleteFeature(ref);
    
    ref = featureRefFactory.create("performace");
    featureService.delete(ref.getId());
    Assert.assertEquals(featureService.count(), 1);
    
    //wait for finish process event
    waitToFinishDeleteFeature(ref);
  }
  
  static class DeleteFeatureListener extends UntypedActor {
    
    @Inject TenantService tenantService;
    
    @Inject ReferenceFactory<FeatureReference> featureRefFactory;
    
    @Override
    public void onReceive(Object message) throws Exception {
      if (message instanceof Event) {
        Event event = (Event) message;
        if ("delete-feature".equals(event.getName())) {
          Feature feature = (Feature) event.getSource();
          FeatureReference ref = featureRefFactory.create(feature.getId());
          Assert.assertEquals(tenantService.findIn("features", ref).count(), 0);
        }
      }
    }
  }
}
