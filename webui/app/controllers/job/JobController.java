/**
 * 
 */
package controllers.job;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.ats.jenkins.JenkinsMaster;
import org.ats.jenkins.JenkinsMavenJob;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import play.Routes;
import play.api.templates.Html;
import play.data.DynamicForm;
import play.data.Form;
import play.libs.F.Callback0;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.WebSocket;
import scala.collection.mutable.StringBuilder;
import utils.DBFactory;
import views.html.job.*;
import views.html.vm.vm;
import views.html.vm.vmlist;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * May 13, 2014
 */
public class JobController extends Controller {

  private static Map<String, ConcurrentLinkedQueue<String>> QueueHolder = new HashMap<String, ConcurrentLinkedQueue<String>>();
  
  public static Result list(String jobType) {
    DBCollection collection = DBFactory.getDatabase().getCollection("job");
    DBCursor cursor = collection.find(new BasicDBObject("type", jobType));
    
   ArrayNode array = Json.newObject().arrayNode();
    while(cursor.hasNext()) {
      ObjectNode json = Json.newObject();
      
      Map<String, String> map = cursor.next().toMap();
      json.put("html", job.render(map).toString());
      
      StringBuilder sb = new StringBuilder("<pre class='");
      sb.append(map.get("name")).append("-console pre-scrollable' style='display: none; max-height: 500px;'>");
      sb.append("<code>").append(map.get("log")).append("</code></pre>");
      json.put("log", sb.toString());
      
      array.add(json);
    }
    response().setContentType("text/json");
    return ok(array);
  }
  
  public static Result remove(String jobName) throws IOException {
    DBCollection collection = DBFactory.getDatabase().getCollection("job");
    DBObject obj = collection.findOne(new BasicDBObject("name", jobName));
    collection.remove(obj);
    JenkinsMaster jmaster = new JenkinsMaster("git.sme.org", "http", 8080);
    JenkinsMavenJob job = new JenkinsMavenJob(jmaster, jobName, null, null, null, null);
    job.delete();
    return ok("OK");
  }
  
  public static Result create(String jobType, String jobName) {
    DBCollection collection = DBFactory.getDatabase().getCollection("vm");
    DBCursor cursor = collection.find();
    StringBuilder sb = new StringBuilder();
    while(cursor.hasNext()) {
      DBObject obj = cursor.next();
      String name = (String)obj.get("name");
      String status = (String)obj.get("status");
      String summary = (String)obj.get("summary");
      sb.append(vm.render(true, false, name, "linux", status, new Html(new StringBuilder(summary))));
    }
    if (jobName == null || jobName.isEmpty()) { 
      return ok(modal.render(jobType, vmlist.render(true, new Html(sb)), Collections.<String,String>emptyMap()));
    } else {
      DBObject obj = DBFactory.getDatabase().getCollection("job").findOne(new BasicDBObject("name", jobName));
      return ok(modal.render(jobType, vmlist.render(true, new Html(sb)), obj.toMap()));
    }
  }
  
  public static Result doCreate() {
    
    //
    DynamicForm form = Form.form().bindFromRequest();
    Map<String, String> map = form.data();
    DBCollection collection = DBFactory.getDatabase().getCollection("job");
    DBCollection counter = DBFactory.getDatabase().getCollection("job_counter");
    DBObject count = counter.findOne();
    if (count == null) {
      count = new BasicDBObject("count", 1);
      counter.insert(count);
    } else {
      Integer number = (Integer)count.get("count") + 1;
      count.put("count", number);
      counter.save(count);
    }
    
    //
    String jobName = "job-" + count.get("count");
    String jobStatus = "Pending";
    String jobResult = "N/A";

    QueueHolder.put(jobName, new ConcurrentLinkedQueue<String>());
    
    if ("performance".equals(map.get("type"))) {
      map.put("number", map.get("number"));
    }
    
    map.put("name", jobName);
    map.put("status", jobStatus);
    map.put("result", jobResult);
    map.put("date", new Date().toString());
    
    //
    
    BasicDBObject jobObj = new BasicDBObject(map);
    collection.insert(jobObj);
    
    return ok(job.render(map));
  }
  
  public static Result edit(String jobName) {
    DBCollection col = DBFactory.getDatabase().getCollection("job");
    DBObject job = col.findOne(new BasicDBObject("name", jobName));
    Map<String, String> update = Form.form().bindFromRequest().data();
    job.putAll(update);
    col.save(job);
    return ok("ok");
  }
  
  public static class RunJob extends Thread {
    
    private final String jobName;
    
    private final boolean again;
    
    public RunJob(String jobName, boolean again) {
      this.jobName = jobName;
      this.again = again;
    }
    
    @Override
    public void run() {
      ConcurrentLinkedQueue<String> queue = QueueHolder.get(jobName);
      if (queue == null) {
        queue = new ConcurrentLinkedQueue<String>();
        QueueHolder.put(jobName, queue);
      }
      
      try {
        DBObject jobData = DBFactory.getDatabase().getCollection("job").findOne(new BasicDBObject("name", jobName));
        DBObject vmData = DBFactory.getDatabase().getCollection("vm").findOne(new BasicDBObject("name", jobData.get("vm")));
        
        JenkinsMaster master = new JenkinsMaster("git.sme.org", "http", 8080);
        JenkinsMavenJob job = new JenkinsMavenJob(master, 
            (String)jobData.get("name"), 
            "selenium".equals(jobData.get("type")) ? (String)vmData.get("ip") : "master", 
            (String)jobData.get("git"), 
            (String)jobData.get("goals"), "");
        
        int buildNumber = again ? job.build() : job.submit();
        System.out.println(buildNumber);
        if (buildNumber == -1) {
          jobData = DBFactory.getDatabase().getCollection("job").findOne(new BasicDBObject("name", jobName));
          jobData.put("status", "Completed");
          jobData.put("result", "Failure");
          DBFactory.getDatabase().getCollection("job").save(jobData);
          return;
        }
        
        int start = 0;
        int last = 0;
        byte[] bytes = null;
        Thread.sleep(3000); //sleep 3s for stable
        while(job.isBuilding(buildNumber)) {

          bytes = job.getConsoleOutput(buildNumber, start);
          last = bytes.length;
          byte[] next = new byte[last - start];

          System.arraycopy(bytes, start, next, 0, next.length);

          start += (last - start);

          if (next.length > 0) { 
            String output = new String(next);
            queue.add(output.trim());
            if (output.indexOf("channel stopped") != -1) break;
          }
        }
        
        String result = "Success";
        if("FAILURE".equals(job.getStatus(buildNumber))) {
          result = "Failure"; 
        }
        
        jobData = DBFactory.getDatabase().getCollection("job").findOne(new BasicDBObject("name", jobName));
        jobData.put("status", "Completed");
        jobData.put("result", result);
        DBFactory.getDatabase().getCollection("job").save(jobData);
        
      } catch (Exception e) {
        DBObject jobData = DBFactory.getDatabase().getCollection("job").findOne(new BasicDBObject("name", jobName));
        jobData.put("status", "Completed");
        jobData.put("result", "Failure");
        DBFactory.getDatabase().getCollection("job").save(jobData);
        e.printStackTrace(System.out);
      } finally {
        queue.add(jobName + ".log.exit");
      }
    }
  }
  
  public static WebSocket<String> console(final String jobName) {
    return new WebSocket<String>() {
      @Override
      public void onReady(play.mvc.WebSocket.In<String> in, play.mvc.WebSocket.Out<String> out) {
        ConcurrentLinkedQueue<String> queue = JobController.QueueHolder.get(jobName);
        
        StringBuilder sb = new StringBuilder();
        while(true) {
          String output = queue.poll();
          if (output == null) continue;
          else if ((jobName + ".log.exit").equals(output)) break;
          out.write(output);
          sb.append(output).append("\n");
        }
        
        DBCollection collection = DBFactory.getDatabase().getCollection("job");
        DBObject obj = collection.findOne(new BasicDBObject("name", jobName));
        obj.put("log", sb.toString());
        collection.save(obj);
        out.close();
      }
    };
  }
  
  public static WebSocket<JsonNode> checkStatus(final String jobName) {
    return new WebSocket<JsonNode>() {
      @Override
      public void onReady(play.mvc.WebSocket.In<JsonNode> in, play.mvc.WebSocket.Out<JsonNode> out) {
        
        in.onClose(new Callback0() {
          @Override
          public void invoke() throws Throwable {
            System.out.println("job check status closing......");
          }
        });
        
        try {
          System.out.println("job check status ready........");
          DBCollection jobCol = DBFactory.getDatabase().getCollection("job");
          DBObject obj = jobCol.findOne(new BasicDBObject("name", jobName));
          if ("Pending".equals(obj.get("status"))) {
            while (true) {
              
              if("selenium".equals(obj.get("type"))) {
                
                DBObject vm = DBFactory.getDatabase().getCollection("vm").findOne(new BasicDBObject("name", obj.get("vm")));
                if ("Available".equals(vm.get("status"))) {
                  
                  if (obj.get("goals") == null || obj.get("goals").toString().isEmpty()) {
                    obj.put("goals", "clean install");
                  } else {
                    obj.put("goals", "clean install " + obj.get("goals"));
                  }
                  
                  break;
                }
                
              } else if ("performance".equals(obj.get("type"))) {
                
                int number = Integer.parseInt((String)obj.get("number"));
                boolean available = true;
                
                StringBuilder sb = new StringBuilder();
                
                for (int i = 0; i < number; i++) {
                  String vmName = (String) obj.get("vm["+ i + "]");
                  DBObject vm = DBFactory.getDatabase().getCollection("vm").findOne(new BasicDBObject("name", vmName));
                  if (!"Available".equals(vm.get("status"))) available = false;
                  sb.append(vm.get("ip"));
                  if (i < number - 1) sb.append(",");
                }
                
                if (available) {
                  obj.put("goals", "verify -Pperformance -Dtest.server=" + sb.toString());
                  break;
                }
              }
              
              Thread.sleep(1000);
              
            }
            
            obj.put("status", "In Progress");
            jobCol.save(obj);
            
            ObjectNode json = Json.newObject();
            json.put("status", "<span class='label label-info'>In Progress</span>");
            json.put("trigger", true);
            out.write(json);
            
            RunJob job = new RunJob(jobName, false);
            job.start();
          }
          
          obj = jobCol.findOne(new BasicDBObject("name", jobName));
          if("In Progress".equals(obj.get("status"))) {
            while(true) {
              DBObject job = jobCol.findOne(new BasicDBObject("name", jobName));
              if ("Completed".equals(job.get("status"))) {
                String result = "<span>N/A</span>";
                if ("Success".equals(job.get("result"))) {
                  result = "<span class='label label-success'>Success</span>";
                } else if ("Failure".equals(job.get("result"))) {
                  result = "<span class='label label-important'>Failure</span>";
                }
                
                ObjectNode json = Json.newObject();
                json.put("status", "<span class='label label-success'>Completed</span>");
                json.put("result", result);
                out.write(json); break;
              }
              
              Thread.sleep(1000);
            }
          }
        } catch (Exception e) {
          e.printStackTrace(System.out);
        } finally {
          out.close();
        }
      }
    };
  }
  
  public static Result ajax() {
    response().setContentType("text/javascript");
    return ok(Routes.javascriptRouter("jobRouter", 
        routes.javascript.JobController.list(),
        routes.javascript.JobController.create(),
        routes.javascript.JobController.doCreate(),
        routes.javascript.JobController.edit(),
        routes.javascript.JobController.remove())); 
  }
}
