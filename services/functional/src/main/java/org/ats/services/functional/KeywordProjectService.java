/**
 * 
 */
package org.ats.services.functional;

import java.util.logging.Logger;

import org.ats.services.data.MongoDBService;
import org.ats.services.organization.base.AbstractMongoCRUD;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * May 4, 2015
 */
@Singleton
public class KeywordProjectService extends AbstractMongoCRUD<KeywordProject>{

  /** .*/
  private final String COL_NAME = "func-project";
  
  @Inject
  private KeywordProjectFactory factory;
  
  @Inject
  KeywordProjectService(MongoDBService mongo, Logger logger) {
    this.col = mongo.getDatabase().getCollection(COL_NAME);
    this.logger = logger;
    
    //create text index
    this.createTextIndex("name");
    
    //
    this.col.createIndex(new BasicDBObject("created_date", 1));
    this.col.createIndex(new BasicDBObject("creator._id", 1));
    this.col.createIndex(new BasicDBObject("tenant._id", 1));
    this.col.createIndex(new BasicDBObject("space._id", 1));
  }
  
  @Override
  public KeywordProject transform(DBObject source) {
    KeywordProject project = factory.create((String) source.get("name"));
    project.put("created_date", source.get("created_date"));
    project.put("active", source.get("active"));
    project.put("_id", source.get("_id"));
    project.put("creator", source.get("creator"));
    project.put("space", source.get("space"));
    project.put("tenant", source.get("tenant"));
    project.put("suites", source.get("suites"));
    return project;
  }

}
