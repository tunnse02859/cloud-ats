/**
 * 
 */
package org.ats.services.organization.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.ats.services.organization.entity.fatory.ReferenceFactory;
import org.ats.services.organization.entity.reference.FeatureReference;

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
public class Tenant extends BasicDBObject {

  /** .*/
  private ReferenceFactory<FeatureReference> featureFactory;
  
  @Inject
  Tenant(ReferenceFactory<FeatureReference> featureFactory, @Assisted String name) {
    this.put("_id", name);
    this.put("created_date", new Date());
    this.featureFactory = featureFactory;
  }

  public String getId() {
    return this.getString("_id");
  }
  
  public String getName() {
    return getId();
  }
  
  public void addFeature(FeatureReference... features) {
    Object obj = this.get("features");
    BasicDBList list = obj == null ? new BasicDBList() : (BasicDBList) obj;
    for (FeatureReference feature : features) {
      list.add(feature.toJSon());
    }
    this.put("features", list);
  }
  
  public void removeFeature(FeatureReference feature) {
    Object obj = this.get("features");
    if (obj == null) return;
    
    BasicDBList list = (BasicDBList) obj;
    list.remove(feature.toJSon());
    this.put("features", list);
  }
  
  public boolean hasFeature(FeatureReference feature) {
    Object obj = this.get("features");
    return obj == null ? false : ((BasicDBList) obj).contains(feature.toJSon());
  }
  
  public List<FeatureReference> getFeatures() {
    Object obj = this.get("features");
    if (obj == null) return Collections.emptyList();
    
    List<FeatureReference> features = new ArrayList<FeatureReference>();
    BasicDBList list = (BasicDBList) obj;
    for (int i = 0; i < list.size(); i++) {
      features.add(featureFactory.create(((BasicDBObject) list.get(i)).getString("_id")));
    }
    
    return Collections.unmodifiableList(features);
  }
}
