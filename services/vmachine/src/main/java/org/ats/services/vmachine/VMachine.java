/**
 * 
 */
package org.ats.services.vmachine;

import javax.annotation.Nullable;

import org.ats.services.organization.entity.AbstractEntity;
import org.ats.services.organization.entity.fatory.ReferenceFactory;
import org.ats.services.organization.entity.reference.SpaceReference;
import org.ats.services.organization.entity.reference.TenantReference;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.mongodb.BasicDBObject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Jul 2, 2015
 */
@SuppressWarnings("serial")
public class VMachine extends AbstractEntity<VMachine> {
  
  @Inject
  private ReferenceFactory<TenantReference> tenantRefFactory;
  
  @Inject
  private ReferenceFactory<SpaceReference> spaceRefFactory;
  
  @Inject
  VMachine(
      @Assisted("_id") String id,
      @Assisted("tenant") TenantReference tenant, 
      @Assisted("space") @Nullable SpaceReference space, 
      @Assisted("isSystem") boolean isSystem, 
      @Assisted("hasUI") boolean hasUI,
      @Assisted("public_ip") @Nullable String publicIp,
      @Assisted("private_ip") String privateIp,
      @Assisted("status") @Nullable Status status) {
    
    this.put("_id", id);
    this.put("tenant", tenant.toJSon());
    this.put("space", space == null ? null : space.toJSon());
    this.put("system", isSystem);
    this.put("ui", hasUI);
    this.put("public_ip", publicIp);
    this.put("private_ip", privateIp);
    this.put("status", status.toString());
  }
  
  public String getId() {
    return this.getString("_id");
  }
  
  public boolean isSystem() {
    return this.getBoolean("system");
  }
  
  public boolean hasUI() {
    return this.getBoolean("ui");
  }
  
  public void setPublicIp(String publicIp) {
    this.put("public_ip", publicIp);
  }
  
  public String getPublicIp() {
    return this.getString("public_ip");
  }
  
  public String getPrivateIp() {
    return this.getString("private_ip");
  }
  
  public TenantReference getTenant() {
    BasicDBObject obj = (BasicDBObject) this.get("tenant");
    return tenantRefFactory.create(obj.getString("_id"));
  }
  
  public SpaceReference getSpace() {
    BasicDBObject obj = (BasicDBObject) this.get("space");
    return obj == null ? null : spaceRefFactory.create(obj.getString("_id"));
  }
  
  public void setStatus(Status status) {
    this.put("status", status.toString());
  }
  
  public Status getStatus() {
    return this.get("status") == null ? null : Status.valueOf(this.getString("status"));
  }
  
  public static enum Status {
    Started, InProgress, Stopped, Error, Initializing
  }
}
