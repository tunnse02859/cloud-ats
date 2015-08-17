/**
 * 
 */
package org.ats.services.keyword;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.logging.Logger;

import org.ats.common.PageList;
import org.ats.services.data.MongoDBService;
import org.ats.services.organization.base.AbstractMongoCRUD;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * @author NamBV2
 *
 * Aug 3, 2015
 */

@Singleton
public class CustomKeywordService extends AbstractMongoCRUD<CustomKeyword>{
  
  private final String COL_NAME = "keyword-custom";
  
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

  @SuppressWarnings("rawtypes")
  @Override
  public CustomKeyword transform(DBObject source) {
    BasicDBObject dbObject = (BasicDBObject) source;
    ObjectMapper mapper = new ObjectMapper();
    
    CustomKeyword cutomKeyword = customKeywordFactory.create(dbObject.getString("project_id"), dbObject.getString("name"));
    cutomKeyword.put("_id", dbObject.get("_id"));
    Object date = dbObject.get("created_date");
    if (date instanceof Date) {
      cutomKeyword.put("created_date", date);
    } else if (date instanceof Map) {
      Object value = ((Map) date).get("$date");
      DateTimeFormatter parser = ISODateTimeFormat.dateTime();
      cutomKeyword.put("created_date", parser.parseDateTime(value.toString()).toDate());
    }
    
    if (dbObject.get("steps") != null) {
      ArrayList actions = (ArrayList) dbObject.get("steps");
      for (Object bar : actions) {
        try {
          if (bar instanceof Map) {
            JsonNode json = mapper.valueToTree(bar);
            cutomKeyword.addAction(mapper.readTree(json.toString()));
          } else if (bar instanceof DBObject) {
            cutomKeyword.addAction(mapper.readTree(bar.toString()));
          }
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
    }
    return cutomKeyword;
  }

}
