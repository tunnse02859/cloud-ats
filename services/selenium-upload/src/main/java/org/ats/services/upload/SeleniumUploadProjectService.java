/**
 * 
 */
package org.ats.services.upload;

import java.util.logging.Logger;

import org.ats.services.OrganizationContext;
import org.ats.services.data.MongoDBService;
import org.ats.services.organization.SpaceService;
import org.ats.services.organization.TenantService;
import org.ats.services.organization.UserService;
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
  
  @Inject
  private OrganizationContext context;
  
  @Inject
  private TenantService tenantService;
  
  @Inject
  private UserService userService;
  
  @Inject
  private SpaceService spaceService;
  
  @Inject
  /**
   * 
   */
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
    //rebuild context
    if (context.getTenant() == null) {
      BasicDBObject tenantSource = (BasicDBObject) source.get("tenant");
      context.setTenant(tenantService.get(tenantSource.getString("_id")));
    }
    if (context.getUser() == null) {
      BasicDBObject userSource = (BasicDBObject) source.get("creator");
      context.setUser(userService.get(userSource.getString("_id")));
    }
    if (context.getSpace() == null && source.get("space") != null) {
      BasicDBObject spaceSource = (BasicDBObject) source.get("space");
      context.setSpace(spaceService.get(spaceSource.getString("_id")));
    }
    
    SeleniumUploadProject project = factory.create(context, (String) source.get("name"));
    project.put("created_date", source.get("created_date"));
    project.put("active", source.get("active"));
    project.put("_id", source.get("_id"));
    project.setStatus(source.get("status") == null ? Status.READY : Status.valueOf((String) source.get("status")));
    if(source.get("name_project_upload") != null) {
      project.setNameProjectUpload(source.get("name_project_upload").toString());
    }
    return project;
  }
}
