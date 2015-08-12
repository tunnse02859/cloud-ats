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

import play.Logger;
import play.libs.EventSource;
import play.libs.F.Callback0;
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
  
  public void send(User user, AbstractJob<?> job) {
    
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
    return status(200);
  }

  public Result feed(final String token) {
    return ok(new EventSource() {
      @Override
      public void onConnected() {
        List<EventSource> events = pool.get(token) == null ? new ArrayList<EventSource>() : pool.get(token);
        if (!events.contains(this)) {
          
          this.onDisconnected(new Callback0() {
            @Override
            public void invoke() throws Throwable {
              List<EventSource> events = pool.get(token);
              if (events == null) return;
              events.remove(this);
              Logger.info("Disconnect an event source for token " + token + ", total " + events.size());
              pool.put(token, events);
            }
          });
          
          events.add(this);
          Logger.info("Add an event source for token " + token + ", total  " + events.size());
          pool.put(token, events);
        } else {
          Logger.info("Event is already");
        }
      }
    });
  }
}
