/**
 * 
 */
package controllers.test;

import helpertest.JMeterScriptHelper;
import helpertest.TestHelper;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.ats.component.usersmgt.group.Group;
import org.ats.jmeter.models.JMeterScript;

import models.test.TestProjectModel;
import models.test.TestProjectModel.TestProjectType;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.BasicDBObject;

import play.libs.Akka;
import play.libs.Json;
import scala.concurrent.duration.Duration;
import akka.actor.ActorRef;
import akka.actor.Cancellable;
import akka.actor.Props;
import akka.actor.UntypedActor;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Oct 27, 2014
 */
public class ProjectStatusActor extends UntypedActor {

  static ActorRef actor = Akka.system().actorOf(Props.create(ProjectStatusActor.class));
  
  final static Cancellable canceller = Akka.system().scheduler().schedule(Duration.create(100, TimeUnit.MILLISECONDS), Duration.create(1, TimeUnit.SECONDS),
      actor, "Check", Akka.system().dispatcher(), null);
  
  Map<String, ProjectChannel> channels = new HashMap<String, ProjectChannel>();
  
  @Override
  public void onReceive(Object msg) throws Exception {
    if (msg instanceof ProjectChannel) {
      ProjectChannel channel = (ProjectChannel) msg;
      channels.put(channel.sessionId, channel);
      getSender().tell("OK", getSelf());
    } else if (msg.equals("Check")) {
      for (ProjectChannel channel : channels.values()) {
        ObjectNode jsonObj = Json.newObject();
        ArrayNode array = jsonObj.arrayNode();
        Set<TestProjectModel> projects = new HashSet<TestProjectModel>();
        for (Group group : TestController.getAvailableGroups(channel.userId)) {
          projects.addAll(TestHelper.getProject(TestProjectType.valueOf(channel.type), new BasicDBObject("group_id", group.getId())));
        }
        for (TestProjectModel project : projects) {

          ObjectNode projectNode = Json.newObject().put("id", project.getId()).put("status", project.getStatus()).put("last_build", project.getLastBuildDate());
              
          switch (project.getType()) {
          case performance:
            ArrayNode snapshotNode = projectNode.arrayNode();
            for (JMeterScript snapshot : JMeterScriptHelper.getJMeterScript(project.getId())) {
              snapshotNode.add(Json.newObject().put("id", snapshot.getString("_id")).put("status", snapshot.getString("status")).put("last_build", snapshot.getLastBuildDate()));
            }
            projectNode.put("snapshots", snapshotNode);
          case functional:
            array.add(projectNode);
            break;
          default:
            break;
          }
        }
        
        jsonObj.put("projects", array);
        channel.out.write(jsonObj);
      }
    } else {
      unhandled(msg);
    }
  }

}
