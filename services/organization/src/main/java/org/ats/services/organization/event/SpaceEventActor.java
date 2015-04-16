package org.ats.services.organization.event;

import java.util.List;
import java.util.logging.Logger;

import javax.inject.Inject;

import org.ats.common.MapBuilder;
import org.ats.common.PageList;
import org.ats.services.event.Event;
import org.ats.services.organization.RoleService;
import org.ats.services.organization.UserService;
import org.ats.services.organization.entity.Space;
import org.ats.services.organization.entity.User;
import org.ats.services.organization.entity.fatory.ReferenceFactory;
import org.ats.services.organization.entity.reference.RoleReference;
import org.ats.services.organization.entity.reference.SpaceReference;

import akka.actor.UntypedActor;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class SpaceEventActor extends UntypedActor {

  @Inject 
  private UserService userService;

  @Inject 
  private Logger logger;

  @Inject
  private ReferenceFactory<SpaceReference> spaceRefFactory;

  @Inject
  private RoleService roleService;
  
  @Override
  public void onReceive(Object message) throws Exception {

    if(message instanceof Event) {
      Event event = (Event) message;
      if ("delete-space".equals(event.getName())) {
        delete(event);
      } else if ("active-space".equals(event.getName())) {
        activation(event, true);
      } else if ("in-active-space".equals(event.getName())) {
        activation(event, false);
      }
    } else {
      unhandled(message);
    }
  }
  
  private void activation(Event event, boolean active) {
    
    Space space = (Space) event.getSource();
    SpaceReference spaceRef = spaceRefFactory.create(space.getId());
    logger.info("Process event" + (active ? " active-space " : " in-active-space ") + spaceRef.toJSon());
    
    DBObject update = new BasicDBObject("$set", new BasicDBObject("active", active));
    
    //active/inactive user
    BasicDBObject query = new BasicDBObject("spaces", new BasicDBObject("$elemMatch", spaceRef.toJSon())).append("active", !active);
    userService.bulkUpdate(query, update);
    
    //active/inactive role
    query = new BasicDBObject("space", spaceRef.toJSon()).append("active", !active);
    roleService.bulkUpdate(query, update);
    
    //notify to listener
    if (!"deadLetters".equals(getSender().path().name())) {
      getSender().tell(event, getSelf());
    }
  }

  private void delete(Event event) {

    Space space = (Space) event.getSource();
    SpaceReference spaceRef = spaceRefFactory.create(space.getId());
    logger.info("Process event delete-space " + spaceRef.toJSon());
    
    //leave user out to space
    PageList<User> listUser = userService.findUsersInSpace(spaceRef);
    listUser.setSortable(new MapBuilder<String, Boolean>("created_date", true).build());
    while(listUser.hasNext()) {
      List<User> users = listUser.next();
      for(User user:users) {
        user.leaveSpace(spaceRef);
        userService.update(user);
      }
    }
    
    //delete role of space
    for(RoleReference ref : space.getRoles()) {
      roleService.delete(ref.getId());

      //wait for event bubble processing
      while (userService.findIn("roles", ref).count() != 0) {
      }
    }

    //wait for event bubble processing
    while(userService.findUsersInSpace(spaceRef).count() != 0 
        || roleService.query(new BasicDBObject("space", spaceRef.toJSon())).count() != 0) {
    }
    
    //send processed event to listener
    if (!"deadLetters".equals(getSender().path().name())) {
      getSender().tell(event, getSelf());
    }
  }
}
