/**
 * 
 */
package org.ats.services.schedule;

import java.util.Date;
import java.util.UUID;

import org.ats.services.OrganizationContext;
import org.ats.services.organization.entity.AbstractEntity;
import org.ats.services.organization.entity.User;
import org.ats.services.organization.entity.fatory.ReferenceFactory;
import org.ats.services.organization.entity.reference.UserReference;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.mongodb.BasicDBObject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Apr 22, 2015
 */
@SuppressWarnings("serial")
public class Schedule extends AbstractEntity<Schedule> {
  
  private ReferenceFactory<UserReference> userRefFactory;
  
  
  @Inject
  Schedule(ReferenceFactory<UserReference> userRefFactory,
      OrganizationContext context,
      @Assisted("name") String name, @Assisted("project_id") String project_id) {
    
    this.userRefFactory = userRefFactory;
    
    User user = context.getUser();
    this.put("name", name);
    this.put("project_id", project_id);
    this.put("created_date", new Date());
    this.put("_id", UUID.randomUUID().toString());
    this.put("creator", context.getUser());
  }
  
  public String getId() {
    return this.getString("_id");
  }
  
  
  public void setKeyword_id(String id) {
    this.put("project_id", id);
  }
  
  public String getKeyword_id() {
    return this.getString("project_id");
  }
  
  public UserReference getCreator() {
    return userRefFactory.create(((BasicDBObject)this.get("creator")).getString("_id"));
  }
  
}
