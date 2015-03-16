/**
 * 
 */
package org.ats.services.organization;

import java.util.logging.Logger;

import org.ats.services.data.MongoDBService;
import org.ats.services.organization.base.AbstractMongoCRUD;
import org.ats.services.organization.entity.Role;
import org.ats.services.organization.entity.fatory.RoleFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Mar 13, 2015
 */
@Singleton
public class RoleService extends AbstractMongoCRUD<Role> {
  
  /** .*/
  private final String COL_NAME = "org-role";
  
  /** .*/
  private RoleFactory factory;
  
  @Inject
  RoleService(MongoDBService mongo, Logger logger, RoleFactory factory) {
    this.col = mongo.getDatabase().getCollection(COL_NAME);
    this.logger = logger;
    this.factory = factory;
    
    this.createTextIndex("name");
    this.col.createIndex(new BasicDBObject("created_date", 1));
    this.col.createIndex(new BasicDBObject("space._id", 1));
  }

  public Role transform(DBObject source) {
    Role role = factory.create((String) source.get("name"));
    role.put("_id", source.get("_id"));
    role.put("created_date", source.get("created_date"));
    role.put("desc", source.get("desc"));
    role.put("space", source.get("space"));
    role.put("permissions", source.get("permissions"));
    return role;
  }

}
