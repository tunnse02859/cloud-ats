/**
 * 
 */
package org.ats.services.organization;

import java.util.logging.Logger;

import org.ats.services.data.MongoDBService;
import org.ats.services.organization.entity.Space;
import org.ats.services.organization.entity.fatory.SpaceFactory;

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
public class SpaceService extends AbstractMongoCRUD<Space> {
  
  /** .*/
  private final String COL_NAME = "org-space";
  
  /** .*/
  private SpaceFactory factory;
  
  @Inject
  SpaceService(MongoDBService mongo, Logger logger, SpaceFactory factory) {
    this.col = mongo.getDatabase().getCollection(COL_NAME);
    this.logger = logger;
    this.factory = factory;
    
    this.createTextIndex("name");
    this.col.createIndex(new BasicDBObject("created_date", 1));
    this.col.createIndex(new BasicDBObject("tenant._id", 1));
    this.col.createIndex(new BasicDBObject("roles._id", 1));
  }

  public Space transform(DBObject source) {
    Space space = factory.create((String) source.get("name"));
    space.put("_id", source.get("_id"));
    space.put("created_date", source.get("created_date"));
    space.put("desc", source.get("desc"));
    space.put("tenant", source.get("tenant"));
    space.put("roles", source.get("roles"));
    return space;
  }

}
