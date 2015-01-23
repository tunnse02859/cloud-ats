/**
 * 
 */
package controllers.test;

import helpertest.JMeterScriptHelper;
import helpertest.TestProjectHelper;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import models.test.TestProjectModel;

import org.ats.component.usersmgt.group.Group;
import org.ats.jmeter.models.JMeterScript;

import play.Logger;
import play.libs.Json;
import scala.concurrent.duration.Duration;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.BasicDBObject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Oct 27, 2014
 */
public class ProjectStatusActor extends UntypedActor {

  private static ActorSystem system = null;
  
  static ActorRef actor = null;
  
  static Map<String, ProjectChannel> channels = new HashMap<String, ProjectChannel>();
  
  public static void start() {
    if (system == null) {
      system = ActorSystem.create("project-logs");
      actor = system.actorOf(Props.create(ProjectStatusActor.class));
      system.scheduler().schedule(Duration.create(100, TimeUnit.MILLISECONDS), Duration.create(1, TimeUnit.SECONDS),
        actor, "Check", system.dispatcher(), null);
    }
    Logger.info("Started Akka system has named project-status");
  }
  
  public static void stop() {
    if (system != null) {
      system.shutdown();
    }
    Logger.info("Shutdown Akka system has named project-status");
  }
  
  static void addChannel(ProjectChannel channel) {
    channels.put(channel.sessionId, channel);
  }
  
  @Override
  public void onReceive(Object msg) throws Exception {
    if (msg instanceof ProjectChannel) {
      ProjectChannel channel = (ProjectChannel) msg;
      channels.put(channel.sessionId, channel);
      getSender().tell("OK", getSelf());
    } else if (msg.equals("Check")) {
      for (ProjectChannel channel : channels.values()) {
        
        ObjectNode jsonObj = Json.newObject();
        ArrayNode arrayStatus = jsonObj.arrayNode();
        
        Set<TestProjectModel> projects = new HashSet<TestProjectModel>();
        for (Group group : TestController.getAvailableGroups(channel.type, channel.userId)) {
          projects.addAll(TestProjectHelper.getProject(new BasicDBObject("group_id", group.getId())));
        }
        
        for (TestProjectModel project : projects) {

          ObjectNode projectNode = Json.newObject().put("id", project.getId()).put("status", project.getStatus()).put("last_build", project.getLastBuildDate());
              
          if (TestProjectModel.PERFORMANCE.equals(project.getType())) {
            ArrayNode snapshotNode = projectNode.arrayNode();
            for (JMeterScript snapshot : JMeterScriptHelper.getJMeterScript(project.getId())) {
              snapshotNode.add(Json.newObject().put("id", snapshot.getString("_id")).put("status", snapshot.getString("status")).put("last_build", snapshot.getLastBuildDate()));
            }
            projectNode.put("snapshots", snapshotNode);
          }
          
          arrayStatus.add(projectNode);
        }
        
        jsonObj.put("projects", arrayStatus);
        channel.out.write(jsonObj);
      }
    } else {
      unhandled(msg);
    }
  }

}
