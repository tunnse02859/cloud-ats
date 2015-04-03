/**
 * 
 */
package org.ats.services.organization;

import java.util.List;
import java.util.logging.Logger;

import org.ats.services.data.MongoDBService;
import org.ats.services.event.Event;
import org.ats.services.event.EventFactory;
import org.ats.services.organization.entity.Feature;
import org.ats.services.organization.entity.Space;
import org.ats.services.organization.entity.Tenant;
import org.ats.services.organization.entity.User;
import org.ats.services.organization.entity.fatory.ReferenceFactory;
import org.ats.services.organization.entity.fatory.TenantFactory;
import org.ats.services.organization.entity.fatory.UserFactory;
import org.ats.services.organization.entity.reference.TenantReference;
import org.ats.services.organization.entity.reference.UserReference;

import com.google.inject.Inject;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

/**
 * @author TrinhTV3
 *
 * Email: TrinhTV3@fsoft.com.vn
 */
public class ActivationService {

  private DBCollection featureCol;
  private DBCollection tenantCol;
  private DBCollection userCol;
  private DBCollection spaceCol;
  private DBCollection roleCole;
  private Logger logger;
  
  @Inject
  private EventFactory eventFactory;
  
  @Inject
  private UserFactory userFactory;
  
  @Inject
  private ReferenceFactory<UserReference> userRefFactory;
  
  @Inject
  private ReferenceFactory<TenantReference> tenantRefFactory;
  
  @Inject
  private TenantFactory tenantFactory;
  
  @Inject
  private UserService userService;
  
  @Inject
  private TenantService tenantService;
  
  @Inject
  public ActivationService(MongoDBService mongo, Logger logger) {
    
    this.featureCol = mongo.getDatabase().getCollection("inactived-feature");
    this.tenantCol = mongo.getDatabase().getCollection("inactived-tenant");
    this.userCol = mongo.getDatabase().getCollection("inactived-user");
    this.spaceCol = mongo.getDatabase().getCollection("inactived-space");
    this.roleCole = mongo.getDatabase().getCollection("inactived-role");
    
    this.logger = logger;
  }
  
  public void inActiveFeature(String id) {
    
  }
  
  public void inActiveFeature(Feature obj) {
    
  }
  
  public void inActiveTenant(String id) {
    
    Tenant tenant = tenantService.transform(tenantService.get(id));
    this.tenantCol.insert(tenant);
    
    TenantReference ref = tenantRefFactory.create(id);
    Event event = eventFactory.create(ref, "inactive-ref-tenant");
    event.broadcast();
    
  }
  
  public void inActiveTenant(Tenant obj) {
    
    Event event = eventFactory.create(obj, "inactive-tenant");
    event.broadcast();
  }
  
  public void activeTenant(String id) {
    
    DBObject source = this.tenantCol.findOne(new BasicDBObject("_id", id));
  }
  
  public void inActiveSpace(Space obj) {
    
  }
  
  public void inActiveSpace(String id) {
    
  }
  
  public void inActiveUser(User obj) {
    
  }
  
  public void inActiveUser(String id) {
    logger.info("inactive user has id : " + id);
    
    UserReference ref = userRefFactory.create(id);
    moveUser(ref);
    
    userService.delete(id);
  }
 
  public void moveUser(UserReference ref) {
    
    DBObject source = userService.get(ref.getId());
    
    User user = userService.transform(source);
    this.userCol.insert(user);
    
  }
  
  public long countInActiveUser() {
    return this.userCol.count();
  }
  
  public long countInActiveTenant() {
    return this.tenantCol.count();
    
  }
  
  public long countSpaceIntoInActiveTenant() {
    return this.spaceCol.count();
  }
  
  public long countRoleIntoInActiveTenant(){
    return this.roleCole.count();
  };
  public void activeUser(User obj) {
    
  }
  
  public void activeUser(String id) {
    
    logger.info("active user has id : "+ id);
    
    DBObject object = this.userCol.findOne(new BasicDBObject("_id", id));
    User user = userService.transform(object);
    userService.create(user);
    
    restoreUser(id);
    
  }
  
  public void restoreUser(String id) {
    
    this.userCol.remove(new BasicDBObject("_id", id));
    
  }
  
  public void moveUser(List<DBObject> list) {
    
    this.userCol.insert(list);
  }
  
  public void moveRole(List<DBObject> list) {
    this.roleCole.insert(list);
  }
  public void moveSpace(List<DBObject> list) {
    
    this.spaceCol.insert(list);
    /*for (Space space : list) {
      
      this.spaceCol.insert(space);
    }*/
    
  }
}
