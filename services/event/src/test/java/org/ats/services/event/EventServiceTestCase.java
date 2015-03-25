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

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Mar 23, 2015
 */
public class EventServiceTestCase {

  private Injector injector;
  
  private EventService service;
  
  private EventFactory factory;
  
  @BeforeMethod
  public void init() throws Exception {
    injector = Guice.createInjector(new MockModule(), new EventModule("src/test/resources/event.conf"));
    service = injector.getInstance(EventService.class);
    factory = injector.getInstance(EventFactory.class);
  }
  
  @Test
  public void test() throws Exception {
    Event event = factory.create("Hello world", "foo");
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
    
    Assert.assertEquals(service.getActors().size(), 1);

    service.setListener(MockSender.class);
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
