/**
 * 
 */
package org.ats.services.organization.event;

import java.util.logging.Logger;

import org.ats.services.data.MongoDBService;
import org.ats.services.event.Event;
import org.ats.services.organization.FeatureService;
import org.ats.services.organization.entity.Feature;
import org.ats.services.organization.entity.Tenant;
import org.ats.services.organization.entity.reference.FeatureReference;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import akka.actor.UntypedActor;

import com.google.inject.Inject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Apr 14, 2015
 */
public class ActivationFeatureTestCase extends AbstractEventTestCase {

  @BeforeMethod
  public void init() throws Exception {
    super.init(this.getClass().getSimpleName());
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
    //this.mongoService.dropDatabase();
  }
  
  @Test
  public void testInactiveFeature() throws InterruptedException {
    
    Tenant tenant = tenantService.get("Fsoft");
    
    Feature feature = featureService.get("functional");
    FeatureReference ref = featureRefFactory.create(feature.getId());
    
    Assert.assertTrue(tenant.getFeatures().contains(ref));
    
    eventService.setListener(InactiveFeatureListener.class);
    activationService.inActiveFeature(feature);
    
    Assert.assertEquals(tenantService.findIn("features", ref).count(), 0);
  }
  
  
  //@Test
  public void testActiveFeature() throws InterruptedException {
    Tenant tenant = tenantService.get("Fsoft");
    Feature feature = tenant.getFeatures().get(0).get();
    
    activationService.inActiveFeature(feature);
    
    while(featureService.count() != 2 || 
        tenantService.findIn("features", featureRefFactory.create(feature.getId())).count() != 0) {
    }
    
    eventService.setListener(ActiveFeatureListener.class);
    activationService.activeFeature("performace");
  }
  
  static class InactiveFeatureListener extends UntypedActor {

    @Inject Logger logger;
    
    @Inject 
    private FeatureService featureService;
    
    @Inject private MongoDBService mongoService;

    @Override
    public void onReceive(Object message) throws Exception {
      
      if (message instanceof FeatureReference) {
        FeatureReference ref = (FeatureReference) message;
        logger.info("inactive feature reference "+ ref.toJSon());
        
        Assert.assertEquals(featureService.count(), 2);

        mongoService.dropDatabase();
      }
    }
  }
  
  static class ActiveFeatureListener extends UntypedActor {

    @Inject Logger logger;
    
    @Inject private MongoDBService mongoService;
    
    @Inject
    private FeatureService featureService;
    
    public void onReceive(Object message) throws Exception {
      if (message instanceof Event) {
        
        Event event = (Event) message;
        
        if("active-feature-ref".equals(event.getName())) {
          
          FeatureReference ref = (FeatureReference) event.getSource();
          logger.info("active feature "+ref.toJSon());
          Assert.assertEquals(featureService.count(), 3);
          mongoService.dropDatabase();
        }
      }
    }
  }
}
