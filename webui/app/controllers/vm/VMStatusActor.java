/**
 * 
 */
package controllers.vm;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import models.vm.VMModel;

import org.ats.cloudstack.CloudStackClient;
import org.ats.cloudstack.VirtualMachineAPI;
import org.ats.cloudstack.model.VirtualMachine;

import play.libs.Akka;
import play.libs.Json;
import play.mvc.WebSocket;
import scala.concurrent.duration.Duration;
import utils.VMHelper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import akka.actor.ActorRef;
import akka.actor.Cancellable;
import akka.actor.Props;
import akka.actor.UntypedActor;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Sep 11, 2014
 */
public class VMStatusActor extends UntypedActor {
  
  static ActorRef actor = Akka.system().actorOf(Props.create(VMStatusActor.class));
  
  final static Cancellable canceler = Akka.system().scheduler().schedule(Duration.create(100, TimeUnit.MILLISECONDS), Duration.create(3, SECONDS),
      actor,
      "Check",
      Akka.system().dispatcher(),
      null);
  
  Map<String, VMChannel> channels = new HashMap<String, VMChannel>();
  
  @Override
  public void onReceive(Object msg) throws Exception {
    if (msg instanceof VMChannel) {
      VMChannel channel = (VMChannel) msg;
      channels.put(channel.sessionId, channel);
      getSender().tell("OK", getSelf());
    } else if (msg.equals("Check")) {
      for (VMChannel channel : channels.values()) {
        CloudStackClient client = VMHelper.getCloudStackClient();
        ObjectNode jsonObj = Json.newObject();
        ArrayNode array = jsonObj.arrayNode();
        for (VMModel sel : VMHelper.getVMsByGroupID(channel.groupId)) {
          VirtualMachine vm = VirtualMachineAPI.findVMById(client, sel.getId(), null);
          if (vm == null) return;
          array.add(Json.newObject().put("id", vm.id).put("status", vm.state));
        }
        jsonObj.put("vms", array);
        channel.out.write(jsonObj);
      }
    } else {
      unhandled(msg);
    }
  }

  public static class VMChannel {
    
    final String sessionId;
    
    final String groupId;
    
    final WebSocket.Out<JsonNode> out;
    
    public VMChannel(String sessionId, String groupId, WebSocket.Out<JsonNode> channel) {
      this.sessionId = sessionId;
      this.groupId = groupId;
      this.out = channel;
    }
    
  }
}
