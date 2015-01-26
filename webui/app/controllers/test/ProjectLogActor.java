/**
 * 
 */
package controllers.test;

import helpertest.JenkinsJobExecutor;
import helpertest.JenkinsJobHelper;
import helpertest.TestProjectHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import models.test.JenkinsJobModel;
import models.test.TestProjectModel;

import org.ats.component.usersmgt.group.Group;

import play.Logger;
import play.libs.Json;
import scala.concurrent.duration.Duration;
import utils.LogBuilder;
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
public class ProjectLogActor extends UntypedActor {
  
  private static ActorSystem system = null;

  static ActorRef actor = null;
  
  static Map<String, ProjectChannel> channels = new HashMap<String, ProjectChannel>();
  
  public static void start() {
    if (system == null) {
      system = ActorSystem.create("project-logs");
      actor = system.actorOf(Props.create(ProjectLogActor.class));
      system.scheduler().schedule(Duration.create(1000, TimeUnit.MILLISECONDS), Duration.create(1, TimeUnit.SECONDS),
        actor, "Dequeue", system.dispatcher(), null);
    }
    Logger.info("Started Akka system has named project-logs");
  }
  
  public static void stop() {
    if (system != null) {
      system.shutdown();
    }
    Logger.info("Shutdown Akka system has named project-logs");
  }
  
  static void addChannel(ProjectChannel channel) {
    channels.put(channel.sessionId, channel);
  }
  
  static void removeChannel(String sessionId) {
    channels.remove(sessionId);
  }
  
  @Override
  public void onReceive(Object msg) throws Exception {
    if (msg instanceof ProjectChannel) {
      ProjectChannel channel = (ProjectChannel) msg;
      channels.put(channel.sessionId, channel);
      getSender().tell("OK", getSelf());
    } else if (msg.equals("Dequeue")) {
      for (ProjectChannel channel : channels.values()) {
        ObjectNode jsonObj = Json.newObject();
        ArrayNode arrayLogs = jsonObj.arrayNode();
        
        //list of projects available
        Set<TestProjectModel> projects = new HashSet<TestProjectModel>();
        for (Group group : TestController.getAvailableGroups(channel.type, channel.userId)) {
          projects.addAll(TestProjectHelper.getProject(new BasicDBObject("group_id", group.getId())));
        }
        
        //stream log
        List<JenkinsJobModel> jobs = new ArrayList<JenkinsJobModel>(JenkinsJobHelper.getJobs(new BasicDBObject()));
        
        for (JenkinsJobModel job : jobs) {
          
          TestProjectModel project = TestProjectHelper.getProjectById(job.getString("project_id"));

          if (!projects.contains(project)) continue;
          
          ConcurrentLinkedQueue<String> queue = JenkinsJobExecutor.QueueHolder.get(job.getId());
          
          if (queue == null) continue;

          String s = queue.poll();

          StringBuilder sb = job.getString("log") == null ? new StringBuilder() : new StringBuilder(job.getString("log"));
          
          if (s != null) {
            //push to log
            if (!s.startsWith("vm-log-")) {
              LogBuilder.log(sb, s);
              job.put("log", sb.toString());
              JenkinsJobHelper.updateJenkinsJob(job);
            }
            
            arrayLogs.add(Json.newObject().put("id", job.getId()).put("msg", s));
          }
          if ("project.log.exit".equals(s)) {
            LogBuilder.log(sb, s);
            job.put("log", sb.toString());
            JenkinsJobExecutor.QueueHolder.remove(job.getId());
          }
        }
        
        jsonObj.put("logs", arrayLogs);
        channel.out.write(jsonObj);
      }
    } else {
      unhandled(msg);
    }
  }

}
