/**
 * 
 */
package org.ats.services.datadriven;

import java.util.UUID;
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
 * May 6, 2015
 */
@Singleton
public class DataDrivenService extends AbstractMongoCRUD<DataDriven> {

  /** .*/
  private final String COL_NAME = "data-driven";
  
  @Inject
  private DataDrivenFactory drivenFactory;
  
  @Inject
  DataDrivenService(MongoDBService mongo, Logger logger) {
    this.logger = logger;
    this.col = mongo.getDatabase().getCollection(COL_NAME);
    
    //create text index
    this.createTextIndex("name");
    
    //
    this.col.createIndex(new BasicDBObject("created_date", 1));
    this.col.createIndex(new BasicDBObject("creator._id", 1));
    this.col.createIndex(new BasicDBObject("tenant._id", 1));
    this.col.createIndex(new BasicDBObject("space._id", 1));
  }
  
  @Override
  public DataDriven transform(DBObject source) {
    String id = source.get("mix_id") == null ? UUID.randomUUID().toString() : (String)source.get("mix_id");
    DataDriven driven = drivenFactory.create(id, (String) source.get("name"), (String) source.get("data_source"));
    driven.put("created_date", source.get("created_date"));
    driven.put("active", source.get("active"));
    driven.put("_id", source.get("_id"));
    driven.put("creator", source.get("creator"));
    driven.put("space", source.get("space"));
    driven.put("tenant", source.get("tenant"));
    return driven;
  }

}
