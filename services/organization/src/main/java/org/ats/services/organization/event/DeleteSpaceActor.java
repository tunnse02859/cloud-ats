package org.ats.services.organization.event;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.inject.Inject;

import org.ats.common.MapBuilder;
import org.ats.common.PageList;
import org.ats.services.event.Event;
import org.ats.services.organization.RoleService;
import org.ats.services.organization.UserService;
import org.ats.services.organization.entity.Role;
import org.ats.services.organization.entity.Space;
import org.ats.services.organization.entity.User;
import org.ats.services.organization.entity.fatory.ReferenceFactory;
import org.ats.services.organization.entity.reference.RoleReference;
import org.ats.services.organization.entity.reference.SpaceReference;

import com.mongodb.BasicDBObject;

import akka.actor.UntypedActor;

public class DeleteSpaceActor extends UntypedActor{

  @Inject 
  private UserService userService;

  @Inject 
  private Logger logger;

  @Inject
  private ReferenceFactory<SpaceReference> spaceRefFactory;

  @Inject
  private ReferenceFactory<RoleReference> roleRefFactory;

  @Inject
  private RoleService roleService;

  @Override
  public void onReceive(Object message) throws Exception {

    if(message instanceof Event) {
      Event event = (Event) message;
      if("delete-space".equals(event.getName())) {
        Space space = (Space) event.getSource();
        SpaceReference ref = spaceRefFactory.create(space.getId());
        process(ref);
      } else if("delete-space-ref".equals(event.getName())) {
        SpaceReference ref = (SpaceReference) event.getSource();
        process(ref);
      } else {
        unhandled(message);
      }
    }

  }

  private void process(SpaceReference reference) throws InterruptedException {
    
    logger.info("Process event source: " + reference);
    
    PageList<Role> listRole = roleService.query(new BasicDBObject("space", reference.toJSon()));
    List<RoleReference> holder = new ArrayList<RoleReference>();
    while (listRole.hasNext()) {
      List<Role> roles = listRole.next();
      for (Role role : roles) {
        holder.add(roleRefFactory.create(role.getId()));
      }
    }

    for(RoleReference ref : holder) {
      roleService.delete(ref.getId());
    }
    
    PageList<User> listUser = userService.findUsersInSpace(reference);
    listUser.setSortable(new MapBuilder<String, Boolean>("created_date", true).build());
    while(listUser.hasNext()) {
      List<User> users = listUser.next();
      for(User user:users) {
        user.leaveSpace(reference);
        userService.update(user);
      }
    }

    //send processed event to listener
    while(userService.findUsersInSpace(reference).count() != 0 && roleService.query(new BasicDBObject("space", reference.toJSon())).count() != 0) {
      Thread.sleep(3000);
    }
    if (!"deadLetters".equals(getSender().path().name())) {
      getSender().tell(reference, getSelf());
    }
  }
}
