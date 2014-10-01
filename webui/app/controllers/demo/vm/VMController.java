/**
 * 
 */
package controllers.demo.vm;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.cloudstack.api.ApiConstants.VMDetails;
import org.ats.cloudstack.AsyncJobAPI;
import org.ats.cloudstack.VirtualMachineAPI;
import org.ats.cloudstack.model.Job;
import org.ats.cloudstack.model.VirtualMachine;
import org.ats.common.ssh.SSHClient;
import org.ats.jenkins.JenkinsMaster;
import org.ats.jenkins.JenkinsSlave;
import org.ats.knife.Knife;

import play.Routes;
import play.api.templates.Html;
import play.data.DynamicForm;
import play.data.Form;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.WebSocket;
import scala.collection.mutable.StringBuilder;
import utils.DBFactory;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.WriteConcern;

import views.html.demo.main;
import views.html.demo.vm.*;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * May 9, 2014
 */
public class VMController extends Controller {
  
  private static Map<String, ConcurrentLinkedQueue<String>> QueueHolder = new HashMap<String, ConcurrentLinkedQueue<String>>();
  
  public static Result doCreate() throws Exception {
    DynamicForm form = Form.form().bindFromRequest();
    int number = Integer.parseInt(form.get("number"));
    String ostype = form.get("ostype");
    String offering = form.get("vmoffering");
    
    BasicDBObject obj = new BasicDBObject();
    obj.put("os", "Ubuntu 12.04 " + ostype);
    
    if ("small".equalsIgnoreCase(offering)) {
      obj.put("cpu", "500 Mhz");
      obj.put("memory", "512 MB");
    } else if ("medium".equalsIgnoreCase(offering)) {
      obj.put("cpu", "1000 Mhz");
      obj.put("memory", "1G");
    } else if ("large".equalsIgnoreCase(offering)) {
      obj.put("cpu", "1000 Mhz");
      obj.put("memory", "2G");
    }
    
    DBCollection collection = DBFactory.getDatabase().getCollection("vm");
    DBCollection counter = DBFactory.getDatabase().getCollection("vm_counter");
    
    ObjectNode json = Json.newObject();
    
    ArrayNode html = json.putArray("html");
    ArrayNode console = json.putArray("console");
    
    for (int i = 0; i < number; i++) {
      
      DBObject count = counter.findOne();
      if (count == null) {
        count = new BasicDBObject("count", 1);
        counter.insert(count);
      } else {
        Integer n = (Integer)count.get("count") + 1;
        count.put("count", n);
        counter.save(count);
      }
      
      String name = "vm-" + count.get("count");

      Date date = new Date(System.currentTimeMillis());
      obj.put("name", name);
      obj.put("date", date);
      obj.put("status", "Starting");
      obj.put("_id", UUID.randomUUID().toString());

      scala.collection.mutable.StringBuilder sb = new scala.collection.mutable.StringBuilder("<span>+ ");
      sb.append(obj.get("os")).append("</span><br>");
      sb.append("<span>+ ").append(date).append("</span>");

      play.api.templates.Html summary = new play.api.templates.Html(sb);
      obj.put("summary", sb.toString());
      collection.insert(obj);

      QueueHolder.put(name, new ConcurrentLinkedQueue<String>());

      RunJob runJob = new RunJob(name, ostype, offering);
      runJob.start();

      html.add(vm.render(false, true, name, "linux", "Starting", summary).toString());
      console.add("<pre class='"+  name +" pre-scrollable' style='display: none;'><code></code><i class='icon-spinner icon-spin'></i></pre>");
    }
    return ok(json);
  }
  
  public static Result create() {
    return ok(modal.render());
  }
  
  public static Result list(Boolean fure) {
    DBCollection collection = DBFactory.getDatabase().getCollection("vm");
    DBCursor cursor = collection.find();
    scala.collection.mutable.StringBuilder sb = new scala.collection.mutable.StringBuilder();
    while(cursor.hasNext()) {
      DBObject obj = cursor.next();
      String name = (String)obj.get("name");
      String status = (String)obj.get("status");
      String summary = (String)obj.get("summary");
      sb.append(vm.render(fure, false, name, "linux", status, new Html(new StringBuilder(summary))));
    }
    return ok(vmlist.render(fure, new Html(sb)));
  }
  
  public static Result detail(String id) {
    DBCollection collection = DBFactory.getDatabase().getCollection("vm");
    DBCursor cursor = collection.find(new BasicDBObject("name", id));
    if (cursor.hasNext()) {
      DBObject obj = cursor.next();
      String status = (String)obj.get("status");
      String ip = (String)obj.get("ip");
      String vmid = (String)obj.get("vmid");
      String cpu = (String)obj.get("cpu");
      String memory = (String)obj.get("memory");
      return ok(vmdetail.render(status, ip, vmid, cpu, memory));
    }
    return ok(vmdetail.render("Starting", null, null, null, null));
  }
  
  public static Result log(String name) {
    DBCollection collection = DBFactory.getDatabase().getCollection("vm");
    DBCursor cursor = collection.find(new BasicDBObject("name", name));
    if (cursor.hasNext()) {
      String log = (String) cursor.next().get("log");
      return ok(vmlog.render(name, log));
    } else {
      return ok(vmlog.render(name, null));
    }
  }
  
  public static Result destroy(String name) throws Exception {
    DBCollection collection = DBFactory.getDatabase().getCollection("vm");
    DBCursor cursor = collection.find(new BasicDBObject("name", name));
    if (cursor.hasNext()) {
      DBObject obj = cursor.next();
      collection.remove(obj, WriteConcern.JOURNAL_SAFE);
      VirtualMachineAPI.destroyVM(obj.get("vmid").toString(), true);
      new JenkinsSlave(new JenkinsMaster("172.27.4.77", "http", 8080), (String) obj.get("ip")).release();
      Knife.getInstance().deleteNode(name);
    } 
    return ok("OK");
  }
  
  public static Result ajax() {
    response().setContentType("text/javascript");
    return ok(Routes.javascriptRouter("vmRouter", 
        routes.javascript.VMController.create(),
        routes.javascript.VMController.detail(),
        routes.javascript.VMController.doCreate(),
        routes.javascript.VMController.list(),
        routes.javascript.VMController.log(),
        routes.javascript.VMController.destroy(),
        routes.javascript.VMController.status()));
  } 
  
  public static class RunJob extends Thread {
    
    private final String name;
    
    private final String ostype;
    
    private final String offering;
    
    public RunJob(String name, String ostype, String offering) {
      this.name = name;
      this.ostype = ostype;
      this.offering = offering;
    }

    @Override
    public void run() {
      
      String template = "jenkins-slave";
      String cookbook = "jenkins-slave-gui";
      
      if ("server".equals(ostype)) {
        template = "jenkins-slave-non-ui";
        cookbook = "jenkins-slave";
      }
      
      ConcurrentLinkedQueue<String> queue = VMController.QueueHolder.get(name);
      
      try {
        String response[] = VirtualMachineAPI.quickDeployVirtualMachine(name, template, offering + " Instance", null);
        String vmId = response[0];
        String jobId = response[1];
        Job job = AsyncJobAPI.queryAsyncJobResult(jobId);
        
        while (!job.getStatus().done()) {
          job = AsyncJobAPI.queryAsyncJobResult(jobId);
        }
        
        if (job.getStatus() == org.apache.cloudstack.jobs.JobInfo.Status.SUCCEEDED) {
          VirtualMachine vm = VirtualMachineAPI.findVMById(vmId, VMDetails.nics);
          String ipAddress = vm.nic[0].ipAddress;
          queue.add("Created VM: " + vmId);
          queue.add("Public IP: " + ipAddress);
          
          DBCollection collection = DBFactory.getDatabase().getCollection("vm");
          DBObject dbObj = collection.findOne(new BasicDBObject("name", name));
          
          dbObj.put("status", "Running");
          dbObj.put("vmid", vmId);
          dbObj.put("ip", ipAddress);
          collection.save(dbObj);
          
          Knife knife = Knife.getInstance();
          
          queue.add("Checking sshd on " + ipAddress);
          
          if (SSHClient.checkEstablished(ipAddress, 22, 120) && knife.bootstrap(vm.nic[0].ipAddress, vm.name, queue, Arrays.asList(cookbook, "jmeter").toArray(new String[]{}))) {
            collection.save(dbObj);
          } else {
            dbObj = collection.findOne(new BasicDBObject("name", name));
            dbObj.put("status", "Error");
            collection.save(dbObj);
          }
        } else {
          DBCollection collection = DBFactory.getDatabase().getCollection("vm");
          DBObject dbObj = collection.findOne(new BasicDBObject("name", name));
          dbObj.put("status", "Error");
          collection.save(dbObj);
        }
        
      } catch (Exception e) {
        e.printStackTrace();
      } finally {
        queue.add(name + ".log.exit");
      }
    }
  }
  
  public static Result status(final String  name) {
    DBCollection collection = DBFactory.getDatabase().getCollection("vm");
    DBCursor cursor = collection.find(new BasicDBObject("name", name));
    if (cursor.hasNext()) {
      DBObject obj = cursor.next();
      return ok((String)obj.get("status"));
    } else {
      return ok("Destroying or not available");
    }
  }
  
  public static WebSocket<String> console(final String name) {
    return new WebSocket<String>() {

      @Override
      public void onReady(play.mvc.WebSocket.In<String> in, play.mvc.WebSocket.Out<String> out) {
        
        ConcurrentLinkedQueue<String> queue = VMController.QueueHolder.get(name);
        
        StringBuilder sb = new StringBuilder();
        while(true) {
          String output = queue.poll();
          if (output == null) continue;
          else if ((name + ".log.exit").equals(output)) break;
          out.write(output);
          sb.append(output).append("\n");
        }
        
        DBCollection collection = DBFactory.getDatabase().getCollection("vm");
        DBObject obj = collection.findOne(new BasicDBObject("name", name));
        obj.put("status", "Available");
        obj.put("log", sb.toString());
        collection.save(obj);
        out.close();
      }
    };
  }
}
