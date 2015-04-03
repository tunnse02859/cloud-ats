/**
 * 
 */
package org.ats.services.organization;

import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.ats.common.PageList;
import org.ats.services.data.MongoDBService;
import org.ats.services.organization.base.AbstractMongoCRUD;
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
    user.put("password", source.get("password"));
    user.put("tenant", source.get("tenant"));
    user.put("spaces", source.get("spaces"));
    user.put("roles", source.get("roles"));
    return user;
  }
  
  public void restoreUser(List<DBObject> list) {
    this.col.insert(list);
  }
}
