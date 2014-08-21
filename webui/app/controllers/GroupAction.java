/**
 * 
 */
package controllers;

import interceptor.AuthenticationInterceptor;
import interceptor.Authorization;
import interceptor.WithoutSystem;
import interceptor.WizardInterceptor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
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
    group.addFeature(organization);
    
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
  
  public static Result editGroup(String g) throws UserManagementException {
    Group group_ = GroupDAO.INSTANCE.findOne(g);
    User currentUser = UserDAO.INSTANCE.findOne(session("user_id"));
    
    //Prevent edit system group or group level 1
    if (group_.getBoolean("system")) return forbidden(views.html.forbidden.render());

    //Prevent edit group level 1 if current user is not system
    if (group_.getInt("level") == 1 && ! currentUser.getBoolean("system")) return forbidden(views.html.forbidden.render());
    
    //Prevent edit group which has no right permission
    if (!currentUser.getBoolean("system")) {
      Collection<Group> adGroup = Organization.getAdministrationGroup();
      Set<Group> childrenGroup = new HashSet<Group>();
      for (Group ag : adGroup) {
        childrenGroup.addAll(ag.getAllChildren());
      }
      
      if (! (adGroup.contains(group_) || childrenGroup.contains(group_))) return forbidden(views.html.forbidden.render());
    }
    
    LinkedList<Group> parents = group_.buildParentTree();
    
    Html body = editgroup.render(group_, parents.isEmpty() ? new HashSet<Feature>(FeatureDAO.INSTANCE.find(new BasicDBObject())) : parents.getLast().getFeatures());
    return ok(index.render("group", body, group_.getId()));
  }
  
  public static Result doEditGroup(String g) throws UserManagementException {
    Group group_ = GroupDAO.INSTANCE.findOne(g);
    User currentUser = UserDAO.INSTANCE.findOne(session("user_id"));
    
    //Prevent edit system group
    if (group_.getBoolean("system")) return forbidden(views.html.forbidden.render());

    //Prevent edit group level 1 if current user is not system
    if (group_.getInt("level") == 1 && ! currentUser.getBoolean("system")) return forbidden(views.html.forbidden.render());
    
    //Prevent edit group which has no right permission
    if (!currentUser.getBoolean("system")) {
      Collection<Group> adGroup = Organization.getAdministrationGroup();
      Set<Group> childrenGroup = new HashSet<Group>();
      for (Group ag : adGroup) {
        childrenGroup.addAll(ag.getAllChildren());
      }
      
      if (! (adGroup.contains(group_) || childrenGroup.contains(group_))) return forbidden(views.html.forbidden.render());
    }
    
    //
    Set<String> currentFeature = group_.getString("feature_ids") == null ? new HashSet<String>() : group_.stringIDtoSet(group_.getString("feature_ids"));
    Set<String> actualFeature = new HashSet<String>();
    
    if (request().getQueryString("feature") != null) Collections.addAll(actualFeature, request().queryString().get("feature"));
    
    //Add new feature
    for (String f : actualFeature) {
      if (!currentFeature.contains(f)) {
        Feature feature = FeatureDAO.INSTANCE.findOne(f);
        group_.addFeature(feature);
      }
    }
    
    //Remove no longer feature
    for (String f : currentFeature) {
      if (!actualFeature.contains(f)) {
        Feature feature = FeatureDAO.INSTANCE.findOne(f);
        if (feature.getBoolean("system")) continue;
        group_.removeFeature(feature);
        
        //remove in children
        for (Group child : group_.getAllChildren()) {
          child.removeFeature(feature);
          GroupDAO.INSTANCE.update(child);
        }
      }
    }
    
    if (request().getQueryString("name") != null) group_.put("name", request().getQueryString("name"));
   
    GroupDAO.INSTANCE.update(group_);
    
    return redirect(controllers.routes.Organization.index() + "?nav=group");
  }
  
  /**
   * TODO: Prevent delete group which no has right permission by pass query
   * @param g
   * @return
   * @throws UserManagementException
   */
  public static Result deleteGroup(String g) throws UserManagementException {
    Group group_   = GroupDAO.INSTANCE.findOne(g);
    User currentUser = UserDAO.INSTANCE.findOne(session("user_id"));
    
    //Prevent delete system group or group level 1
    if (group_.getBoolean("system")) return forbidden(views.html.forbidden.render());
    
    //Prevent delete group which has no right permission
    if (!currentUser.getBoolean("system")) {
      Collection<Group> adGroup = Organization.getAdministrationGroup();
      Set<Group> childrenGroup = new HashSet<Group>();
      for (Group ag : adGroup) {
        childrenGroup.addAll(ag.getAllChildren());
      }
      
      if (! (adGroup.contains(group_) || childrenGroup.contains(group_))) return forbidden(views.html.forbidden.render());
    }
    
    BasicDBObject query = new BasicDBObject("joined", true);
    query.put("group_ids", Pattern.compile(group_.getId()));
    Collection<User> users_ = UserDAO.INSTANCE.find(query);

    GroupDAO.INSTANCE.delete(group_);
    
    while(EventExecutor.INSTANCE.isInProgress()) {
    }
    
    if (group_.getInt("level") == 1) {
      for (User u : users_) {
        u = UserDAO.INSTANCE.findOne(u.getId());
        u.put("joined", false);
        UserDAO.INSTANCE.update(u);
      }
    }
    
    return redirect(controllers.routes.Organization.index() + "?nav=group");
  }
  
  private static List<User> getAvailableUser(Group groupInvitation) throws UserManagementException {
    User currentUser = UserDAO.INSTANCE.findOne(session("user_id"));
    Collection<Group> adGroups = Organization.getAministrationGroup(currentUser);
    LinkedList<Group> parents = groupInvitation.buildParentTree();
    Group parent = parents.getLast();
    
    for (Group g : parents) {
      BasicDBObject query = new BasicDBObject("name", "Administration");
      query.put("system", true);
      query.put("group_id", g.getId());
      query.put("user_ids", Pattern.compile(currentUser.getId()));
      if (!RoleDAO.INSTANCE.find(query).isEmpty()) {
        return parent.getUsers();
      }
    }
    
    return Collections.emptyList();
  }
}
