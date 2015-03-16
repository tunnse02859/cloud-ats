/**
 * 
 */
package org.ats.services.organization.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.ats.services.organization.entity.fatory.RoleReferenceFactory;
import org.ats.services.organization.entity.fatory.SpaceReferenceFactory;
import org.ats.services.organization.entity.fatory.TenantReferenceFactory;
import org.ats.services.organization.entity.reference.RoleReference;
import org.ats.services.organization.entity.reference.SpaceReference;
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
public class User extends BasicDBObject {
  
  /** .*/
  private RoleReferenceFactory roleFactory;
  
  /** .*/
  private SpaceReferenceFactory spaceFactory;
  
  /** .*/
  private TenantReferenceFactory tenantFactory;
  
  @Inject
  User(RoleReferenceFactory roleFactory, SpaceReferenceFactory spaceFactory, TenantReferenceFactory tenantFactory,
      @Assisted("email") String email, @Assisted("firstName") String firstName,  @Assisted("lastName") String lastName) {
    this.put("_id", email);
    this.put("first_name", firstName);
    this.put("last_name", lastName);
    this.put("created_date", new Date());
    
    this.roleFactory = roleFactory;
    this.spaceFactory = spaceFactory;
    this.tenantFactory = tenantFactory;
  }
  
  public void setPassword(String password) {
    this.put("password", password);
  }
  
  public String getPassword() {
    return this.getString("password");
  }
  
  public String getEmail() {
    return this.getString("_id");
  }
  
  public void setFirstName(String firstName) {
    this.put("first_name", firstName);
  }
  
  public String getFirstName() {
    return this.getString("first_name");
  }
  
  public void setLastName(String lastName) {
    this.put("last_name", lastName);
  }
  
  public String getLastName() {
    return this.getString("last_name");
  }
  
  public void setTenant(TenantReference tenant) {
    this.put("tenant", tenant.toJSon());
  }
  
  public void joinSpace(SpaceReference space) {
    Object obj = this.get("spaces");
    BasicDBList spaces = obj == null ? new BasicDBList() : (BasicDBList) obj;
    spaces.add(space.toJSon());
    this.put("spaces", spaces);
  }
  
  public void leaveSpace(SpaceReference space) {
    Object obj = this.get("spaces");
    if (obj == null) return;
    
    BasicDBList spaces = (BasicDBList) obj;
    spaces.remove(space.toJSon());
    this.put("spaces", spaces);
  }
  
  public List<SpaceReference> getSpaces() {
    Object obj = this.get("spaces");
    if (obj == null) return Collections.emptyList();
    BasicDBList spaces = (BasicDBList) obj;
    List<SpaceReference> list = new ArrayList<SpaceReference>();
    for (int i = 0; i < spaces.size(); i++) {
      list.add(spaceFactory.create(((BasicDBObject) spaces.get(i)).getString("_id")));
    }
    return Collections.unmodifiableList(list);
  }
  
  public boolean inSpace(SpaceReference space) {
    Object obj = this.get("spaces");
    return obj == null ? false : ((BasicDBList) obj).contains(space.toJSon());
  }
  
  public TenantReference getTanent() {
    Object obj = this.get("tenant");
    return obj == null ? null : tenantFactory.create(((BasicDBObject) obj).getString("_id"));
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
      roles.add(roleFactory.create(((BasicDBObject) list.get(i)).getString("_id")));
    }
    
    return Collections.unmodifiableList(roles);
  }
}
