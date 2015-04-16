/**
 * 
 */
package org.ats.services.organization;

import java.util.Date;
import java.util.logging.Logger;

import org.ats.common.PageList;
import org.ats.services.OrganizationContext;
import org.ats.services.data.MongoDBService;
import org.ats.services.event.Event;
import org.ats.services.event.EventFactory;
import org.ats.services.organization.base.AbstractMongoCRUD;
import org.ats.services.organization.base.AuthenticationService;
import org.ats.services.organization.entity.User;
import org.ats.services.organization.entity.fatory.UserFactory;
import org.ats.services.organization.entity.reference.SpaceReference;
import org.ats.services.organization.entity.reference.TenantReference;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Mar 10, 2015
 */
@Singleton
public class UserService extends AbstractMongoCRUD<User> {

  /** .*/
  private final String COL_NAME = "org-user";
  
  /** .*/
  @Inject
  private UserFactory factory;
  
  @Inject
  private EventFactory eventFactory;
  
  @Inject
  private OrganizationContext context;
  
  @Inject
  private AuthenticationService<User> authService;
  
  @Inject
  UserService(MongoDBService mongo, Logger logger) {
    this.col = mongo.getDatabase().getCollection(COL_NAME);
    this.logger = logger;
    
    //create text index
    this.createTextIndex("_id", "first_name", "last_name");
    
    //create index for spaces, tenant, role and created_date
    this.col.createIndex(new BasicDBObject("created_date", 1));
    this.col.createIndex(new BasicDBObject("tenant._id", 1));
    this.col.createIndex(new BasicDBObject("spaces._id", 1));
    this.col.createIndex(new BasicDBObject("roles._id", 1));
  }
  
  public PageList<User> findUsersInSpace(SpaceReference space) {
    return findIn("spaces",  space);
  }
  
  public PageList<User> findUserInTenant(TenantReference tenant) {
    BasicDBObject query = new BasicDBObject("tenant", tenant.toJSon());
    return query(query);
  }
  
  public User transform(DBObject source) {
    User user = factory.create((String) source.get("_id"), (String) source.get("first_name"), (String) source.get("last_name"));
    user.put("created_date", (Date) source.get("created_date"));
    user.put("active", source.get("active"));
    user.put("password", source.get("password"));
    user.put("tenant", source.get("tenant"));
    user.put("spaces", source.get("spaces"));
    user.put("roles", source.get("roles"));
    return user;
  }
  
  @Override
  public void delete(User obj) {
    if (obj == null) return;
    super.delete(obj);
    
    if (context.getUser() != null && context.getUser().getEmail().equals(obj.getEmail())) {
      authService.logOut();
    }
    
    Event event = eventFactory.create(obj, "delete-user");
    event.broadcast();
  }
  
  @Override
  public void delete(String id) {
    this.delete(this.get(id));
  }
  
  @Override
  public void active(User obj) {
    if (obj == null) return;
    super.active(obj);
    Event event = eventFactory.create(obj, "active-user");
    event.broadcast();
  }
  
  @Override
  public void active(String id) {
    this.active(this.get(id));
  }
  
  @Override
  public void inActive(User obj) {
    if (obj == null) return;
    super.inActive(obj);
    
    if (context.getUser() != null && context.getUser().getEmail().equals(obj.getEmail())) {
      authService.logOut();
    }
    
    Event event = eventFactory.create(obj, "in-active-user");
    event.broadcast();
  }
  
  @Override
  public void inActive(String id) {
    this.inActive(this.get(id));
  }
}
