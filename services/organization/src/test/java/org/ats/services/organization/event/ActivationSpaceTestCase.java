/**
 * 
 */
package org.ats.services.organization.event;

import java.util.logging.Logger;

import org.ats.services.data.MongoDBService;
import org.ats.services.event.Event;
import org.ats.services.organization.RoleService;
import org.ats.services.organization.SpaceService;
import org.ats.services.organization.entity.reference.SpaceReference;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;

import akka.actor.UntypedActor;

import com.google.inject.Inject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Apr 14, 2015
 */
public class ActivationSpaceTestCase extends AbstractEventTestCase {

  @BeforeMethod
  public void init() throws Exception {
    super.init(this.getClass().getSimpleName());
  }
  
  static class InactiveSpaceListener extends UntypedActor {

    @Inject
    private Logger logger;
    
    @Inject
    private SpaceService spaceService;
    
    @Inject
    private RoleService roleService;
    
    @Inject 
    private MongoDBService mongoService;
    
    @Override
    public void onReceive(Object message) throws Exception {
      if(message instanceof Event) {
        
        Event event = (Event) message;
        SpaceReference ref = (SpaceReference) event.getSource();
        logger.info("inactive space reference "+ ref.toJSon());
        Assert.assertEquals(roleService.count(), 0);
        Assert.assertEquals(spaceService.count(), 1);
        
        mongoService.dropDatabase();
      }
    }
  }
  
  static class ActiveSpaceListener extends UntypedActor {

    @Inject
    private Logger logger;
    
    @Inject
    private RoleService roleService;
    
    @Inject
    private SpaceService spaceService;
    
    @Inject 
    private MongoDBService mongoService;
    
    @Override
    public void onReceive(Object message) throws Exception {
      if(message instanceof Event) {
        
        Event event = (Event) message;
        if("active-space-ref".equals(event.getName())) {
          
          SpaceReference ref = (SpaceReference) event.getSource();
          logger.info("active space reference "+ ref.toJSon());
          Assert.assertEquals(roleService.count(), 2);
          Assert.assertEquals(spaceService.count(), 2);
          
          mongoService.dropDatabase();
        }
      }
    }
  }
}
