package org.ats.services.organization.event;

import java.util.logging.Logger;

import org.ats.common.MapBuilder;
import org.ats.common.PageList;
import org.ats.services.event.Event;
import org.ats.services.organization.RoleService;
import org.ats.services.organization.SpaceService;
import org.ats.services.organization.UserService;
import org.ats.services.organization.entity.Space;
import org.ats.services.organization.entity.Tenant;
import org.ats.services.organization.entity.fatory.ReferenceFactory;
import org.ats.services.organization.entity.reference.TenantReference;

import akka.actor.UntypedActor;

import com.google.inject.Inject;
import com.mongodb.BasicDBObject;

public class DeleteTenantActor extends UntypedActor{

  @Inject
  private UserService userService;
  
  @Inject
  private SpaceService spaceService;
  
  @Inject
  private RoleService roleService;
  
  @Inject
  private ReferenceFactory<TenantReference> tenantRefFactory;
  
  @Inject 
  private Logger logger;
  
  @Override
  public void onReceive(Object message) throws Exception {

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
    
    logger.info("Process event source: " + ref);

    //Delete all user in tenant
    userService.deleteBy(new BasicDBObject("tenant", ref.toJSon()));
    
    //Delete all role in tenant's spaces
    PageList<Space> listSpace = spaceService.findSpaceInTenant(ref);
    listSpace.setSortable(new MapBuilder<String, Boolean>("created_date", true).build());
    while(listSpace.hasNext()) {
      for (Space space : listSpace.next()) {
        roleService.deleteBy(new BasicDBObject("space", new BasicDBObject("_id", space.getId())));
      }
    }
    
    //Delete all tenant's space
    spaceService.deleteBy(new BasicDBObject("tenant", ref.toJSon()));
    
    //notify to listener
    if (!"deadLetters".equals(getSender().path().name())) {
      getSender().tell(ref, getSelf());
    }
    
  }
}
