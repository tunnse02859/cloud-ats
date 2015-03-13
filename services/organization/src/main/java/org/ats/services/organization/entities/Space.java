/**
 * 
 */
package org.ats.services.organization.entities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.ats.services.data.common.Reference;
import org.ats.services.organization.entities.Role.RoleRef;
import org.ats.services.organization.entities.Tenant.TenantRef;

import com.mongodb.BasicDBList;
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
  
  public void addRole(RoleRef... roles) {
    Object obj = this.get("roles");
    BasicDBList list = obj == null ? new BasicDBList() : (BasicDBList) obj;
    for (RoleRef role : roles) {
      list.add(role.toJSon());
    }
    this.put("roles", list);
  }
  
  public void removeRole(RoleRef role) {
    Object obj = this.get("roles");
    if (obj == null) return;
    
    BasicDBList list = (BasicDBList) obj;
    list.remove(role.toJSon());
    this.put("roles", list);
  }
  
  public boolean hasRole(RoleRef role) {
    Object obj = this.get("roles");
    return obj == null ? false : ((BasicDBList) obj).contains(role.toJSon());
  }
  
  public List<RoleRef> getRoles() {
    Object obj = this.get("roles");
    if (obj ==  null) return Collections.emptyList();
    
    BasicDBList list = (BasicDBList) obj;
    List<RoleRef> roles = new ArrayList<RoleRef>();
    for (int i = 0; i < list.size(); i++) {
      roles.add(new RoleRef(((BasicDBObject) list.get(i)).getString("_id")));
    }
    
    return Collections.unmodifiableList(roles);
  }
  
  public TenantRef getTenant() {
    Object obj = this.get("tenant");
    return obj == null ? null : new TenantRef(((BasicDBObject) obj).getString("_id"));
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
