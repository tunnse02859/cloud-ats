/**
 * 
 */
package org.ats.component.usersmgt.feature;

import java.util.Collection;
import java.util.regex.Pattern;

import org.ats.component.usersmgt.Event;
import org.ats.component.usersmgt.EventExecutedException;
import org.ats.component.usersmgt.EventListener;
import org.ats.component.usersmgt.UserManagementException;
import org.ats.component.usersmgt.group.Group;
import org.ats.component.usersmgt.group.GroupDAO;
import org.ats.component.usersmgt.role.Permission;
import org.ats.component.usersmgt.role.PermissionDAO;

import com.mongodb.BasicDBObject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Jul 15, 2014
 */
public class FeatureEventListener implements EventListener{

  public void execute(Event event) throws EventExecutedException {
    if ("delete-feature".equals(event.getType())) {
      try {
        this.processDeleteFeatureInGroup(event);
        this.processDeleteFeatureInPermission(event);
      } catch (UserManagementException e) {
        e.printStackTrace();
      }
    }
  }
  
  private void processDeleteFeatureInGroup(Event event) throws UserManagementException {
    Feature feature = new Feature(event.getSource());
    Pattern p = Pattern.compile(feature.getId());
    Collection<Group> groups = GroupDAO.INSTANCE.find(new BasicDBObject("feature_ids", p));
    for (Group group : groups) {
      group.removeFeature(feature);
      GroupDAO.INSTANCE.update(group);
    }
  }
  
  private void processDeleteFeatureInPermission(Event event) throws UserManagementException {
    Feature feature = new Feature(event.getSource());
    Collection<Permission> perms = PermissionDAO.INSTANCE.find(new BasicDBObject("feature_id", feature.getId()));
    for (Permission perm : perms) {
      PermissionDAO.INSTANCE.delete(perm);
    }
  }
}
