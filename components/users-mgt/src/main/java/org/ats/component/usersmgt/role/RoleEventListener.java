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
import org.ats.component.usersmgt.group.Group;
import org.ats.component.usersmgt.group.GroupDAO;
import org.ats.component.usersmgt.user.User;
import org.ats.component.usersmgt.user.UserDAO;

import com.mongodb.BasicDBObject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Jul 30, 2014
 */
public class RoleEventListener implements EventListener {

  public void execute(Event event) throws EventExecutedException {
    if ("delete-role".equals(event.getType())) {
      try {
        this.processDeletePermisisonInRole(event);
        this.processDeleteRoleInGroup(event);
        this.processDeleteRoleInUser(event);
      } catch (UserManagementException e) {
        e.printStackTrace();
      }
    }
  }
  
  private void processDeletePermisisonInRole(Event event) throws UserManagementException {
    Role role = new Role().from(event.getSource());
    Collection<Permission> perms = role.getPermissions();
    for (Permission perm : perms) {
      PermissionDAO.getInstance(event.getDbName()).delete(perm);
    }
  }

  private void processDeleteRoleInGroup(Event event) throws UserManagementException {
    Role role = new Role().from(event.getSource());
    Pattern p = Pattern.compile(role.getId());
    Collection<Group> groups = GroupDAO.getInstance(event.getDbName()).find(new BasicDBObject("role_ids", p));
    for (Group group : groups) {
      group.removeRole(role);
      GroupDAO.getInstance(event.getDbName()).update(group);
    }
  }
  
  private void processDeleteRoleInUser(Event event) throws UserManagementException {
    Role role = new Role().from(event.getSource());
    Pattern p = Pattern.compile(role.getId());
    Collection<User> users = UserDAO.getInstance(event.getDbName()).find(new BasicDBObject("role_ids", p));
    for (User user : users) {
      user.removeRole(role);
      UserDAO.getInstance(event.getDbName()).update(user);
    }
  }
}
