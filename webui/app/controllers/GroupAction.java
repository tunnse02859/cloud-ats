/**
 * 
 */
package controllers;

import interceptor.AuthenticationInterceptor;
import interceptor.Authorization;
import interceptor.WithoutSystem;
import interceptor.WizardInterceptor;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.regex.Pattern;

import org.ats.component.usersmgt.EventExecutor;
import org.ats.component.usersmgt.UserManagementException;
import org.ats.component.usersmgt.feature.Feature;
import org.ats.component.usersmgt.feature.FeatureDAO;
import org.ats.component.usersmgt.feature.Operation;
import org.ats.component.usersmgt.group.Group;
import org.ats.component.usersmgt.group.GroupDAO;
import org.ats.component.usersmgt.role.Permission;
import org.ats.component.usersmgt.role.Role;
import org.ats.component.usersmgt.role.RoleDAO;
import org.ats.component.usersmgt.user.User;
import org.ats.component.usersmgt.user.UserDAO;

import com.mongodb.BasicDBObject;

import play.api.templates.Html;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.With;
import views.html.organization.*;
import views.html.organization.group.*;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Aug 12, 2014
 */
@With({WizardInterceptor.class, AuthenticationInterceptor.class})
@Authorization(feature = "Organization", operation = "Administration")

public class GroupAction extends Controller {

  @WithoutSystem
  public static Result newGroup() throws UserManagementException {
    Group current = Organization.setCurrentGroup(null);
    session().put("group_id", current.getId());
    
    Html body = newgroup.render(current.getFeatures());
    return ok(index.render("group" , body, current.getId()));
  }
  
  @WithoutSystem
  public static Result newGroupBody() throws UserManagementException {
    Group current = GroupDAO.INSTANCE.findOne(session("group_id"));
    return ok(newgroup.render(current.getFeatures()));
  }
  
  @WithoutSystem
  public static Result doCreate() throws UserManagementException {
    String name = request().getQueryString("name");
    Group group = new Group(name);
    String[] features = request().queryString().get("feature");
    for (String f : features) {
      group.addFeature(FeatureDAO.INSTANCE.findOne(f));
    }
    
    
    Feature organization = FeatureDAO.INSTANCE.find(new BasicDBObject("name", "Organization")).iterator().next();
    
    Role administration = new Role("Administration", group.getId());
    administration.put("system", true);
    group.addRole(administration);
    
    for (Operation operation : organization.getOperations()) {
      administration.addPermission(new Permission(organization.getId(), operation.getId()));
    }
    
    Group current = GroupDAO.INSTANCE.findOne(session("group_id"));
    int level = current.getInt("level");
    group.put("level", level + 1);
    current.addGroupChild(group);
    
    GroupDAO.INSTANCE.create(group);
    GroupDAO.INSTANCE.update(current);
    RoleDAO.INSTANCE.create(administration);
    
    session().put("group_id", group.getId());
    
    return redirect(controllers.routes.Organization.body());
  }
  
  @WithoutSystem
  public static Result invite() throws UserManagementException {
    Group currentGroup = GroupDAO.INSTANCE.findOne(session("group_id"));
    User currentUser = UserDAO.INSTANCE.findOne(session("user_id"));
    
    if (currentGroup.getInt("level") == 1) {
      BasicDBObject query = new BasicDBObject("group_id", currentGroup.getId());
      query.put("name", "Administration");
      query.put("user_ids", Pattern.compile(currentUser.getId()));
      
      Role adRole = RoleDAO.INSTANCE.find(query).iterator().next();
      
      StringBuilder sb = new StringBuilder();
      for (Group g : currentGroup.buildParentTree()) {
        sb.append("/").append(g.get("name"));
      }
      sb.append("/").append(currentGroup.getString("name"));
      String groupPath = sb.toString();
      
      sb.setLength(0);
      sb.append("http://").append(request().host());
      sb.append(controllers.routes.Invitation.index(currentUser.getId(), adRole.getId(), currentGroup.getId()));
      
      Html body = invite.render(groupPath, sb.toString());
      return ok(index.render("group" , body, currentGroup.getId()));
    } else if (currentGroup.getInt("level") > 1) {
      
      Html body = adduser.render(getAvailableUser(currentGroup));
      return ok(index.render("group" , body, currentGroup.getId()));
    } else {
      return forbidden(views.html.forbidden.render());
    }
  }
  
  public static Result addUser() throws UserManagementException {
    if (request().getQueryString("user") != null) {
      Group currentGroup = GroupDAO.INSTANCE.findOne(session("group_id"));
      String[] users = request().queryString().get("user");
      for (String u : users) {
        User user = UserDAO.INSTANCE.findOne(u);
        user.joinGroup(currentGroup);
        currentGroup.addUser(user);

        UserDAO.INSTANCE.update(user);
        GroupDAO.INSTANCE.update(currentGroup);
      }
    }
    return redirect(controllers.routes.Organization.index() + "?nav=user");
  }
  
  public static Result deleteGroup(String g) throws UserManagementException {
    GroupDAO.INSTANCE.delete(g);
    while(EventExecutor.INSTANCE.isInProgress()) {
    }
    return redirect(controllers.routes.Organization.index() + "?nav=group");
  }
  
  private static Set<User> getAvailableUser(Group groupInvitation) throws UserManagementException {
    User currentUser = UserDAO.INSTANCE.findOne(session("user_id"));
    LinkedList<Group> groups = groupInvitation.buildParentTree();
    for (int i = 0; i < groups.size(); i++) {
      Group parent = groups.get(i);
      BasicDBObject query = new BasicDBObject("name", "Administration");
      query.put("group_id", parent.getId());
      query.put("user_ids", Pattern.compile(currentUser.getId()));
      if (!RoleDAO.INSTANCE.find(query).isEmpty()) {
        Set<User> holder = new HashSet<User>();
        for (User u : parent.getUsers()) {
          if (!groupInvitation.getUsers().contains(u)) holder.add(u);
        }
        return holder;
      }
    }
    return Collections.emptySet();
  }
}
