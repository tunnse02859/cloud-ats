package org.ats.services.organization.event;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.ats.common.MapBuilder;
import org.ats.common.PageList;
import org.ats.services.event.Event;
import org.ats.services.organization.SpaceService;
import org.ats.services.organization.UserService;
import org.ats.services.organization.entity.Space;
import org.ats.services.organization.entity.Tenant;
import org.ats.services.organization.entity.User;
import org.ats.services.organization.entity.fatory.ReferenceFactory;
import org.ats.services.organization.entity.reference.TenantReference;

import akka.actor.UntypedActor;

import com.google.inject.Inject;

public class DeleteTenantActor extends UntypedActor{

  @Inject
  private UserService userService;
  
  @Inject
  private SpaceService spaceService;
  
  @Inject
  private ReferenceFactory<TenantReference> tenantRefFactory;
  
  @Inject 
  private Logger logger;
  
  @Override
  public void onReceive(Object message) throws Exception {

    logger.info("Recieved event " + message);
    
    if (message instanceof Event) {
      Event event = (Event) message;
      if ("delete-tenant".equals(event.getName())) {
        Tenant tenant = (Tenant) event.getSource();
        TenantReference ref = tenantRefFactory.create(tenant.getId());
        process(ref);
      } else if ("delete-tenant-ref".equals(event.getName())) {
        TenantReference ref = (TenantReference) event.getSource();
        process(ref);
      } else {
        unhandled(message);
      }
    }
    
  }

  private void process(TenantReference ref) {
    
    PageList<User> listUser = userService.findUserInTenant(ref);
    listUser.setSortable(new MapBuilder<String, Boolean>("created_date", true).build());
    
    List<User> holder = new ArrayList<User>();
    while(listUser.hasNext()) {
      for (User user : listUser.next()) {
        holder.add(user);
      }
    }
    
    for (User user : holder) {
      userService.delete(user);
    }
    
    PageList<Space> listSpace = spaceService.findSpaceInTenant(ref);
    listSpace.setSortable(new MapBuilder<String, Boolean>("created_date", true).build());
    
    List<Space> holderSpace = new ArrayList<Space>();
    while(listSpace.hasNext()) {
      for (Space space : listSpace.next()) {
        holderSpace.add(space);
      }
    }
    for (Space space : holderSpace) {
      spaceService.delete(space);
    }
    
    if (!"deadLetters".equals(getSender().path().name())) {
      getSender().tell(ref, getSelf());
    }
    
  }
}
