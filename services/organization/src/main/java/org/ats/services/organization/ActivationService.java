/**
 * 
 */
package org.ats.services.organization;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.ats.common.MapBuilder;
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
import org.ats.services.organization.entity.reference.RoleReference;
import org.ats.services.organization.entity.reference.SpaceReference;
import org.ats.services.organization.entity.reference.TenantReference;
import org.ats.services.organization.entity.reference.UserReference;
import org.ats.services.organization.entity.reference.FeatureReference;

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

  @Inject
  private FeatureService featureService;
  
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
  private ReferenceFactory<FeatureReference> featureRefFactory;
  
  @Inject
  private ReferenceFactory<RoleReference> roleRefFactory;
  
  @Inject
  private ReferenceFactory<UserReference> userRefFactory;
  
  @Inject
  private ReferenceFactory<TenantReference> tenantRefFactory;
  
  @Inject
  private ReferenceFactory<SpaceReference> spaceRefFactory;
  
  @Inject
  private TenantFactory tenantFactory;
  
  @Inject 
  private RoleService roleService;
  
  @Inject
  private SpaceService spaceService;
  
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
    this.roleCol = mongo.getDatabase().getCollection("inactived-role");
    
    this.logger = logger;
  }
  
  public void inActiveFeature(String id) {
    FeatureReference ref = featureRefFactory.create(id);
    Feature feature = featureService.get(id);
    if(ref.get() != null) {
      
      //Insert feature into inactive-feature
      this.featureCol.insert(feature);
      
      //Insert tenant into inactive-tenant
      PageList<Tenant> listTenant = tenantService.findIn("features", ref);
      listTenant.setSortable(new MapBuilder<String,Boolean>("created_date", true).build());
      List<Tenant> holder = new ArrayList<Tenant>();
      while(listTenant.hasNext()) {
        for(Tenant tenant:listTenant.next()) {
          holder.add(tenant);
        }
      }
      for(Tenant tenant:holder) {
        DBObject tenantObj = this.tenantCol.findOne(new BasicDBObject("_id",tenant.getId()));
        if(tenantObj == null) {
          this.tenantCol.insert(tenant);
        }
      }
      Event event = eventFactory.create(ref, "inactive-feature-ref");
      event.broadcast();
    } else {
      logger.info("Feature is inactived not available");
    }
    
  }
  
  public void inActiveFeature(Feature obj) {
    FeatureReference ref = featureRefFactory.create(obj.getId());
    if(ref.get() != null) {
      this.featureCol.insert(obj);
      PageList<Tenant> listTenant = tenantService.findIn("features", ref);
      listTenant.setSortable(new MapBuilder<String,Boolean>("created_date", true).build());
      List<Tenant> holder = new ArrayList<Tenant>();
      while(listTenant.hasNext()) {
        for(Tenant tenant:listTenant.next()) {
          holder.add(tenant);
        }
      }
      for(Tenant tenant:holder) {
        DBObject tenantObj = this.tenantCol.findOne(new BasicDBObject("_id",tenant.getId()));
        if(tenantObj == null) {
          this.tenantCol.insert(tenant);
        }
      }
      Event event = eventFactory.create(obj, "inactive-feature");
      event.broadcast();
    } else {
      logger.info("Feature is inactived not available");
    }
    
  }
  
  public void activeFeature(String id) {
    DBObject dbObj = this.featureCol.findOne(new BasicDBObject("_id",id));
    if(dbObj != null) {
      Feature feature = featureService.transform(dbObj);
      FeatureReference ref = featureRefFactory.create(feature.getId());
      Event event = eventFactory.create(ref, "active-feature-ref");
      event.broadcast();
    } else {
      logger.info("Feature is actived not available");
    }
  }
  
  public void activeFeature(Feature obj) {
    DBObject dbObj = this.featureCol.findOne(new BasicDBObject("_id",obj.getId()));
    if(dbObj != null) {
      Event event = eventFactory.create(obj, "active-feature");
      event.broadcast();
    } else {
      logger.info("Feature is actived not available");
    }
  }
  
  public void inActiveTenant(String id) {
    
    Tenant tenant = tenantService.get(id);
    
    if (tenant != null) {
      this.tenantCol.insert(tenant);
      
      TenantReference ref = tenantRefFactory.create(id);
      Event event = eventFactory.create(ref, "inactive-tenant-ref");
      event.broadcast();
    } else {
      
      logger.info("Tenant to inactived is not available");
    }
    
  }
  
  public void inActiveTenant(Tenant obj) {
    TenantReference ref = tenantRefFactory.create(obj.getId());
    Event event = eventFactory.create(ref, "inactive-tenant-ref");
    event.broadcast();
  }
  
  public void activeTenant(String id) {
    
    DBObject source = this.tenantCol.findOne(new BasicDBObject("_id", id));
    
    if (source != null) {
      Tenant tenant = tenantService.transform(source);
      tenantService.create(tenant);

      TenantReference ref = tenantRefFactory.create(id);
      Event event = eventFactory.create(ref, "active-tenant-ref");
      event.broadcast();
    } else {
      logger.info("Tenant to active is not available");
    }
    
  }
  
  public void inActiveSpace(Space obj) {
    SpaceReference ref = spaceRefFactory.create(obj.getId());
    if(ref.get() != null) {
      
      //Insert space into inactive-space
      this.spaceCol.insert(obj);
      
      //Insert role into inactive-role
      PageList<Role> listRole = roleService.query(new BasicDBObject("space", ref.toJSon()));
      List<RoleReference> holder = new ArrayList<RoleReference>();
      while (listRole.hasNext()) {
        List<Role> roles = listRole.next();
        for (Role role : roles) {
          holder.add(roleRefFactory.create(role.getId()));
        }
      }

      for(RoleReference reference : holder) {
        this.roleCol.insert(reference.get());
      }
      
      //Insert user into inactive-user
      PageList<User> listUser = userService.findUsersInSpace(ref);
      listUser.setSortable(new MapBuilder<String, Boolean>("created_date", true).build());
      while(listUser.hasNext()) {
        List<User> users = listUser.next();
        for(User user:users) {
          this.userCol.insert(user);
        }
      }
      Event event = eventFactory.create(obj, "inactive-space");
      event.broadcast();
    } else {
      logger.info("Space is inactived not available");
    }
  }
  
  public void inActiveSpace(String id) {
    SpaceReference ref = spaceRefFactory.create(id);
    if(ref.get() != null) {
      Space space = spaceService.get(id);
      //Insert space into inactive-space
      this.spaceCol.insert(space);
      
      //Insert role into inactive-role
      PageList<Role> listRole = roleService.query(new BasicDBObject("space", ref.toJSon()));
      List<RoleReference> holder = new ArrayList<RoleReference>();
      while (listRole.hasNext()) {
        List<Role> roles = listRole.next();
        for (Role role : roles) {
          holder.add(roleRefFactory.create(role.getId()));
        }
      }

      for(RoleReference reference : holder) {
        this.roleCol.insert(reference.get());
      }
      
      //Insert user into inactive-user
      PageList<User> listUser = userService.findUsersInSpace(ref);
      listUser.setSortable(new MapBuilder<String, Boolean>("created_date", true).build());
      while(listUser.hasNext()) {
        List<User> users = listUser.next();
        for(User user:users) {
          this.userCol.insert(user);
        }
      }
      Event event = eventFactory.create(ref, "inactive-space-ref");
      event.broadcast();
    } else {
      logger.info("Space is inactived not available");
    }
  }
  
  public void activeSpace(String id) {
    DBObject dbObj = this.spaceCol.findOne(new BasicDBObject("_id",id));
    if(dbObj != null) {
      SpaceReference ref = spaceRefFactory.create(id);
      Event event = eventFactory.create(ref, "active-space-ref");
      event.broadcast();
    } else {
      logger.info("Space is actived not available");
    }
  }
  
  public void activeSpace(Space obj) {
    DBObject dbObj = this.spaceCol.findOne(new BasicDBObject("_id",obj.getId()));
    if(dbObj != null) {
      Event event = eventFactory.create(obj, "active-space");
      event.broadcast();
    } else {
      logger.info("Space is actived not available");
    }
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
  
  public long countInActiveUser(DBObject query) {
    return this.query(query, this.userCol).count();
  }
  
  public long countInActiveTenant() {
    return this.tenantCol.count();
    
  }
  
  public long countSpaceIntoInActiveTenant(DBObject query) {
    return this.query(query, this.spaceCol).count();
  }
  
  public long countRoleIntoInActiveTenant(DBObject query){
    return this.query(query, this.roleCol).count();
  }
  
  public long countUser() {
    return this.userCol.count();
  }
  
  public long countTenant() {
    return this.tenantCol.count();
  }
  
  public long countSpace() {
    return this.spaceCol.count();
  }
  
  public long countRole() {
    return this.roleCol.count();
  }
  
  public long countFeature() {
    return this.featureCol.count();
  }
  public void activeUser(User obj) {
    
  }
  
  public boolean hasTenant(String id) {
    
    DBObject object = this.tenantCol.findOne(new BasicDBObject("_id", id));
    
    if (object != null) {
      return true;
    } else return false;
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
  
  public void deleteUsersBy(DBObject query) {
    
    this.userCol.remove(query);
  }
  
  public void deleteSpaceBy(DBObject query) {
    this.spaceCol.remove(query);
  }
  
  public void deleteRoleBy(DBObject query) {
    this.roleCol.remove(query);
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
