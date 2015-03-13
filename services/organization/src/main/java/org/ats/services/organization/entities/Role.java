/**
 * 
 */
package org.ats.services.organization.entities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.ats.services.data.common.Reference;
import org.ats.services.organization.entities.Feature.Action;
import org.ats.services.organization.entities.Feature.FeatureRef;
import org.ats.services.organization.entities.Space.SpaceRef;
import org.ats.services.organization.entities.Tenant.TenantRef;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Mar 9, 2015
 */
@SuppressWarnings("serial")
public class Role  extends BasicDBObject {

  public Role(String name) {
    this.put("_id", UUID.randomUUID().toString());
    this.setName(name);
  }
  
  public void setDescription(String desc) {
    this.put("desc", desc);
  }
  
  public String getDescription() {
    return this.getString("desc");
  }
  
  public void setSpace(SpaceRef space) {
    this.put("space", space.toJSon());
  }
  
  public SpaceRef getSpace() {
    Object obj = this.get("space");
    return obj == null ? null : new SpaceRef(((BasicDBObject) obj).getString("_id"));
  }
  
  public void addPermission(Permission... perms) {
    Object obj = this.get("permissions");
    BasicDBList permissions = obj == null ? new BasicDBList() : (BasicDBList) obj ;
    for (Permission perm : perms) {
      permissions.add(perm);
    }
    this.put("permissions", permissions);
  }
  
  public boolean hasPermisison(Permission perm) {
    Object obj = this.get("permissions");
    return obj == null ? false : ((BasicDBList) obj).contains(perm);
  }
  
  public void removePermission(Permission perm) {
    Object obj = this.get("permissions");
    if (obj == null) return;
    
    BasicDBList permissions = (BasicDBList) obj ;
    permissions.remove(perm);
    this.put("permissions", permissions);
  }
  
  public List<Permission> getPermissions() {
    Object obj = this.get("permissions");
    if (obj == null) return Collections.emptyList();
    
    BasicDBList permissions = (BasicDBList) obj ;
    List<Role.Permission> list = new ArrayList<Role.Permission>();
    for (int i = 0; i < permissions.size(); i++) {
      list.add((Permission) permissions.get(i));
    }
    
    return Collections.unmodifiableList(list);
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
  
  /**
   * 
   * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
   *
   * Mar 12, 2015
   * 
   * This Permission represents an rule "feature:action@tenant:space
   */
  public static class Permission extends BasicDBObject {
    
    public Permission(String rule) {
      this.put("rule", rule);
    }
    
    public String getRule() {
      return this.getString("rule");
    }
    
    public void setRule(String rule) {
      this.put("rule", rule);
    }
    
    public TenantRef getTenant() {
      String rule = getRule();
      String tenantId = rule.substring(rule.indexOf('@') + 1, rule.lastIndexOf(':'));
      return new TenantRef(tenantId);
    }
    
    public SpaceRef getSpace() {
      String rule = getRule();
      String  spaceId = rule.substring(rule.lastIndexOf(':') + 1); 
      if ("*".equals(spaceId)) return Space.ANY;
      return new SpaceRef(spaceId);
    }
    
    public FeatureRef getFeature() {
      String rule = getRule();
      String featureId = rule.substring(0, rule.indexOf(':'));
      if ("*".equals(featureId)) return Feature.ANY;
      return new FeatureRef(featureId);
    }
    
    public Action getAction() {
      String rule = getRule();
      String actionId = rule.substring(rule.indexOf(':') + 1, rule.indexOf('@'));
      if ("*".equals(actionId)) return Action.ANY;
      return new Action(actionId);
    }
    
    public static final class Builder {
      
      /** .*/
      private String feature;
      
      /** .*/
      private String action;
      
      /** .*/
      private String tenant;
      
      /** .*/
      private String space;
        
      public Builder() {
      }
      
      public Builder feature(String feature) {
        this.feature = feature;
        return this;
      }
      
      public Builder action(String action) {
        this.action = action;
        return this;
      }
      
      public Builder tenant(String tenant) {
        this.tenant = tenant;
        return this;
      }
      
      public Builder space(String space) {
        this.space = space;
        return this;
      }
      
      public Permission build() {
        return new Permission(new StringBuilder()
          .append(feature).append(":").append(action)
          .append("@")
          .append(tenant).append(":").append(space)
          .toString());
      }
    }
  }
  
  public static class RoleRef extends Reference<Role> {

    public RoleRef(String id) {
      super(id);
    }

    @Override
    public Role get() {
      return null;
    }
  }
}
