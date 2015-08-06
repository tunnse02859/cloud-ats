/**
 * 
 */
package org.ats.services.keyword;

import java.io.IOException;
import java.util.logging.Logger;

import org.ats.common.PageList;
import org.ats.services.data.MongoDBService;
import org.ats.services.datadriven.DataDrivenFactory;
import org.ats.services.datadriven.DataDrivenReference;
import org.ats.services.organization.base.AbstractMongoCRUD;
import org.ats.services.organization.entity.fatory.ReferenceFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * @author NamBV2
 *
 * Aug 3, 2015
 */

@Singleton
public class CustomKeywordService extends AbstractMongoCRUD<CustomKeyword>{
  
  private final String COL_NAME = "func-custom";
  
  @Inject
  private CustomKeywordFactory customKeywordFactory;
  
  @Inject 
  CustomKeywordService(MongoDBService mongo, Logger logger) {
    this.col = mongo.getDatabase().getCollection(COL_NAME);
    this.logger = logger;
    this.createTextIndex("name");
    this.col.createIndex(new BasicDBObject("created_date",1));
    this.col.createIndex(new BasicDBObject("project_id",1));
  }
  
  public PageList<CustomKeyword> getCustomKeywords(String projectId) {
    return query(new BasicDBObject("project_id", projectId));
  }

  @Override
  public CustomKeyword transform(DBObject source) {
    BasicDBObject dbObject = (BasicDBObject) source;
    ObjectMapper mapper = new ObjectMapper();
    
    CustomKeyword cutomKeyword = customKeywordFactory.create(dbObject.getString("project_id"), dbObject.getString("name"));
    cutomKeyword.put("_id", dbObject.get("_id"));
    cutomKeyword.put("created_date", dbObject.get("created_date"));
    
    if(dbObject.get("steps") != null) {
      BasicDBList actions = (BasicDBList) dbObject.get("steps");
      for(Object obj : actions) {
        try {
          cutomKeyword.addAction(mapper.readTree(obj.toString()));
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
    }
    return cutomKeyword;
  }

}
