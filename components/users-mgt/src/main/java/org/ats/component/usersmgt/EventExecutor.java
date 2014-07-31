/**
 * 
 */
package org.ats.component.usersmgt;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.ats.component.usersmgt.feature.FeatureEventListener;
import org.ats.component.usersmgt.feature.OperationEventListener;
import org.ats.component.usersmgt.group.GroupEventListener;
import org.ats.component.usersmgt.role.PermissionEventListener;
import org.ats.component.usersmgt.role.RoleEventListener;
import org.ats.component.usersmgt.user.UserEventListener;

import com.mongodb.DBCursor;
import com.mongodb.DBObject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Jul 15, 2014
 */
public class EventExecutor {

  private List<EventListener> listeners;
  
  public static final EventExecutor INSTANCE = new EventExecutor();
  
  private ScheduledExecutorService service = null;
  
  private final LinkedBlockingQueue<Event> eventQueue = new LinkedBlockingQueue<Event>();
  
  private EventExecutor() {
    this.listeners = new ArrayList<EventListener>();
    this.listeners.add(new OperationEventListener());
    this.listeners.add(new FeatureEventListener());
    this.listeners.add(new PermissionEventListener());
    this.listeners.add(new RoleEventListener());
    this.listeners.add(new GroupEventListener());
    this.listeners.add(new UserEventListener());
    
    //
    DBCursor cursor = DataFactory.getDatabase("cloud-ats").getCollection("event").find();
    while (cursor.hasNext()) {
      final DBObject obj = cursor.next();
      Event event = new Event(obj) {
        @Override
        public String getType() {
          return (String) obj.get("type");
        }
      };
      
      try {
        eventQueue.put(event);
      } catch (InterruptedException e) {
        e.printStackTrace();
        throw new RuntimeException(e);
      }
    }
  }
  
  public LinkedBlockingQueue<Event> getQueue() {
    return eventQueue;
  }

  public void start() {
    
    this.service = Executors.newSingleThreadScheduledExecutor();
    
    this.service.scheduleAtFixedRate(new Runnable() {
      
      public void run() {
        try {
          Event event = eventQueue.poll();
          if (event == null) return;

          System.out.println("recieve " + event);

          for (EventListener listener : listeners) {
            listener.execute(event);
          }
          
          event.dequeue();
        } catch (EventExecutedException e) {
          e.printStackTrace();
          throw new RuntimeException(e);
        }
      }
    }, 0, 100, TimeUnit.MILLISECONDS);
  }
  
  public void stop() {
    this.service.shutdown();
  }
  
  public long eventCount() {
    return DataFactory.getDatabase("cloud-ats").getCollection("event").count();
  }
  
  public boolean isInProgress() {
    return eventCount() != 0;
  }
}
