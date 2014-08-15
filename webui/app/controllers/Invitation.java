/**
 * 
 */
package controllers;

import org.ats.component.usersmgt.UserManagementException;
import org.ats.component.usersmgt.group.Group;
import org.ats.component.usersmgt.group.GroupDAO;
import org.ats.component.usersmgt.role.Role;
import org.ats.component.usersmgt.role.RoleDAO;
import org.ats.component.usersmgt.user.User;
import org.ats.component.usersmgt.user.UserDAO;

import play.mvc.Controller;
import play.mvc.Result;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Aug 14, 2014
 */
public class Invitation extends Controller {

  public static Result index(String u, String r, String g) throws UserManagementException {
    User user = UserDAO.INSTANCE.findOne(u);
    Role role = RoleDAO.INSTANCE.findOne(r);
    Group group = GroupDAO.INSTANCE.findOne(g);
    
    if (verify(user, role, group)) {
      User currentUser = UserDAO.INSTANCE.findOne(session("user_id"));
      
      if (!currentUser.getBoolean("joined") && currentUser.getGroups().isEmpty()) {
        currentUser.joinGroup(group);
        group.addUser(currentUser);
        
        GroupDAO.INSTANCE.update(group);
        UserDAO.INSTANCE.update(currentUser);
      }
    }
    
    return redirect(controllers.routes.Application.index());
  }
  
  private static boolean verify(User user, Role role, Group group) {
    if (!"Administration".equals(role.getName())) return false;
    if (!role.getGroup().equals(group)) return false;
    if (!role.getUsers().contains(user)) return false;
    if (!group.getUsers().contains(user)) return false;
    if (!user.getGroups().contains(group)) return false;
    return true;
  }
}
