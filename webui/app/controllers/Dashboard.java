/**
 * 
 */
package controllers;

import helpertest.JenkinsJobHelper;
import helpertest.TestProjectHelper;
import interceptor.AuthenticationInterceptor;
import interceptor.WizardInterceptor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import models.test.JenkinsJobModel;
import models.test.JenkinsJobStatus;

import org.ats.component.usersmgt.UserManagementException;
import org.ats.component.usersmgt.group.Group;
import org.ats.component.usersmgt.group.GroupDAO;
import org.ats.component.usersmgt.user.User;
import org.ats.component.usersmgt.user.UserDAO;

import play.api.templates.Html;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.With;
import scala.collection.mutable.StringBuilder;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.BasicDBObject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Aug 5, 2014
 */

@With({WizardInterceptor.class, AuthenticationInterceptor.class})
public class Dashboard extends Controller {
  
  public static Html chart(String jobType) throws UserManagementException {
      ObjectNode json = buildJobData(jobType);
      String data1 = json.get("bar").toString();
      String data2 = json.get("pie").toString();
      String labels = json.get("labels").toString();

      return views.html.dashboard.chart.render(jobType + "-chart", jobType, new Html(new StringBuilder(data1)), new Html(new StringBuilder(data2)), new Html(new StringBuilder(labels)));
  }
  
  private static ObjectNode buildJobData(String jobType) {
    SimpleDateFormat identityFormat = new SimpleDateFormat("yyyyMMdd");
    SimpleDateFormat normalFormat = new SimpleDateFormat("MMM dd");
    
    Map<Integer, Integer> completed = new HashMap<Integer, Integer>();
    Map<Integer, Integer> running = new HashMap<Integer, Integer>();
    Map<Integer, Integer> error = new HashMap<Integer, Integer>();
    
    Map<Integer, String> labels = new HashMap<Integer, String>();
    
    ObjectNode json = Json.newObject();
    ArrayNode array = json.arrayNode();
    
    List<JenkinsJobModel> jobs = JenkinsJobHelper.getJobs(new BasicDBObject("job_type", jobType));
    for (JenkinsJobModel job : jobs) {
      for (JenkinsJobModel.JenkinsBuildResult result : job.getResults()) {
        
        Date date = new Date(result.getLong("build_time"));
        
        int identityString = Integer.parseInt(identityFormat.format(date));
        String normalString = normalFormat.format(date);
        labels.put(identityString, normalString);
        
        JenkinsJobStatus status = JenkinsJobStatus.valueOf(result.getString("status"));
        switch (status) {
        case Completed:
          completed.put(identityString, completed.get(identityString) == null ? 1 : (completed.get(identityString) + 1));
          break;
        case Aborted:
        case Errors:
        case Failure:
          error.put(identityString, error.get(identityString) == null ? 1 : (error.get(identityString) + 1));
          break;
        case Running:
        case Initializing:
          running.put(identityString, running.get(identityString) == null ? 1 : (running.get(identityString) + 1));
          break;
        default:
          break;
        }
      }
    }
    
    //Completed Node;
    foo(array, completed, "Completed");
    
    //Running Node;
    foo(array, running, "Running");
    
    //Error Node
    foo(array, error, "Error");
    
    json.put("bar", array);
    
    ArrayNode pieArray = json.arrayNode();
    
    int completedCount = 0;
    for (Integer i : completed.values()) {
      completedCount += i;
    }
    pieArray.add(Json.newObject().put("label", "Completed").put("data", completedCount));
    
    int runningCount = 0;
    for (Integer i : running.values()) {
      runningCount += i;
    }
    pieArray.add(Json.newObject().put("label", "Running").put("data", runningCount));
    
    int errorCount = 0;
    for (Integer i : error.values()) {
      errorCount += i;
    }
    pieArray.add(Json.newObject().put("label", "Error").put("data", errorCount));
    
    json.put("pie", pieArray);
    
    if (labels.isEmpty()) return json;

    ArrayNode lableArray = json.arrayNode();
    Iterator<Map.Entry<Integer, String>> iterator = labels.entrySet().iterator();
    while (iterator.hasNext()) {
      Map.Entry<Integer, String> entry = iterator.next();
      lableArray.add(Json.newObject().arrayNode().add(entry.getKey()).add(entry.getValue()));
    }
    json.put("labels", lableArray);    
    return json;
  }
  
  private static void foo(ArrayNode array, Map<Integer, Integer> source, String label) {
    ObjectNode node = Json.newObject();
    node.put("label", label);
    ArrayNode nodeArray = node.arrayNode();
    
    List<Integer> keys = new ArrayList<Integer>(source.keySet());
    Collections.sort(keys, new Comparator<Integer>() {
      @Override
      public int compare(Integer o1, Integer o2) {
        return o1 - o2;
      }
    });
    for (Integer key : keys) {
      nodeArray.add(Json.newObject().arrayNode().add(key).add(source.get(key)));
    }
    node.put("data", nodeArray);
    array.add(node);
  }
  
  public static Html body() throws UserManagementException {
    
    if (TestProjectHelper.getCollection().count() == 0) {
      User currentUser = UserDAO.getInstance(Application.dbName).findOne(session("user_id"));
      Group currentGroup = GroupDAO.getInstance(Application.dbName).findOne(session("group_id"));
      return views.html.dashboard.body.render(currentUser, currentGroup);
    } else {
      return views.html.dashboard.chartbody.render();
    }
  }
  
  public static Html groupMenuList() throws UserManagementException {
    StringBuilder sb = new StringBuilder();
    User currentUser = UserDAO.getInstance(Application.dbName).findOne(session("user_id"));
    
    List<Group> groups = currentUser.getGroups();
    Collections.sort(groups, new Comparator<Group>() {
      @Override
      public int compare(Group o1, Group o2) {
        return o1.getInt("level") - o2.getInt("level");
      }
    });
    for (Group group : groups) {
      sb.append("<li><a href='");
      sb.append(controllers.routes.Dashboard.changeGroup(group.getId())).append("'>");
      LinkedList<Group> parents = GroupDAO.getInstance(Application.dbName).buildParentTree(group);
      for (Group p : parents) {
        sb.append(" / ").append(p.getString("name"));
      }
      sb.append(" / ").append(group.getString("name"));
      sb.append("</a></li>");
    }
    return new Html(sb);
  }
  
  public static Result changeGroup(String g) throws UserManagementException {
    User currentUser = UserDAO.getInstance(Application.dbName).findOne(session("user_id"));
    Group group = GroupDAO.getInstance(Application.dbName).findOne(g);
    if (group.getUsers().contains(currentUser)) {
      session("group_id", group.getId());
    }
    return redirect(controllers.routes.Application.dashboard());
  }
  
  public static Result updateProfile() throws UserManagementException {
    User currentUser = UserDAO.getInstance(Application.dbName).findOne(session("user_id"));
    currentUser.put("firstname", request().getQueryString("firstname"));
    currentUser.put("lastname", request().getQueryString("lastname"));
    currentUser.put("im", request().getQueryString("im"));
    currentUser.put("tel", request().getQueryString("tel"));
    UserDAO.getInstance(Application.dbName).update(currentUser);
    
    ObjectNode json = Json.newObject();
    json.put("firstname", currentUser.getString("firstname"));
    json.put("lastname", currentUser.getString("lastname"));
    json.put("im", currentUser.getString("im"));
    json.put("tel", currentUser.getString("tel"));
    
    return ok(json);
  }
}
