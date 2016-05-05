/**
 * 
 */
package org.ats.services.upload;

import java.util.Date;
import java.util.UUID;

import org.ats.services.OrganizationContext;
import org.ats.services.organization.entity.AbstractEntity;
import org.ats.services.organization.entity.User;
import org.ats.services.organization.entity.fatory.ReferenceFactory;
import org.ats.services.organization.entity.reference.SpaceReference;
import org.ats.services.organization.entity.reference.TenantReference;
import org.ats.services.organization.entity.reference.UserReference;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.mongodb.BasicDBObject;

/**
 * @author NamBV2
 *
 * Sep 17, 2015
 */
@SuppressWarnings("serial")
public class SeleniumUploadProject extends AbstractEntity<SeleniumUploadProject>{
  
  private ReferenceFactory<TenantReference> tenantRefFactory;
  private ReferenceFactory<UserReference> userRefFactory;
  private ReferenceFactory<SpaceReference> spaceRefFactory;
  
  public static enum Status {
    READY, RUNNING
  }
  
  @Inject
  /**
   * 
   */
  public SeleniumUploadProject(ReferenceFactory<TenantReference> tenantRefFactory,
      ReferenceFactory<UserReference> userRefFactory,
      ReferenceFactory<SpaceReference> spaceRefFactory,
      OrganizationContext context,
      @Assisted("name") String name, @Assisted("mix_id") String mix_id) {
    this.tenantRefFactory = tenantRefFactory;
    this.userRefFactory = userRefFactory;
    this.spaceRefFactory = spaceRefFactory;
    
    if(context == null || context.getUser() == null)
      throw new IllegalStateException("You need logged in system to creat new functional project");
    
    User user = context.getUser();
    this.put("name", name);
    this.put("mix_id", mix_id);
    this.put("creator", new BasicDBObject("_id", user.getEmail()));
    
    if(context.getSpace() != null) {
      this.put("space", new BasicDBObject("_id", context.getSpace().getId()));
    }
    
    this.put("tenant", user.getTanent().toJSon());
    this.put("created_date", new Date());
    this.setActive(true);
    this.put("_id", UUID.randomUUID().toString());
    setStatus(Status.READY);
  }
  
  public void setMixId(String id) {
    this.put("mix_id", id);
  }
  
  public String getMixId() {
    return this.getString("mix_id");
  }
  
  public void setNameProjectUpload(String name) {
    this.put("name_project_upload", name);
  }
  
  public String getNameProjectUpload() {
    return this.getString("name_project_upload") != null ? this.getString("name_project_upload") : null;
  }
  
  public void setRawData(byte[] file) {
    this.put("raw", file != null ? file : null);
  }
  
  public byte[] getRawData() {
    return this.get("raw") != null ? (byte[])this.get("raw") : null;
  }
  
  public String getId() {
    return this.getString("_id");
  }
  
  public Status getStatus() {
    return this.get("status") != null ? Status.valueOf(this.getString("status")) : Status.READY;
  }
  
  public void setStatus(Status status) {
    this.put("status", status.toString());
  }
  
  public UserReference getCreator() {
    return userRefFactory.create(((BasicDBObject)this.get("creator")).getString("_id"));
  }
  
  public SpaceReference getSpace() {
    if (this.get("space") == null) return null;
    BasicDBObject obj = (BasicDBObject) this.get("space");
    return spaceRefFactory.create(obj.getString("_id"));
  }
  
  public TenantReference getTenant() {
    return tenantRefFactory.create(((BasicDBObject)this.get("tenant")).getString("_id"));
  }
}