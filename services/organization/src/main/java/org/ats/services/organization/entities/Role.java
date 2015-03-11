/**
 * 
 */
package org.ats.services.organization.entities;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Mar 9, 2015
 */
public class Role  extends BasicDBObject {

  /** .*/
  private static final long serialVersionUID = 1L;
  
  public Role(String name) {
    this.put("_id", UUID.randomUUID().toString());
    this.setName(name);
  }
  
  public void addPermission(Role.Permission... perms) {
    Object obj = this.get("permissions");
    BasicDBList permissions = obj == null ? new BasicDBList() : (BasicDBList) obj ;
    for (Role.Permission perm : perms) {
      permissions.add(perm);
    }
    this.put("permissions", permissions);
  }
  
  public boolean hasPermisison(String rule) {
    Role.Permission perm = new Role.Permission(rule);
    Object obj = this.get("permissions");
    BasicDBList permissions = obj == null ? new BasicDBList() : (BasicDBList) obj ;
    return permissions.contains(perm);
  }
  
  public void removePermission(String rule) {
    this.removePermission(new Role.Permission(rule));
  }
  
  public void removePermission(Role.Permission perm) {
    Object obj = this.get("permissions");
    BasicDBList permissions = obj == null ? new BasicDBList() : (BasicDBList) obj ;
    permissions.remove(perm);
    this.put("permissions", permissions);
  }
  
  public List<Role.Permission> getPermissions() {
    Object obj = this.get("permissions");
    BasicDBList permissions = obj == null ? new BasicDBList() : (BasicDBList) obj ;
    
    List<Role.Permission> list = new ArrayList<Role.Permission>();
    for (int i = 0; i < permissions.size(); i++) {
      list.add((Permission) permissions.get(i));
    }
    return list;
  }
  
  public void setName(String name) {
    this.put("name", name);
  }
  
  public String getName() {
    return this.getString("name");
  }
  
  public String getId() {
    return this.getString("_id");
  }
  
  public static class Permission extends BasicDBObject {

    /** .*/
    private static final long serialVersionUID = 1L;
    
    public Permission(String rule) {
      this.put("rule", rule);
    }
    
    public String getRule() {
      return this.getString("rule");
    }
    
    public void setRule(String rule) {
      this.put("rule", rule);
    }
  }
}
