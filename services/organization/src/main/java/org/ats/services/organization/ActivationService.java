/**
 * 
 */
package org.ats.services.organization;

import java.util.logging.Logger;

import org.ats.services.data.MongoDBService;
import org.ats.services.event.Event;
import org.ats.services.event.EventFactory;
import org.ats.services.organization.entity.Feature;
import org.ats.services.organization.entity.Space;
import org.ats.services.organization.entity.Tenant;
import org.ats.services.organization.entity.User;
import org.ats.services.organization.entity.fatory.ReferenceFactory;
import org.ats.services.organization.entity.fatory.UserFactory;
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

//  private final String COL_INACTIVE_FEATURE = "inactived-feature";
//  private final String COL_INACTIVE_TENANT = "inactived-tenant";
//  private final String COL_INACTIVE_SPACE = "inactived-space";
//  private final String COL_INACTIVE_USER = "inactived-user";
  
  private DBCollection featureCol;
  private DBCollection tenantCol;
  private DBCollection userCol;
  private DBCollection spaceCol;
  private Logger logger;
  
  @Inject
  private EventFactory eventFactory;
  
  @Inject
  private UserFactory userFactory;
  
  @Inject
  private ReferenceFactory<UserReference> userRefFactory;
  
  @Inject
  private UserService userService;
  
  @Inject
  public ActivationService(MongoDBService mongo, Logger logger) {
    
    this.featureCol = mongo.getDatabase().getCollection("inactived-feature");
    this.tenantCol = mongo.getDatabase().getCollection("inactived-tenant");
    this.userCol = mongo.getDatabase().getCollection("inactived-user");
    this.spaceCol = mongo.getDatabase().getCollection("inactived-space");
    
    this.logger = logger;
  }
  
  public void inActiveFeature(String id) {
    
  }
  
  public void inActiveFeature(Feature obj) {
    
  }
  
  public void inActiveTenant(String id) {
    
  }
  
  public void inActiveTenant(Tenant obj) {
    
  }
  
  public void inActiveSpace(Space obj) {
    
  }
  
  public void inActiveSpace(String id) {
    
  }
  
  public void inActiveUser(User obj) {
    
    Event event = eventFactory.create(obj, "inactive-user");
    event.broadcast();
  }
  
  public void inActiveUser(String id) {
    logger.info("inactive user has id : " + id);
    
    UserReference ref = userRefFactory.create(id);
    Event event = eventFactory.create(ref, "inactive-ref-user");
    event.broadcast();
  }
 
  public void moveUser(UserReference ref) {
    
    DBObject source = userService.get(ref.getId());
    
    User user = userService.transform(source);
    this.userCol.insert(user);
    
  }
  
  public long countInActiveUser() {
    return this.userCol.count();
  }
  
  public void activeUser(User obj) {
    
    Event event = eventFactory.create(obj, "active-user");
    event.broadcast();
  }
  
  public void activeUser(String id) {
    
    logger.info("active user has id : "+ id);
    
    UserReference ref = userRefFactory.create(id);
    Event event = eventFactory.create(ref, "active-ref-user");
    event.broadcast();
  }
  
  public void restoreUser(UserReference ref) {
    
    this.userCol.remove(new BasicDBObject("_id", ref.getId()));
    
  }
}
