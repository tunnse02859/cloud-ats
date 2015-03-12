/**
 * 
 */
package org.ats.services.organization.entities;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.ats.services.data.common.Reference;
import org.ats.services.organization.UserService;
import org.ats.services.organization.entities.Space.SpaceRef;
import org.ats.services.organization.entities.Tenant.TenantRef;

import com.google.inject.Inject;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Mar 9, 2015
 */
public class User extends BasicDBObject {

  /** .*/
  private static final long serialVersionUID = 1L;
  
  public User(String email, String firstName, String lastName) {
    this.put("_id", email);
    this.put("email", email);
    this.put("first_name", firstName);
    this.put("last_name", lastName);
    this.put("created_date", new Date());
  }
  
  public String getEmail() {
    return this.getString("email");
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
  
  public void setTenant(TenantRef tenant) {
    this.put("tenant", tenant.toJSon());
  }
  
  public void joinSpace(SpaceRef space) {
    Object obj = this.get("spaces");
    BasicDBList spaces = obj == null ? new BasicDBList() : (BasicDBList) obj;
    spaces.add(space.toJSon());
    this.put("spaces", spaces);
  }
  
  public void leaveSpace(SpaceRef space) {
    Object obj = this.get("spaces");
    BasicDBList spaces = obj == null ? new BasicDBList() : (BasicDBList) obj;
    spaces.remove(space.toJSon());
    this.put("spaces", spaces);
  }
  
  public List<SpaceRef> getSpaces() {
    Object obj = this.get("spaces");
    BasicDBList spaces = obj == null ? new BasicDBList() : (BasicDBList) obj;
    List<SpaceRef> list = new ArrayList<SpaceRef>();
    for (int i = 0; i < spaces.size(); i++) {
      list.add(new SpaceRef(((BasicDBObject) spaces.get(i)).getString("_id")));
    }
    return list;
  }
  
  public boolean inSpace(SpaceRef space) {
    Object obj = this.get("spaces");
    BasicDBList spaces = obj == null ? new BasicDBList() : (BasicDBList) obj;
    return spaces.contains(space.toJSon());
  }
  
  public TenantRef getTanent() {
    return new TenantRef(((BasicDBObject)this.get("tenant")).getString("_id"));
  }
  
  public static class UserRef extends Reference<User> {
    
    @Inject
    private UserService service;

    public UserRef(String id) {
      super(id);
    }

    @Override
    public User get() {
      return service.get(id);
    }
    
  }
}
