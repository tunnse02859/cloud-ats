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
import org.ats.services.organization.entity.Feature.Action;
import org.ats.services.organization.entity.fatory.PermissionFactory;
import org.ats.services.organization.entity.fatory.ReferenceFactory;
import org.ats.services.organization.entity.reference.FeatureReference;
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
public class Role  extends AbstractEntity<Role> {
  
  /** .*/
  private ReferenceFactory<SpaceReference> spaceFactory;
  
  /** .*/
  private PermissionFactory permFactory;

  @Inject
  Role(ReferenceFactory<SpaceReference> spaceFactory, PermissionFactory permFactory, @Assisted String name) {
    this.put("_id", UUID.randomUUID().toString());
    this.setName(name);
    this.put("created_date", new Date());
    this.setActive(true);
    
    this.spaceFactory = spaceFactory;
    this.permFactory = permFactory;
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
  
  public void setDescription(String desc) {
    this.put("desc", desc);
  }
  
  public String getDescription() {
    return this.getString("desc");
  }
  
  public void setCreator(String email) {
	this.put("creator", email);
  }

  public String getCreator() {
	return this.getString("creator");
  }
  
  public void setSpace(SpaceReference space) {
    this.put("space", space.toJSon());
  }
  
  public SpaceReference getSpace() {
    Object obj = this.get("space");
    return obj == null ? null : spaceFactory.create(((BasicDBObject) obj).getString("_id"));
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
      list.add(permFactory.create(((BasicDBObject) permissions.get(i)).getString("rule")));
    }
    
    return Collections.unmodifiableList(list);
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
    
    /** .*/
    private ReferenceFactory<TenantReference> tenantFactory;
    
    /** .*/
    private ReferenceFactory<SpaceReference> spaceFactory;
    
    /** .*/
    private ReferenceFactory<FeatureReference> featureFactory;
    
    @Inject
    Permission(ReferenceFactory<TenantReference> tenantFactory, ReferenceFactory<SpaceReference> spaceFactory, ReferenceFactory<FeatureReference> featureFactory, @Assisted String rule) {
      this.put("rule", rule);
      this.tenantFactory = tenantFactory;
      this.spaceFactory = spaceFactory;
      this.featureFactory = featureFactory;
    }
    
    public String getRule() {
      return this.getString("rule");
    }
    
    public void setRule(String rule) {
      this.put("rule", rule);
    }
    
    public TenantReference getTenant() {
      String rule = getRule();
      String tenantId = rule.substring(rule.indexOf('@') + 1, rule.lastIndexOf(':'));
      return this.tenantFactory.create(tenantId);
    }
    
    public Reference<Space> getSpace() {
      String rule = getRule();
      String  spaceId = rule.substring(rule.lastIndexOf(':') + 1); 
      if ("*".equals(spaceId)) return Space.ANY;
      return spaceFactory.create(spaceId);
    }
    
    public Reference<Feature> getFeature() {
      String rule = getRule();
      String featureId = rule.substring(0, rule.indexOf(':'));
      if ("*".equals(featureId)) return Feature.ANY;
      return featureFactory.create(featureId);
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
      
      public String build() {
        return new StringBuilder()
          .append(feature).append(":").append(action)
          .append("@")
          .append(tenant).append(":").append(space)
          .toString();
      }
    }
  }
}
