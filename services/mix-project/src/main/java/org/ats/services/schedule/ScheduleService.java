/**
 * 
 */
package org.ats.services.schedule;


import java.util.logging.Logger;

import org.ats.common.PageList;
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
public class ScheduleService extends AbstractMongoCRUD<Schedule> {

  /** .*/
  private final String COL_NAME = "schedule";
  
  
  @Inject
  private ScheduleFactory factory;
  
  
  @Inject
  ScheduleService(MongoDBService mongo, Logger logger) {
    this.col = mongo.getDatabase().getCollection(COL_NAME);
    this.logger = logger;
    
    //create text index
    this.createTextIndex("name");
    
    this.col.createIndex(new BasicDBObject("created_date", 1));
    this.col.createIndex(new BasicDBObject("project_id", 1));
  }
  
  public PageList<Schedule> getSchedules(String projectId) {
    return query(new BasicDBObject("project_id", projectId));
  }

  @Override
  public Schedule transform(DBObject source) {
    Schedule schedule = factory.create((String) source.get("name"), source.get("project_id") != null ? (String) source.get("project_id") : "");
    schedule.put("created_date", source.get("created_date"));
    schedule.put("_id", source.get("_id"));
   
    schedule.put("user", source.get("user"));
    
    schedule.put("hour", source.get("hour"));
    schedule.put("minute", source.get("minute"));
    schedule.put("day", source.get("day"));
    schedule.put("dateRepeat", source.get("dateRepeat"));
    schedule.put("suites", source.get("suites"));
    schedule.put("options", source.get("options"));
    schedule.put("value_delay", source.get("value_delay"));
    
    
    return schedule;
  }
   
}
