/**
 * TrinhTV3@fsoft.com.vn
 */
package org.ats.services.project;

import java.util.logging.Logger;

import org.ats.services.data.MongoDBService;
import org.ats.services.organization.base.AbstractMongoCRUD;

import com.google.inject.Inject;
import com.mongodb.DBObject;

/**
 * @author TrinhTV3
 *
 */
public class MixProjectService extends AbstractMongoCRUD<MixProject> {
  
  /** .*/
  private final String COL_NAME = "mix-project";
  
  @Inject MixProjectFactory mpFactory;
  
  @Inject
  public MixProjectService(MongoDBService mongo, Logger logger) {
    this.col = mongo.getDatabase().getCollection(COL_NAME);
    this.logger = logger;
    
    //create text index
    this.createTextIndex("name");
  }
  
  @Override
  public MixProject transform(DBObject source) {
    
    String name = (String) source.get("name");
    String keywordId = (String) source.get("keyword_id");
    String performanceId = (String) source.get("performance_id");
    String seleniumId = (String) source.get("selenium_id");
    String creator = (String) source.get("creator");
    String _id = (String) source.get("_id");
    
    MixProject mp = mpFactory.create(_id, name, keywordId, performanceId, seleniumId, creator);
    mp.put("created_date", source.get("created_date"));
    
    return mp;
  }

}
