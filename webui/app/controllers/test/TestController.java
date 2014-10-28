/**
 * 
 */
package controllers.test;

import static akka.pattern.Patterns.ask;
import helpertest.TestHelper;
import interceptor.AuthenticationInterceptor;
import interceptor.WizardInterceptor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import models.test.JenkinsJobStatus;
import models.test.TestProjectModel;
import models.test.TestProjectModel.TestProjectType;

import org.ats.component.usersmgt.UserManagementException;
import org.ats.component.usersmgt.feature.Feature;
import org.ats.component.usersmgt.feature.FeatureDAO;
import org.ats.component.usersmgt.feature.Operation;
import org.ats.component.usersmgt.group.Group;
import org.ats.component.usersmgt.group.GroupDAO;
import org.ats.component.usersmgt.role.Permission;
import org.ats.component.usersmgt.role.Role;
import org.ats.component.usersmgt.user.User;
import org.ats.component.usersmgt.user.UserDAO;

import play.api.templates.Html;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.WebSocket;
import play.mvc.With;
import scala.concurrent.Await;
import scala.concurrent.duration.Duration;

import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.BasicDBObject;

import controllers.Application;
import views.html.test.*;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Oct 21, 2014
 */

@With({WizardInterceptor.class, AuthenticationInterceptor.class})
public class TestController extends Controller {
  
  public static Result createNewProject(String type) {
    return ok(index.render(type, newproject.render(type)));
  }
  
  public static WebSocket<JsonNode> projectStatus(final String type, final String sessionId, final String currentUserId) {
    return new WebSocket<JsonNode>() {
      @Override
      public void onReady(play.mvc.WebSocket.In<JsonNode> in, play.mvc.WebSocket.Out<JsonNode> out) {
        try {
          Await.result(ask(ProjectStatusActor.actor, new ProjectChannel(sessionId, currentUserId, type, out), 1000), Duration.create(1, TimeUnit.SECONDS));
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    };
  }
  
  public static String getColorByStatus(String status) {
    JenkinsJobStatus _status = JenkinsJobStatus.valueOf(status);
    switch (_status) {
    case Ready:
    case Completed:  
      return "green";
    case Running:
      return "cyan";
    case Aborted:
      return "orange";
    case Errors:
      return "red";
    case Initializing:
      return "blue";
    default:
      return "";
    }
  }
  
  public static Html groupMenuList() throws UserManagementException {
    scala.collection.mutable.StringBuilder sb = new scala.collection.mutable.StringBuilder();
    List<Group> groups = getAvailableGroups();
    
    for (Group group : groups) {
      buildGroupPath(sb, group);
    }
    
    return new Html(sb);
  }
  
  public static Result getProjectList() throws UserManagementException {
    TestProjectType type = TestProjectType.performance ;
    if (request().getQueryString("type") != null)
      type = TestProjectType.valueOf(request().getQueryString("type"));

    String group_id = null;
    if (request().getQueryString("group") != null) 
      group_id = request().getQueryString("group");
    
    String userText = null;
    if (request().getQueryString("user") != null) {
      userText = request().getQueryString("user");
    }
    return ok(getProjectListHtml(type.toString(), group_id, userText));
  }
  
  public static Html getProjectListHtml(String type, String group_id, String userText) throws UserManagementException {
    scala.collection.mutable.StringBuilder sb = new scala.collection.mutable.StringBuilder();
    
    Set<TestProjectModel> set = new HashSet<TestProjectModel>();
    if (group_id == null) {
      for (Group group : getAvailableGroups()) {
        set.addAll(TestHelper.getProject(TestProjectType.valueOf(type), new BasicDBObject("group_id", group.getId())));
      }
    }
    
    List<TestProjectModel> projects = new ArrayList<TestProjectModel>(set);
    Collections.sort(projects, new Comparator<TestProjectModel>() {
      @Override
      public int compare(TestProjectModel o1, TestProjectModel o2) {
        return o2.getIndex() - o1.getIndex();
      }
    });
    
    for (TestProjectModel p : projects) {
      sb.append(project.render(p));
    }
    return new Html(sb);
  }
  
  public static List<Group> getAvailableGroups() throws UserManagementException {
    return getAvailableGroups(session("user_id"));
  }
  
  public static List<Group> getAvailableGroups(String currentUserId) throws UserManagementException {
    
    User currentUser = UserDAO.getInstance(Application.dbName).findOne(currentUserId);
    
    Feature perfFeature = FeatureDAO.getInstance(Application.dbName).find(new BasicDBObject("name", "Performance")).iterator().next();
    Operation perfAdOperation = null;
    Operation perfTestOperation = null;
    for (Operation op : perfFeature.getOperations()) {
      if (op.getName().equals("Administration")) {
        perfAdOperation = op;
      } else if (op.getName().equals("Test")) {
        perfTestOperation = op;
      }
    }
    
    List<Role> perfAdRoles = new ArrayList<Role>();
    List<Role> perfTestRoles = new ArrayList<Role>();
    
    for (Role role : currentUser.getRoles()) {
      for (Permission per : role.getPermissions()) {
        Feature f = per.getFeature();
        Operation op = per.getOpertion();
        if (f.equals(perfFeature)) {
          if (op.equals(perfAdOperation))
            perfAdRoles.add(role);
          else if (op.equals(perfTestOperation)) {
            perfTestRoles.add(role);
          }
        }
      }
    }
    
    List<Group> adminGroups = getGroupsHasPermission(currentUser, perfAdRoles);
    List<Group> testGroups = getGroupsHasPermission(currentUser, perfTestRoles);
    Set<Group> set = new HashSet<Group>();
    
    for (Group group : adminGroups) {
      set.add(group);
      set.addAll(group.getAllChildren());
    }
    set.addAll(testGroups);
    List<Group> groups = new ArrayList<Group>(set);
    
    Collections.sort(groups, new Comparator<Group>() {
      @Override
      public int compare(Group o1, Group o2) {
        return o1.getInt("level") - o2.getInt("level");
      }
    });
    
    return groups;
  }
  
  private static void buildGroupPath(scala.collection.mutable.StringBuilder sb, Group group) throws UserManagementException {
    sb.append("<li><a href='javascript:void(0);'>");
    LinkedList<Group> parents = GroupDAO.getInstance(Application.dbName).buildParentTree(group);
    for (Group p : parents) {
      sb.append(" / ").append(p.getString("name"));
    }
    sb.append(" / ").append(group.getString("name"));
    sb.append("</a></li>");
  }
  
  private static List<Group> getGroupsHasPermission(User currentUser, List<Role> adRoles) {
    List<Group> groups = currentUser.getGroups();
    
    List<Group> holder = new ArrayList<Group>();
    for (Role role : adRoles) {
      Group group = role.getGroup();
      if (groups.contains(group)) holder.add(group);
    }
    
    return holder;
  }
  
  public static Group getCompany() throws UserManagementException {
    User currentUser = UserDAO.getInstance(Application.dbName).findOne(session("user_id"));
    List<Group> groups = currentUser.getGroups();
    for (Group group : groups) {
      if (group.getInt("level") == 1) return group;
    }
    
    return null;
  }
}
