/**
 * 
 */
package org.ats.services.organization.event;

import java.util.logging.Logger;

import org.ats.common.PageList;
import org.ats.services.data.MongoDBService;
import org.ats.services.event.Event;
import org.ats.services.organization.RoleService;
import org.ats.services.organization.SpaceService;
import org.ats.services.organization.UserService;
import org.ats.services.organization.entity.Feature;
import org.ats.services.organization.entity.Role;
import org.ats.services.organization.entity.Space;
import org.ats.services.organization.entity.fatory.ReferenceFactory;
import org.ats.services.organization.entity.reference.FeatureReference;
import org.ats.services.organization.entity.reference.SpaceReference;

import com.google.inject.Inject;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;

import akka.actor.UntypedActor;

/**
 * @author NamBV2
 *
 * Apr 7, 2015
 */
public class ActivationSpaceActor extends UntypedActor{
  
  @Inject 
  private Logger logger;
  
  @Inject
  private ReferenceFactory<SpaceReference> spaceRefFactory;
  
  @Inject
  private RoleService roleService;
  
  @Inject
  private SpaceService spaceService;
  
  @Inject
  private UserService userService;
  
  @Inject
  private MongoDBService mongo;
  
  
  @Override
  public void onReceive(Object message) throws Exception {
    logger.info("Recieved event "+message);
    if(message instanceof Event) {
      Event event = (Event) message;
      if("inactive-space".equals(event.getName())) {
        Space space = (Space) event.getSource();
        SpaceReference ref = spaceRefFactory.create(space.getId());
        processInactive(ref);
      } else if("inactive-space-ref".equals(event.getName())) {
        SpaceReference ref = (SpaceReference) event.getSource();
        processInactive(ref);
      } else if("active-space".equals(event.getName())) {
        Space space = (Space) event.getSource();
        SpaceReference ref = spaceRefFactory.create(space.getId());
        processActive(ref);
      } else if("active-space-ref".equals(event.getName())) {
        SpaceReference ref = (SpaceReference) event.getSource();
        processActive(ref);
      } else {
        unhandled(message);
      }
    }
  }

  private void processActive(SpaceReference ref) {
    DBCollection spaceCol = mongo.getDatabase().getCollection("inactived-space");
    DBCollection roleCol = mongo.getDatabase().getCollection("inactived-role");
    DBCollection userCol = mongo.getDatabase().getCollection("inactived-user");
    PageList<Role> listRole = roleService.query(new BasicDBObject("space", ref.toJSon()));
    System.out.println("------------------+++++++++++++"+listRole.count());
  }

  private void processInactive(SpaceReference ref) throws InterruptedException {
    spaceService.delete(ref.getId());
    while(userService.findUsersInSpace(ref).count() != 0 && roleService.query(new BasicDBObject("space", ref.toJSon())).count() != 0) {
      Thread.sleep(300);
    }
    PageList<Role> listRole = roleService.query(new BasicDBObject("space", ref.toJSon()));
    System.out.println("------------------+++++++++++++"+listRole.count());
    System.out.println("----2"+getSender().path().name());
    if(!"deadLetters".equals(getSender().path().name())) {
      getSender().tell(ref,getSelf());
    }
  }
  
}
