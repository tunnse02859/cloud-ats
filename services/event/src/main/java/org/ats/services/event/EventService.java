/**
 * 
 */
package org.ats.services.event;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import scala.concurrent.duration.Duration;
import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.IndirectActorProducer;
import akka.actor.Props;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Mar 23, 2015
 */
@Singleton
public class EventService {

  /** .*/
  private ActorSystem system;
  
  /** .*/
  private ActorRef listener;

  /** .*/
  private Injector injector;

  /** .*/
  private Map<Class<? extends Actor>, String> clazzes;
  
  /** .*/
  private Map<Class<? extends Actor>, ActorRef> actors;

  /** .*/
  @Inject
  private Logger logger;

  @Inject
  EventService(@Named("ats.cloud.event.actors") Map<Class<? extends Actor>, String> clazzes) {
    this.clazzes = clazzes;
    this.actors = new HashMap<Class<? extends Actor>, ActorRef>();
  }

  public void setInjector(Injector injector) {
    this.injector = injector;
  }

  public void addActor(Class<? extends Actor> clazz, String name) {
    clazzes.put(clazz, name);
  }
  
  public Map<Class<? extends Actor>, ActorRef> getActors() {
    return actors;
  }
  
  public void schedule(Class<? extends Actor> clazz, long timeout) {
    ActorRef actor = 
        actors.get(clazz) == null ? 
            system.actorOf(Props.create(GenericDependencyInjector.class, injector, clazz), clazz.getName()) : actors.get(clazz);
    system.scheduler().schedule(
        Duration.create(0, TimeUnit.MILLISECONDS), 
        Duration.create(timeout, TimeUnit.MILLISECONDS), 
        actor, "schedule", system.dispatcher(), null);        
  }

  public void process(Event event) {
    if (system == null) throw new IllegalStateException("The event service is not started");
    for (Map.Entry<Class<? extends Actor>, String> entry : clazzes.entrySet()) {
      Class<? extends Actor> clazz = entry.getKey();
      String name = entry.getValue();
      
      ActorRef actor = 
          actors.get(clazz) == null ? 
              system.actorOf(Props.create(GenericDependencyInjector.class, injector, clazz), name) : actors.get(clazz);
              
      actors.put(clazz, actor);
      actor.tell(event, listener);
    }
  }
  
  public void setListener(Props props) {
    this.listener = system.actorOf(props);
  }
  
  public void setListener(Class<? extends Actor> listener) {
    this.listener = system.actorOf(Props.create(GenericDependencyInjector.class, injector, listener));
  }
  
  public ActorRef getListener() {
    return listener;
  }

  public void start() {
    if (injector == null) throw new IllegalStateException("Could not start event service without Guice injector");
    if (system == null) {
      system = ActorSystem.create("ats-event-system");
    }
  }

  public void stop() {
    if (system != null) {
      system.shutdown();
    }
    logger.info("Shutdown Akka system has named 'ats-event-system'");
  }
  
  static class GenericDependencyInjector implements IndirectActorProducer {

    /** .*/
    private Injector injector;
    
    /** .*/
    private Class<? extends Actor> clazz;
    
    GenericDependencyInjector(Injector injector, Class<? extends Actor> clazz) {
      this.injector = injector;
      this.clazz = clazz;
    }
    
    public Class<? extends Actor> actorClass() {
      return clazz;
    }

    public Actor produce() {
      return injector.getInstance(clazz);
    }
  }
}
