/**
 * 
 */
package org.ats.services.organization.event;

import java.util.ArrayList;
import java.util.List;

import org.ats.common.MapBuilder;
import org.ats.common.PageList;
import org.ats.services.event.Event;
import org.ats.services.organization.ActivationService;
import org.ats.services.organization.RoleService;
import org.ats.services.organization.SpaceService;
import org.ats.services.organization.TenantService;
import org.ats.services.organization.UserService;
import org.ats.services.organization.entity.Role;
import org.ats.services.organization.entity.Space;
import org.ats.services.organization.entity.Tenant;
import org.ats.services.organization.entity.User;
import org.ats.services.organization.entity.fatory.ReferenceFactory;
import org.ats.services.organization.entity.reference.SpaceReference;
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
  
  @Inject
  private UserService userService;
  
  @Inject
  private RoleService roleService;
  
  @Inject
  private ReferenceFactory<SpaceReference> spaceRefFactory;
  
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
    
    BasicDBObject query = new BasicDBObject("tenant", ref.toJSon());
    
    // restore spaces
    PageList<DBObject> listSpace = activationService.findSpaceIntoInActiveTenant(ref);
    
    listSpace.setSortable(new MapBuilder<String, Boolean>("created_date", true).build());
    List<DBObject> list = new ArrayList<DBObject>();
    
    while(listSpace.hasNext()) {
      
      for (DBObject source : listSpace.next()) {
        
        Space space = spaceService.transform(source);
        
        // restore role
        SpaceReference refSpace = spaceRefFactory.create(space.getId());
        
        BasicDBObject querySpace = new BasicDBObject("space", refSpace.toJSon());
        
        PageList<DBObject> listRole = activationService.findRoleIntoInActiveSpace(refSpace);
        listRole.setSortable(new MapBuilder<String, Boolean>("created_date", true).build());
        List<DBObject> listRoleObject = new ArrayList<DBObject>();
        
        while (listRole.hasNext()) {
          
          for (DBObject sourceRole : listRole.next()) {
            
            Role role = roleService.transform(sourceRole);
            
            listRoleObject.add(role);
            
            if (listRoleObject.size() == 1000) {
              roleService.restoreRole(listRoleObject);
              listRoleObject.clear();
            }
          }
        }
        
        if (listRoleObject.size() > 0) {
          roleService.restoreRole(listRoleObject);
        }
        
        // delete role in activated space
        activationService.deleteRoleBy(querySpace);
        
        // continue restore spaces
        list.add(space);
        
        if (list.size() == 1000) {
          spaceService.restoreSpace(list);
          list.clear();
        }
      }
    }
    
    if (list.size() > 0) {
      spaceService.restoreSpace(list);
    }
    //delete space if it belong to activated tenant
    activationService.deleteSpaceBy(query);
    
    // Restore user
    PageList<DBObject> listUser = activationService.findUserIntoInActiveTenant(ref);
    listUser.setSortable(new MapBuilder<String, Boolean>("created_date", true).build());
    
    list = new ArrayList<DBObject>();
    while (listUser.hasNext()) {
      
      for (DBObject source : listUser.next()) {
        
        User user = userService.transform(source);
        
        list.add(user);
        
        
        if (list.size() == 1000) {
          userService.restoreUser(list);
          
          list.clear();
        }
      }
    }
    
    if (list.size() > 0) {
      userService.restoreUser(list);
    }
    
    // delete user if users belong to activated tenant
    activationService.deleteUsersBy(query);
    // delete activated tenant 
    
    activationService.deleteTenant(ref.getId());
    
    while (activationService.countSpaceIntoInActiveTenant(new BasicDBObject("tenant", ref.toJSon())) != 0
       && activationService.countInActiveUser(new BasicDBObject("tenant", ref.toJSon())) != 0 
       && activationService.hasTenant(ref.getId())) {

    }
    
    if (!"deadLetters".equals(getSender().path().name())) {
      getSender().tell(ref, getSelf());
    }
  }
  
  private void inactivationProcessing(TenantReference ref) throws InterruptedException {
    
    PageList<Space> listSpace = spaceService.findSpaceInTenant(ref);
    
    listSpace.setSortable(new MapBuilder<String, Boolean>("created_date", true).build());
    List<DBObject> list = new ArrayList<DBObject>();
    
    // moving spaces
    while(listSpace.hasNext()) {
      
      for (Space space : listSpace.next()) {
        
        // moving role in moved space
        SpaceReference spaceRef = spaceRefFactory.create(space.getId());
        PageList<Role> listRole = roleService.query(new BasicDBObject("space", spaceRef.toJSon()));
        listRole.setSortable(new MapBuilder<String, Boolean>("created_date", true).build());
        
        List<DBObject> listRoleObject = new ArrayList<DBObject>(1000);
        
        while(listRole.hasNext()) {
          for (Role role : listRole.next()) {
            
            listRoleObject.add(role);
            if (listRoleObject.size() == 1000) {
              activationService.moveRole(listRoleObject);
            }
          }
        }
        if (listRoleObject.size() > 0) {
          activationService.moveRole(listRoleObject);
        }
        
        // continue handle for moving spaces
        list.add(space);
        if (list.size() == 1000) {
          activationService.moveSpace(list);
          list.clear();
        }
      }
    }
    if (list.size() > 0) {
      activationService.moveSpace(list);
    }
    
    // moving user
    PageList<User> listUser = userService.findUserInTenant(ref);
    
    listUser.setSortable(new MapBuilder<String, Boolean>("created_date", true).build());
    list = new ArrayList<DBObject>(1000);
    while (listUser.hasNext()) {
      for (User user : listUser.next()) {
        
        list.add(user);
        if (list.size() == 1000) {
          activationService.moveUser(list);
        }
      }
    }
    
    if (list.size() > 0) {
      activationService.moveUser(list);
    }

    // delete tenant
    tenantService.delete(ref.getId());
    //
    
    while (userService.query(new BasicDBObject("tenant", ref.toJSon())).count() != 0 &&
        spaceService.query(new BasicDBObject("tenant", ref.toJSon())).count() != 0
        && (tenantService.get(ref.getId()) != null)) {
      
    }
    
    if (!"deadLetters".equals(getSender().path().name())) {
      getSender().tell(ref, getSelf());
    }
    
  }

}
