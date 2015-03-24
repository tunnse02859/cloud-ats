/**
 * 
 */
package org.ats.services.event;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import akka.actor.UntypedActor;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Mar 23, 2015
 */
public class EventServiceTestCase {

  private Injector injector;
  
  private EventService service;
  
  private EventFactory<String> factory;
  
  @BeforeMethod
  public void init() {
    injector = Guice.createInjector(new MockModule());
    service = injector.getInstance(EventService.class);
    factory = injector.getInstance(Key.get(new TypeLiteral<EventFactory<String>>(){}));
  }
  
  @Test
  public void test() throws Exception {
    Event<String> event = factory.create("Hello world", "foo");
    try {
      event.broadcast();
      Assert.fail("Could not process event then event service is not started");
    } catch (IllegalStateException e) {
    }
    
    try {
      service.start();
      Assert.fail("Could not start event service without Guice injector");
    } catch (IllegalStateException e) {
    }
    
    service.setInjector(injector);
    service.start();
    event.broadcast();

    service.addActor(MockActor.class);
    event.broadcast();
    
    service.setSender(MockSender.class);
    event.broadcast();
  }
  
  static class MockSender extends UntypedActor{
    @Override
    public void onReceive(Object message) throws Exception {
      System.out.println(message);
      Assert.assertEquals(message, "foo Hello world");
    }
  }
}
