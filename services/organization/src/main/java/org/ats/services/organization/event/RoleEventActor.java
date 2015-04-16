/**
 * 
 */
package org.ats.services.organization.event;

import java.util.List;
import java.util.logging.Logger;

import org.ats.common.MapBuilder;
import org.ats.common.PageList;
import org.ats.services.event.Event;
import org.ats.services.organization.SpaceService;
import org.ats.services.organization.UserService;
import org.ats.services.organization.entity.Role;
import org.ats.services.organization.entity.Space;
import org.ats.services.organization.entity.User;
import org.ats.services.organization.entity.fatory.ReferenceFactory;
import org.ats.services.organization.entity.reference.RoleReference;

import akka.actor.UntypedActor;

import com.google.inject.Inject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Mar 24, 2015
 */
public class RoleEventActor extends UntypedActor {
  
  @Inject
  private SpaceService spaceService;
  
  @Inject
  private UserService userService;
  
  @Inject
  private ReferenceFactory<RoleReference> roleRefFactory;
  
  @Inject
  private Logger logger;

  @Override
  public void onReceive(Object message) throws Exception {
    
    if (message instanceof Event) {
      Event event = (Event) message;
      if ("delete-role".equals(event.getName())) {
        delete(event);
      } else if ("active-role".equals(event.getName())) {
        
      } else if ("in-active-role".equals(event.getName())) {
        
      }
    } else {
      unhandled(message);
    }
  }
  
  private void delete(Event event) {
    
    Role role = (Role) event.getSource();
    RoleReference ref = roleRefFactory.create(role.getId());
    logger.info("Process event delete-role " + ref.toJSon());
    
    PageList<Space> listSpace = spaceService.findIn("roles", ref);
    listSpace.setSortable(new MapBuilder<String, Boolean>("created_date", true).build());
    while(listSpace.hasNext()) {
      List<Space> spaces = listSpace.next();
      for (Space space : spaces) {
        space.removeRole(ref);
        spaceService.update(space);
      }
    }
    
    PageList<User> listUser = userService.findIn("roles", ref); 
    listUser.setSortable(new MapBuilder<String, Boolean>("created_date", true).build());
    while(listUser.hasNext()) {
      List<User> users = listUser.next();
      for (User user : users) {
        user.removeRole(ref);
        userService.update(user);
      }
    }
    
    //send processed event to listener
    if (!"deadLetters".equals(getSender().path().name())) {
      getSender().tell(ref, getSelf());
    }
  }
}
