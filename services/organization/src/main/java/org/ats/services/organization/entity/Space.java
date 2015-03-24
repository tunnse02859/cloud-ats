/**
 * 
 */
package org.ats.services.organization.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.ats.services.data.common.Reference;
import org.ats.services.organization.entity.fatory.ReferenceFactory;
import org.ats.services.organization.entity.reference.RoleReference;
import org.ats.services.organization.entity.reference.TenantReference;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
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
  public static final Reference<Space> ANY = new Reference<Space>("*") {
    @Override
    public Space get() { return null; }
  };
  
  /** .*/
  private ReferenceFactory<TenantReference> tenantFactory;
  
  /** .*/
  private ReferenceFactory<RoleReference> roleFactory;
  
  @Inject
  Space(ReferenceFactory<TenantReference> tenantFactory, ReferenceFactory<RoleReference> roleFactory, @Assisted String name) {
    this.put("_id", UUID.randomUUID().toString());
    this.put("name", name);
    this.put("created_date", new Date());
    
    this.tenantFactory = tenantFactory;
    this.roleFactory = roleFactory;
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
  
  public void setTenant(TenantReference tenant) {
    this.put("tenant", tenant.toJSon());
  }
  
  public void addRole(RoleReference... roles) {
    Object obj = this.get("roles");
    BasicDBList list = obj == null ? new BasicDBList() : (BasicDBList) obj;
    for (RoleReference role : roles) {
      list.add(role.toJSon());
    }
    this.put("roles", list);
  }
  
  public void removeRole(RoleReference role) {
    Object obj = this.get("roles");
    if (obj == null) return;
    
    BasicDBList list = (BasicDBList) obj;
    list.remove(role.toJSon());
    this.put("roles", list);
  }
  
  public boolean hasRole(RoleReference role) {
    Object obj = this.get("roles");
    return obj == null ? false : ((BasicDBList) obj).contains(role.toJSon());
  }
  
  public List<RoleReference> getRoles() {
    Object obj = this.get("roles");
    if (obj ==  null) return Collections.emptyList();
    
    BasicDBList list = (BasicDBList) obj;
    List<RoleReference> roles = new ArrayList<RoleReference>();
    for (int i = 0; i < list.size(); i++) {
      roles.add(this.roleFactory.create(((BasicDBObject) list.get(i)).getString("_id")));
    }
    
    return Collections.unmodifiableList(roles);
  }
  
  public TenantReference getTenant() {
    Object obj = this.get("tenant");
    return obj == null ? null : this.tenantFactory.create(((BasicDBObject) obj).getString("_id"));
  }
}
