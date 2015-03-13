/**
 * 
 */
package org.ats.services.organization.entities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.ats.services.data.common.Reference;
import org.ats.services.organization.TenantService;
import org.ats.services.organization.entities.Feature.FeatureRef;

import com.google.inject.Inject;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Mar 9, 2015
 */
public class Tenant extends BasicDBObject {

  /** .*/
  private static final long serialVersionUID = 1L;
  
  public Tenant(String name) {
    this.put("_id", name);
  }

  public String getId() {
    return this.getString("_id");
  }
  
  public String getName() {
    return getId();
  }
  
  public void addFeature(FeatureRef... features) {
    Object obj = this.get("features");
    BasicDBList list = obj == null ? new BasicDBList() : (BasicDBList) obj;
    for (FeatureRef feature : features) {
      list.add(feature.toJSon());
    }
    this.put("features", list);
  }
  
  public void removeFeature(FeatureRef feature) {
    Object obj = this.get("features");
    if (obj == null) return;
    
    BasicDBList list = (BasicDBList) obj;
    list.remove(feature.toJSon());
    this.put("features", list);
  }
  
  public boolean hasFeature(FeatureRef feature) {
    Object obj = this.get("features");
    return obj == null ? false : ((BasicDBList) obj).contains(feature.toJSon());
  }
  
  public List<FeatureRef> getFeatures() {
    Object obj = this.get("features");
    if (obj == null) return Collections.emptyList();
    
    List<FeatureRef> features = new ArrayList<FeatureRef>();
    BasicDBList list = (BasicDBList) obj;
    for (int i = 0; i < list.size(); i++) {
      features.add(new FeatureRef(((BasicDBObject) list.get(i)).getString("_id")));
    }
    
    return Collections.unmodifiableList(features);
  }
  
  public static class TenantRef extends Reference<Tenant> {
    
    @Inject
    private TenantService service;

    public TenantRef(String id) {
      super(id);
    }

    @Override
    public Tenant get() {
      return service.get(id);
    }
    
  }
}
