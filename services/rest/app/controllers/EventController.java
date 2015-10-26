/**
 * 
 */
package controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.ats.jenkins.JenkinsMaster;
import org.ats.jenkins.JenkinsMavenJob;
import org.ats.services.executor.job.AbstractJob;
import org.ats.services.executor.job.KeywordJob;
import org.ats.services.keyword.KeywordProject;
import org.ats.services.keyword.KeywordProjectService;
import org.ats.services.organization.base.AuthenticationService;
import org.ats.services.organization.entity.User;
import org.ats.services.performance.PerformanceProject;
import org.ats.services.performance.PerformanceProjectService;
import org.ats.services.upload.SeleniumUploadProject;
import org.ats.services.upload.SeleniumUploadProjectService;
import org.ats.services.vmachine.VMachine;
import org.ats.services.vmachine.VMachineService;

import play.Logger;
import play.libs.EventSource;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import actions.CorsComposition;

import com.google.inject.Inject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Aug 8, 2015
 */
@CorsComposition.Cors
public class EventController extends Controller {
  
  private static ConcurrentHashMap<String, List<EventSource>> pool = new ConcurrentHashMap<String, List<EventSource>>();
  
  @Inject AuthenticationService<User> authenService;
  
  @Inject VMachineService vmachineService;
  
  @Inject PerformanceProjectService performanceService;
  
  @Inject KeywordProjectService keywordService;
  
  @Inject SeleniumUploadProjectService uploadProjectService;
  
  public void send(User user, AbstractJob<?> job) throws Exception {
    VMachine jenkinsVM;
    
    if (AbstractJob.Type.Performance == job.getType()) {
      PerformanceProject project = performanceService.get(job.getProjectId());
      jenkinsVM = vmachineService.getSystemVM(project.getTenant(), project.getSpace());
    } else {
      if (job instanceof KeywordJob) {
        KeywordProject project = keywordService.get(job.getProjectId());
        jenkinsVM = vmachineService.getSystemVM(project.getTenant(), project.getSpace());
      } else {
        SeleniumUploadProject project = uploadProjectService.get(job.getProjectId());
        jenkinsVM = vmachineService.getSystemVM(project.getTenant(), project.getSpace());
      }
      
    }
    
    JenkinsMaster jenkinsMaster = new JenkinsMaster(jenkinsVM.getPublicIp(), "http", "/jenkins", 8080);
    JenkinsMavenJob jenkinsJob = new JenkinsMavenJob(jenkinsMaster, job.getId(), null , null, null);
    
    //Send for everyone
    if (user == null) {
     Logger.info("Send to all user");
     for (List<EventSource> list : pool.values()) {
       for (EventSource event : list) {
         event.send(EventSource.Event.event(Json.parse(job.toString())));
       }
     }
     return;
    }
    
    String token = authenService.createToken(user);
    
    List<EventSource> events = pool.get(token);
    if (events == null) return ;
    
    for (EventSource event : events) {
      boolean isBuilding;
      try {
        isBuilding = jenkinsJob.isBuilding(1);
      } catch (Exception e) {
        isBuilding = false;
      }
      job.put("isBuilding", isBuilding);
      try {
        event.send(EventSource.Event.event(Json.parse(job.toString())));
      } catch (Exception e) {
        e.printStackTrace();
        System.out.println(job);
      }
    }
  }
  
  public Result close(String token) {
    List<EventSource> events = pool.get(token);
    if (events == null) return status(200);
    
    for (EventSource event : events) {
      event.close();
    }
    pool.remove(token);

    return status(200);
  }

  public Result feed(final String token) {
    return ok(new EventSource() {
      @Override
      public void onConnected() {
        List<EventSource> events = pool.get(token) == null ? new ArrayList<EventSource>() : pool.get(token);
        if (!events.contains(this)) {
          
          events.add(this);
          pool.put(token, events);
        } else {
          Logger.info("Event is already");
        }
      }
    });
  }
}
