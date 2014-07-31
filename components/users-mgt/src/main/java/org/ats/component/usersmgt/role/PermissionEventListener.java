/**
 * 
 */
package org.ats.component.usersmgt.role;

import java.util.Collection;
import java.util.regex.Pattern;

import org.ats.component.usersmgt.Event;
import org.ats.component.usersmgt.EventExecutedException;
import org.ats.component.usersmgt.EventListener;
import org.ats.component.usersmgt.UserManagementException;

import com.mongodb.BasicDBObject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Jul 30, 2014
 */
public class PermissionEventListener implements EventListener {

  public void execute(Event event) throws EventExecutedException {
    if ("delete-permission".equals(event.getType())) {
      try {
        this.processDeletePermissionInRole(event);
      } catch (UserManagementException e) {
        e.printStackTrace();
      }
    }
  }

  private void processDeletePermissionInRole(Event event) throws UserManagementException {
    Permission permission = new Permission(event.getSource());
    Pattern p = Pattern.compile(permission.getId());
    Collection<Role> roles = RoleDAO.INSTANCE.find(new BasicDBObject("permission_ids", p));
    for (Role role : roles) {
      role.removePermission(permission);
      RoleDAO.INSTANCE.update(role);
    }
  }
}
