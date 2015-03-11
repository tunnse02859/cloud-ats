/**
 * 
 */
package org.ats.services.organization.entities;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
  
  public void setTenant(Tenant.Reference tenant) {
    this.put("tenant", tenant);
  }
  
  public void joinSpace(Space.Reference space) {
    Object obj = this.get("spaces");
    BasicDBList spaces = obj == null ? new BasicDBList() : (BasicDBList) obj;
    spaces.add(space);
    this.put("spaces", spaces);
  }
  
  public void leaveSpace(Space.Reference space) {
    Object obj = this.get("spaces");
    BasicDBList spaces = obj == null ? new BasicDBList() : (BasicDBList) obj;
    spaces.remove(space);
    this.put("spaces", spaces);
  }
  
  public List<Space.Reference> getSpaces() {
    Object obj = this.get("spaces");
    BasicDBList spaces = obj == null ? new BasicDBList() : (BasicDBList) obj;
    List<Space.Reference> list = new ArrayList<Space.Reference>();
    for (int i = 0; i < spaces.size(); i++) {
      list.add((Space.Reference) spaces.get(i));
    }
    return list;
  }
  
  public boolean inSpace(Space.Reference space) {
    Object obj = this.get("spaces");
    BasicDBList spaces = obj == null ? new BasicDBList() : (BasicDBList) obj;
    return spaces.contains(space);
  }
  
  public Tenant.Reference getTanent() {
    return (Tenant.Reference) this.get("tenant");
  }
  
}
