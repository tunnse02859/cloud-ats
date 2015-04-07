/**
 * 
 */
package org.ats.services.organization;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.ats.common.PageList;
import org.ats.services.data.MongoDBService;
import org.ats.services.data.common.MongoPageList;
import org.ats.services.event.Event;
import org.ats.services.event.EventFactory;
import org.ats.services.organization.entity.Feature;
import org.ats.services.organization.entity.Role;
import org.ats.services.organization.entity.Space;
import org.ats.services.organization.entity.Tenant;
import org.ats.services.organization.entity.User;
import org.ats.services.organization.entity.fatory.ReferenceFactory;
import org.ats.services.organization.entity.fatory.TenantFactory;
import org.ats.services.organization.entity.fatory.UserFactory;
import org.ats.services.organization.entity.reference.SpaceReference;
import org.ats.services.organization.entity.reference.TenantReference;
import org.ats.services.organization.entity.reference.UserReference;

import com.google.inject.Inject;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
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
  private DBCollection roleCol;
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
  private SpaceService spaceService;
  
  @Inject
  public ActivationService(MongoDBService mongo, Logger logger) {
    
    this.featureCol = mongo.getDatabase().getCollection("inactived-feature");
    this.tenantCol = mongo.getDatabase().getCollection("inactived-tenant");
    this.userCol = mongo.getDatabase().getCollection("inactived-user");
    this.spaceCol = mongo.getDatabase().getCollection("inactived-space");
    this.roleCol = mongo.getDatabase().getCollection("inactived-role");
    
    this.logger = logger;
  }
  
  public void inActiveFeature(String id) {
    
  }
  
  public void inActiveFeature(Feature obj) {
    
  }
  
  public void inActiveTenant(String id) {
    
    Tenant tenant = tenantService.get(id);
    
    if (tenant != null) {
      this.tenantCol.insert(tenant);
      
      TenantReference ref = tenantRefFactory.create(id);
      Event event = eventFactory.create(ref, "inactive-ref-tenant");
      event.broadcast();
    } else {
      
      logger.info("Tenant to inactived is not available");
    }
    
  }
  
  public void inActiveTenant(Tenant obj) {
    
    Event event = eventFactory.create(obj, "inactive-tenant");
    event.broadcast();
  }
  
  public void activeTenant(String id) {
    
    DBObject source = this.tenantCol.findOne(new BasicDBObject("_id", id));
    
    if (source != null) {
      Tenant tenant = tenantService.transform(source);
      tenantService.create(tenant);

      TenantReference ref = tenantRefFactory.create(id);
      Event event = eventFactory.create(ref, "active-ref-tenant");
      event.broadcast();
    } else {
      logger.info("Tenant to active is not available");
    }
    
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
    
    User user = userService.get(ref.getId());
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
    return this.roleCol.count();
  }
  
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
    this.roleCol.insert(list);
  }
  
  public void moveSpace(List<DBObject> list) {
    
    this.spaceCol.insert(list);
  }
  
  public void deleteTenant(Tenant tenant) {
    this.tenantCol.remove(tenant);
  }
  
  public void deleteTenant(String id) {
    this.tenantCol.remove(new BasicDBObject("_id", id));
  }
  
  public void deleteRole(Role role) {
    
    this.roleCol.remove(role);
  }
  
  public void deleteSpace(Space space) {
    this.spaceCol.remove(space);
  }
  
  public void deleteUser(User user) {
    this.userCol.remove(user);
  }
  
  public PageList<DBObject> findSpaceIntoInActiveTenant(TenantReference ref) {
    
    BasicDBObject query = new BasicDBObject("tenant", ref.toJSon());
    
    return query(query, this.spaceCol);
  }
  
  public PageList<DBObject> findUserIntoInActiveTenant(TenantReference ref) {
    BasicDBObject query = new BasicDBObject("tenant", ref.toJSon());
    
    return query(query, this.userCol);
  }
  
  public PageList<DBObject> findRoleIntoInActiveSpace(SpaceReference ref) {
    
    BasicDBObject query = new BasicDBObject("space", ref.toJSon());
    
    return query(query, this.roleCol);
  }
  
  public PageList<DBObject> query(DBObject query, DBCollection col) {
    return query(query, 10, col);
  }

  public PageList<DBObject> query(DBObject query, int pageSize, DBCollection col) {
    return buildPageList(pageSize, col, query);
  }
  
  @SuppressWarnings("serial")
  protected PageList<DBObject> buildPageList(int pageSize, DBCollection col, DBObject query) {
    return new MongoPageList<DBObject>(pageSize, col, query) {
      @Override
      protected List<DBObject> get(int from) {
        DBObject sortable = null;
        
        if (this.sortableKeys != null && this.sortableKeys.size() > 0) {
          sortable = new BasicDBObject();
          for (Map.Entry<String, Boolean> entry : this.sortableKeys.entrySet()) {
            sortable.put(entry.getKey(), entry.getValue() ? 1 : -1);
          }
        }
        
        DBCursor cursor = null;
        if (sortable != null) {
          cursor = this.col.find(query).sort(sortable).skip(from).limit(pageSize);
        } else {
          cursor = this.col.find(query).skip(from).limit(pageSize);
        }
        
        List<DBObject> list = new ArrayList<DBObject>();
        while(cursor.hasNext()) {
          DBObject source = cursor.next();
          
            list.add(source);
          } 
        
        return list;
      }
    };
  }
  
}
