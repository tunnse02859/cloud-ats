/**
 * 
 */
package org.ats.services.event;

import akka.actor.UntypedActor;

import com.google.inject.Inject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Mar 23, 2015
 */
public class MockActor extends UntypedActor {
  
  @Inject MockService service;
  
  @Override
  public void onReceive(Object message) throws Exception {
    if (message instanceof Event) {
      Event event = (Event) (message);
      if (!"deadLetters".equals(getSender().path().name())) {
        getSender().tell(service.foo() + " " + event.getSource(), getSelf());
      }
    } else {
      unhandled(message);
    }
  }
}
