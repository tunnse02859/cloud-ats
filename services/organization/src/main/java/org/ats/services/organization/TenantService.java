/**
 * 
 */
package org.ats.services.organization;

import java.util.logging.Logger;

import org.ats.services.data.MongoDBService;
import org.ats.services.event.Event;
import org.ats.services.event.EventFactory;
import org.ats.services.organization.base.AbstractMongoCRUD;
import org.ats.services.organization.entity.Tenant;
import org.ats.services.organization.entity.fatory.ReferenceFactory;
import org.ats.services.organization.entity.fatory.TenantFactory;
import org.ats.services.organization.entity.reference.TenantReference;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Mar 10, 2015
 */
@Singleton
public class TenantService extends AbstractMongoCRUD<Tenant> {
  
  /** .*/
  private final String COL_NAME = "org-tenant";
  
  /** .*/
  @Inject
  private TenantFactory factory;
  
  @Inject
  private EventFactory eventFactory;
  
  @Inject
  private ReferenceFactory<TenantReference> tenantRefFactory;
  
  @Inject
  TenantService(MongoDBService mongo, Logger logger) {
    this.col = mongo.getDatabase().getCollection(COL_NAME);
    this.logger = logger;
    
    this.createTextIndex("_id");
    this.col.createIndex(new BasicDBObject("created_date", 1));
    this.col.createIndex(new BasicDBObject("features._id", 1));
  }

  public Tenant transform(DBObject source) {
    Tenant tenant = this.factory.create((String) source.get("_id"));
    tenant.put("created_date", source.get("created_date"));
    tenant.put("features", source.get("features"));
    return tenant;
  }
  
  @Override
  public void delete(Tenant tenant) {
    super.delete(tenant);
    
    Event event = eventFactory.create(tenant, "delete-tenant");
    event.broadcast();
    
  }
  
  @Override
  public void delete(String id) {
    super.delete(id);
    
    TenantReference ref = tenantRefFactory.create(id);
    Event event = eventFactory.create(ref, "delete-tenant-ref");
    event.broadcast();
    
  }
}



