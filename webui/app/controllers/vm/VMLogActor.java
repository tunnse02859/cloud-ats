/**
 * 
 */
package controllers.vm;

import helpervm.VMHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import models.vm.VMModel;
import models.vm.VMModel.VMStatus;
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
 * Sep 22, 2014
 */
public class VMLogActor extends UntypedActor {
  
  private static ActorSystem system = null;
  
  static ActorRef actor =  null;

  static Map<String, VMChannel> channels = new HashMap<String, VMChannel>();
  
  public static void start() {
    if (system == null) {
      system = ActorSystem.create("vm-logs");
      actor = system.actorOf(Props.create(VMLogActor.class));
      system.scheduler().schedule(Duration.create(3000, TimeUnit.MILLISECONDS), Duration.create(10, TimeUnit.SECONDS),
          actor,
          "Dequeue",
          system.dispatcher(),
          null);
    }
    Logger.info("Started Akka system has named vm-logs");
  }
  
  public static void stop() {
    if (system != null) {
      system.shutdown();
    }
    Logger.info("Shutdown Akka system has named vm-logs");
  }
  
  static void addChannel(VMChannel channel) {
    channels.put(channel.sessionId, channel);
  }
  
  static void removeChannel(String sessionId) {
    channels.remove(sessionId);
  }
  
  @Override
  public void onReceive(Object msg) throws Exception {
    if (msg instanceof VMChannel) {
      VMChannel channel = (VMChannel) msg;
      channels.put(channel.sessionId, channel);
      getSender().tell("OK", getSelf());
    } else if (msg.equals("Dequeue")) {
      
      for (VMChannel channel : channels.values()) {
        List<VMModel> vms = VMHelper.getVMsByGroupID(channel.groupId, new BasicDBObject("system", false));
        ObjectNode jsonObj = Json.newObject();
        ArrayNode array = jsonObj.arrayNode();
        for (VMModel vm : vms) {
          ConcurrentLinkedQueue<String> queue = VMCreator.QueueHolder.get(vm.getName());
          if (queue == null) continue;
          String s = queue.poll();
          if (s != null) {
            //push to log
            StringBuilder sb = vm.getString("log") == null ? new StringBuilder() : new StringBuilder(vm.getString("log"));
            LogBuilder.log(sb, s);
            vm.put("log", sb.toString());
            
            if ("setup.vm.error".equals(s)) vm.setStatus(VMStatus.Error);
            VMHelper.updateVM(vm);
            
            array.add(Json.newObject().put("id", vm.getId()).put("msg", s));
            Logger.debug(s);
          }
          if ("log.exit".equals(s)) {
            VMCreator.QueueHolder.remove(vm.getName());
            if (!"Error".equals(vm.getString("status"))) {
              vm.setStatus(VMStatus.Ready);
              VMHelper.updateVM(vm);
            }
          }
        }
        if (array.size() != 0) {
          jsonObj.put("vms", array);
          channel.out.write(jsonObj);
        }
      }
    } else {
      unhandled(msg);
    }
  }

}
