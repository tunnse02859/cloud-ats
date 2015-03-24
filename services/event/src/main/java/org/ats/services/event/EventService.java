/**
 * 
 */
package org.ats.services.event;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.IndirectActorProducer;
import akka.actor.Props;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;

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
  private ActorRef sender;

  /** .*/
  private Injector injector;

  /** .*/
  private Set<Class<? extends Actor>> clazzes;
  
  /** .*/
  private Map<Class<? extends Actor>, ActorRef> actors;

  /** .*/
  private Logger logger;

  @Inject
  EventService(Logger logger) {
    clazzes = new HashSet<Class<? extends Actor>>();
    this.logger = logger;
    this.actors = new HashMap<Class<? extends Actor>, ActorRef>();
  }

  public void setInjector(Injector injector) {
    this.injector = injector;
  }

  public void addActor(Class<? extends Actor> clazz) {
    clazzes.add(clazz);
  }

  public void process(Event<?> event) {
    if (system == null) throw new IllegalStateException("The event service is not started");
    for (Class<? extends Actor> clazz : clazzes) {
      ActorRef actor = actors.get(clazz) == null ? system.actorOf(Props.create(GenericDependencyInjector.class, injector, clazz)) : actors.get(clazz);
      actors.put(clazz, actor);
      actor.tell(event, sender);
    }
  }
  
  public void setSender(Class<? extends Actor> sender) {
    this.sender = system.actorOf(Props.create(sender), "sender");
  }
  
  public ActorRef getSender() {
    return sender;
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
