/**
 * 
 */
package org.ats.component.usersmgt.user;

import java.util.Collection;
import java.util.regex.Pattern;

import org.ats.component.usersmgt.Event;
import org.ats.component.usersmgt.EventExecutedException;
import org.ats.component.usersmgt.EventListener;
import org.ats.component.usersmgt.UserManagementException;
import org.ats.component.usersmgt.group.Group;
import org.ats.component.usersmgt.group.GroupDAO;

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
      }
    } catch (UserManagementException e) {
      e.printStackTrace();
    }
  }
  
  private void processDeleteUserInGroup(Event event) throws UserManagementException {
    User user = new User(event.getSource());
    Pattern p = Pattern.compile(user.getId());
    Collection<Group> groups = GroupDAO.INSTANCE.find(new BasicDBObject("user_ids", p));
    for (Group group : groups) {
      group.removeUser(user);
      GroupDAO.INSTANCE.update(group);
    }
  }
}
