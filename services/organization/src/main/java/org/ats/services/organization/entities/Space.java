/**
 * 
 */
package org.ats.services.organization.entities;

import java.util.UUID;

import org.ats.services.data.common.Reference;
import org.ats.services.organization.entities.Tenant.TenantRef;

import com.mongodb.BasicDBObject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Mar 9, 2015
 */
@SuppressWarnings("serial")
public class Space extends BasicDBObject {

  /** .*/
  public static final SpaceRef ANY = new SpaceRef("*");
  
  /** .*/
  public static final SpaceRef CURRENT = new SpaceRef("_");
  
  public Space(String name, String description) {
    this.put("_id", UUID.randomUUID().toString());
    this.put("name", name);
    this.put("desc", description);
  }
  
  public String getId() {
    return this.getString("_id");
  }
  
  public String getName() {
    return this.getString("name");
  }
  
  public void setDescription(String desc) {
    this.put("desc", desc);
  }
  
  public String getDescription() {
    return this.getString("desc");
  }
  
  public void setTenant(TenantRef tenant) {
    this.put("tenant", tenant.toJSon());
  }
  
  public TenantRef getTenant() {
    return new TenantRef(((BasicDBObject) this.get("tenant")).getString("_id"));
  }
  
  public static class SpaceRef extends Reference<Space> {

    public SpaceRef(String id) {
      super(id);
    }

    @Override
    public Space get() {
      return null;
    }
    
  }
}
