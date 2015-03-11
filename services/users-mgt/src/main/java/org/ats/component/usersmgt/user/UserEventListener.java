/**
 * 
 */
package org.ats.component.usersmgt.user;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.ats.component.usersmgt.Event;
import org.ats.component.usersmgt.EventExecutedException;
import org.ats.component.usersmgt.EventListener;
import org.ats.component.usersmgt.UserManagementException;
import org.ats.component.usersmgt.group.Group;
import org.ats.component.usersmgt.group.GroupDAO;
import org.ats.component.usersmgt.role.Role;
import org.ats.component.usersmgt.role.RoleDAO;

import com.mongodb.BasicDBObject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Jul 30, 2014
 */
public class UserEventListener implements EventListener {

  public void execute(Event event) throws EventExecutedException {
    try {
      if ("delete-user".equals(event.getType())) {
        this.processDeleteUserInGroup(event);
      } else if ("leave-group".equals(event.getType())) {
        this.processLeaveUserInGroup(event);
      }
    } catch (UserManagementException e) {
      e.printStackTrace();
    }
  }
  
  private void processLeaveUserInGroup(Event event) throws UserManagementException {
    User user = new User().from(event.getSource());
    List<Group> currentGroup = user.getGroups();
    
    User actual = UserDAO.getInstance(event.getDbName()).findOne(user.getId());
    List<Group> actualGroup = actual.getGroups();
    
    Group leaveGroup = null;
    for (Group g : actualGroup) {
      if (!currentGroup.contains(g)) {
        leaveGroup = g;
        break;
      }
    }
    leaveGroup.removeUser(user.getId());
    GroupDAO.getInstance(event.getDbName()).update(leaveGroup);
    
    //remove role of current group
    List<Role> roles = user.getRoles();
    for (Role r : leaveGroup.getRoles()) {
      if (roles.contains(r)) {
        user.removeRole(r);
        r.removeUser(user);
        RoleDAO.getInstance(event.getDbName()).update(r);
      }
    }
    
    //leave group child if exist
    Set<Group> children = leaveGroup.getAllChildren();
    for (Group child : children) {
      if (child.getUsers().contains(actual)) {
        child.removeUser(user.getId());
        user.leaveGroup(child);
        
        //remove role of child group
        for (Role r : child.getRoles()) {
          if (roles.contains(r)) {
            user.removeRole(r);
            r.removeUser(user);
            RoleDAO.getInstance(event.getDbName()).update(r);
          }
        }
        
        GroupDAO.getInstance(event.getDbName()).update(child);
      }
    }
    
    UserDAO.getInstance(event.getDbName()).update(user);
  }
  
  private void processDeleteUserInGroup(Event event) throws UserManagementException {
    User user = new User().from(event.getSource());
    Pattern p = Pattern.compile(user.getId());
    Collection<Group> groups = GroupDAO.getInstance(event.getDbName()).find(new BasicDBObject("user_ids", p));
    for (Group group : groups) {
      group.removeUser(user);
      GroupDAO.getInstance(event.getDbName()).update(group);
    }
    for (Role role : user.getRoles()) {
      role.removeUser(user);
      RoleDAO.getInstance(event.getDbName()).update(role);
    }
  }
}
