/**
 * 
 */
package org.ats.services.functional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.ats.common.PageList;
import org.ats.services.OrganizationContext;
import org.ats.services.organization.entity.AbstractEntity;
import org.ats.services.organization.entity.User;
import org.ats.services.organization.entity.fatory.ReferenceFactory;
import org.ats.services.organization.entity.reference.SpaceReference;
import org.ats.services.organization.entity.reference.TenantReference;
import org.ats.services.organization.entity.reference.UserReference;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Apr 22, 2015
 */
@SuppressWarnings("serial")
public class KeywordProject extends AbstractEntity<KeywordProject> {
  
  private ReferenceFactory<TenantReference> tenantRefFactory;
  private ReferenceFactory<UserReference> userRefFactory;
  private ReferenceFactory<SpaceReference> spaceRefFactory;
  private ReferenceFactory<SuiteReference> suiteRefFactory;
  
  private KeywordProjectService projectService;
  
  @Inject
  KeywordProject(ReferenceFactory<TenantReference> tenantRefFactory, 
      ReferenceFactory<UserReference> userRefFactory,
      ReferenceFactory<SpaceReference> spaceRefFactory,
      ReferenceFactory<SuiteReference> suiteRefFactory,
      KeywordProjectService projectService,
      OrganizationContext context,
      @Assisted("name") String name) {
    
    this.tenantRefFactory = tenantRefFactory;
    this.userRefFactory = userRefFactory;
    this.spaceRefFactory = spaceRefFactory;
    this.suiteRefFactory = suiteRefFactory;
    this.projectService = projectService;
    
    if (context == null || context.getUser() == null) 
      throw new IllegalStateException("You need logged in system to creat new functional project");
    
    User user = context.getUser();
    this.put("name", name);
    this.put("creator", new BasicDBObject("_id", user.getEmail()));
    
    if (context.getSpace() != null) {
      this.put("space", new BasicDBObject("_id", context.getSpace().getId()));
    }
    
    this.put("tenant", user.getTanent().toJSon());
    this.put("created_date", new Date());
    this.setActive(true);
    this.put("_id", UUID.randomUUID().toString());
    this.put("suites", null);
  }
  
  public String getId() {
    return this.getString("_id");
  }
  
  public UserReference getCreator() {
    return userRefFactory.create(((BasicDBObject)this.get("creator")).getString("_id"));
  }
  
  public SpaceReference getSpace() {
    if (this.get("space") == null) return null;
    BasicDBObject obj = (BasicDBObject) this.get("space");
    return spaceRefFactory.create(obj.getString("_id"));
  }
  
  public TenantReference getTenant() {
    return tenantRefFactory.create(((BasicDBObject)this.get("tenant")).getString("_id"));
  }
  
  public void addSuite(SuiteReference... suites) {
    Object obj = this.get("suites");
    BasicDBList list = obj == null ? new BasicDBList() : (BasicDBList) obj;
    for (SuiteReference ref: suites) {
      if (!verifySuite(ref)) throw new IllegalArgumentException("The suite has already added into another project or not existed");
      list.add(ref.toJSon());
    }
    this.put("suites", list);
  }
  
  private boolean verifySuite(SuiteReference ref) {
    if (ref.get() == null) return false;
    PageList<KeywordProject> list = projectService.findIn("suites", ref);
    return list.count() == 0;
  }
  
  public void removeSuite(SuiteReference ref) {
    Object obj = this.get("suites");
    if (obj == null) return;
    
    BasicDBList list = (BasicDBList) obj;
    list.remove(ref.toJSon());
    this.put("suites", list);
  }
  
  public List<SuiteReference> getSuites() {
    Object obj = this.get("suites");
    if (obj ==  null) return Collections.emptyList();
    
    BasicDBList list = (BasicDBList) obj;
    List<SuiteReference> suites = new ArrayList<SuiteReference>();
    for (int i = 0; i < list.size(); i++) {
      suites.add(suiteRefFactory.create(((BasicDBObject) list.get(i)).getString("_id")));
    }
    
    return Collections.unmodifiableList(suites);
  }
}
