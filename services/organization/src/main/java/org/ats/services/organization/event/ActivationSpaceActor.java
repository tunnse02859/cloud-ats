/**
 * 
 */
package org.ats.services.organization.event;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.ats.common.PageList;
import org.ats.services.data.MongoDBService;
import org.ats.services.event.Event;
import org.ats.services.organization.ActivationService;
import org.ats.services.organization.RoleService;
import org.ats.services.organization.SpaceService;
import org.ats.services.organization.UserService;
import org.ats.services.organization.entity.Role;
import org.ats.services.organization.entity.Space;
import org.ats.services.organization.entity.User;
import org.ats.services.organization.entity.fatory.ReferenceFactory;
import org.ats.services.organization.entity.reference.RoleReference;
import org.ats.services.organization.entity.reference.SpaceReference;

import akka.actor.UntypedActor;

import com.google.inject.Inject;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

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
  
  @Inject
  private ActivationService activationService;
  
  @Override
  public void onReceive(Object message) throws Exception {
    
    if(message instanceof Event) {
      Event event = (Event) message;
       if("inactive-space-ref".equals(event.getName())) {
         
        logger.info("Recieved event "+message);
        processInactive(event);
      } else if("active-space-ref".equals(event.getName())) {
        
        logger.info("Recieved event "+message);
        processActive(event);
      } else {
        unhandled(message);
      }
    }
  }

  private void processActive(Event event) {
    DBCollection spaceCol = mongo.getDatabase().getCollection("inactived-space");
    DBCollection userCol = mongo.getDatabase().getCollection("inactived-user");
    SpaceReference ref = (SpaceReference) event.getSource();
    PageList<Role> listRole = roleService.query(new BasicDBObject("space", ref.toJSon()));
    //insert role into role collection
    PageList<DBObject> listRoleObj = activationService.findRoleIntoInActiveSpace(ref);
    //List<Role> listRole = new ArrayList<Role>();
    List<DBObject> listObj = new ArrayList<DBObject>();
    
    while(listRoleObj.hasNext()) {
      for(DBObject obj:listRoleObj.next()) {
        Role role = roleService.transform(obj);
        listObj.add(role);
        activationService.deleteRole(role);
        if(listObj.size() == 1000) {
          roleService.restoreRole(listObj);
          listObj.clear();
        }
      }
    }
    if (listObj.size() > 0) {
      roleService.restoreRole(listObj);
    }
    /*for(Role role:listRole) {
      roleService.create(role);
      activationService.deleteRole(role);
    }*/
    
    //insert space into user
    PageList<DBObject> listUserObj = activationService.findUserInActiveSpace(ref);
    List<User> listUser = new ArrayList<User>();
    while(listUserObj.hasNext()) {
      for(DBObject obj:listUserObj.next()) {
        User user = userService.transform(obj);
        listUser.add(user);
      }
    }
    
    for(User u:listUser) {
      User user = userService.get(u.getEmail());
      user.joinSpace(ref);
      userService.update(user);
      if(u.getSpaces().size() == user.getSpaces().size()) {
        userCol.remove(new BasicDBObject("_id",u.getEmail()));
      }
    }
    
    //Insert space in space collection
    DBObject spaceObj = spaceCol.findOne(new BasicDBObject("_id",ref.getId()));
    Space space = spaceService.transform(spaceObj);
    spaceService.create(space);
    activationService.deleteSpace(space);
    
    if(!"deadLetters".equals(getSender().path().name())) {
      getSender().tell(event, getSelf());
    }
  }

  private void processInactive(Event event) throws InterruptedException {
    SpaceReference ref = (SpaceReference) event.getSource();
    spaceService.delete(ref.getId());
    while(userService.findUsersInSpace(ref).count() != 0 && roleService.query(new BasicDBObject("space", ref.toJSon())).count() != 0) {
      Thread.sleep(300);
    }
    if(!"deadLetters".equals(getSender().path().name())) {
      getSender().tell(event,getSelf());
    }
  }
  
}
