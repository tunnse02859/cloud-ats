/**
 * 
 */
package org.ats.services.organization.event;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.ats.common.MapBuilder;
import org.ats.common.PageList;
import org.ats.services.event.Event;
import org.ats.services.organization.ActivationService;
import org.ats.services.organization.SpaceService;
import org.ats.services.organization.TenantService;
import org.ats.services.organization.entity.Space;
import org.ats.services.organization.entity.Tenant;
import org.ats.services.organization.entity.fatory.ReferenceFactory;
import org.ats.services.organization.entity.reference.TenantReference;

import com.google.inject.Inject;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import akka.actor.UntypedActor;

/**
 * @author TrinhTV3
 *
 * Email: TrinhTV3@fsoft.com.vn
 */
public class ActivationTenantActor extends UntypedActor {

  @Inject
  private ReferenceFactory<TenantReference> tenantRefFactory;
  
  @Inject
  private SpaceService spaceService;
  @Inject private TenantService tenantService;
  
  @Inject private ActivationService activationService;
  @Override
  public void onReceive(Object message) throws Exception {
    
    if (message instanceof Event) {
      
      Event event = (Event) message;
      if ("inactive-tenant".equals(event.getName())) {
        
        Tenant tenant = (Tenant) event.getSource();
        
        TenantReference ref = tenantRefFactory.create(tenant.getId());
        inactivationProcessing(ref);
        
      } else if ("inactive-ref-tenant".equals(event.getName())) {
        
        TenantReference ref = (TenantReference) event.getSource();
        inactivationProcessing(ref);
        
      } else if ("active-tenant".equals(event.getName())) {
        
        Tenant tenant = (Tenant) event.getSource();
        TenantReference ref = tenantRefFactory.create(tenant.getId());
        
        activationProcessing(ref);
        
      } else if ("active-ref-tenant".equals(event.getName())) {
        
        TenantReference ref = (TenantReference) event.getSource();
        activationProcessing(ref);
        
      } else unhandled(message);
      
    }
  }
  
  private void activationProcessing(TenantReference ref) {
    
  }
  
  private void inactivationProcessing(TenantReference ref) {
    
    System.out.println(ref.getId());
    PageList<Space> listSpace = spaceService.findSpaceInTenant(ref);
    
    listSpace.setSortable(new MapBuilder<String, Boolean>("created_date", true).build());
    List<Space> list = new ArrayList<Space>();
    while(listSpace.hasNext()) {
      
      for (Space space : listSpace.next()) {
        list.add(space);
        if (list.size() == 1000) {
          activationService.moveSpace(list);
          list.clear();
        }
      }
    }
    System.out.println(list.size());
    if (list.size() > 0) {
      activationService.moveSpace(list);
    }
    
    if (!"deadLetters".equals(getSender().path().name())) {
      getSender().tell(ref, getSelf());
    }
    
    tenantService.delete(ref.getId());
    //
  }

}
