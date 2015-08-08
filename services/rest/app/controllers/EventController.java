/**
 * 
 */
package controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.ats.services.executor.job.AbstractJob;
import org.ats.services.organization.base.AuthenticationService;
import org.ats.services.organization.entity.User;

import com.google.inject.Inject;

import play.Logger;
import play.libs.EventSource;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import actions.CorsComposition;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Aug 8, 2015
 */
@CorsComposition.Cors
public class EventController extends Controller {
  
  private static ConcurrentHashMap<String, List<EventSource>> pool = new ConcurrentHashMap<String, List<EventSource>>();
  
  @Inject AuthenticationService<User> authenService;
  
  public void send(User user, AbstractJob<?> job) {
    String token = authenService.createToken(user);
    
    List<EventSource> events = pool.get(token);
    if (events == null) return ;
    
    for (EventSource event : events) {
      event.send(EventSource.Event.event(Json.parse(job.toString())));
    }
  }
  
  public Result close(String token) {
    List<EventSource> events = pool.get(token);
    if (events == null) return status(200);
    
    for (EventSource event : events) {
      event.close();
    }
    pool.remove(token);
    System.out.println("close");
    return status(200);
  }

  public Result feed(final String token) {
    final String remoteAddress = request().remoteAddress();
    Logger.info(remoteAddress + " - SSE connected");
   
    if (pool.get(token) != null) {
      System.out.println(pool.get(token).size());
    }
    
    return ok(new EventSource() {
      @Override
      public void onConnected() {
        
        final EventSource currentSocket = this;
        
        List<EventSource> events = pool.get(token) == null ? new ArrayList<EventSource>() : pool.get(token);
        events.add(currentSocket);
        
        pool.put(token, events);
      }
    });
  }
}
