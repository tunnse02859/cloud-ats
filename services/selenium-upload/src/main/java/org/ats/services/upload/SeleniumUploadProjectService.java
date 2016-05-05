/**
 * 
 */
package org.ats.services.upload;

import java.util.logging.Logger;

import org.ats.services.data.MongoDBService;
import org.ats.services.organization.base.AbstractMongoCRUD;
import org.ats.services.upload.SeleniumUploadProject.Status;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * @author NamBV2
 *
 * Sep 17, 2015
 */
@Singleton
public class SeleniumUploadProjectService extends AbstractMongoCRUD<SeleniumUploadProject>{
  
  private final String COL_NAME = "selenium-upload-project";
  
  @Inject
  private SeleniumUploadProjectFactory factory;
  
  /**
   * 
   */
  @Inject
  public SeleniumUploadProjectService(MongoDBService mongo, Logger logger) {
    this.col = mongo.getDatabase().getCollection(COL_NAME);
    this.logger = logger;
    
    this.createTextIndex("name");
    this.col.createIndex(new BasicDBObject("create_date",1));
    this.col.createIndex(new BasicDBObject("creator._id",1));
    this.col.createIndex(new BasicDBObject("tenant._id",1));
    this.col.createIndex(new BasicDBObject("space._id",1));
  }

  @Override
  public SeleniumUploadProject transform(DBObject source) {
    
    SeleniumUploadProject project = factory.create((String) source.get("name"), source.get("mix_id") != null ? (String) source.get("mix_id") : "");
    project.put("created_date", source.get("created_date"));
    project.put("active", source.get("active"));
    project.put("_id", source.get("_id"));
    project.setStatus(source.get("status") == null ? Status.READY : Status.valueOf((String) source.get("status")));
    if(source.get("name_project_upload") != null) {
      project.setNameProjectUpload(source.get("name_project_upload").toString());
    }
    project.put("creator", source.get("creator"));
    return project;
  }
}
