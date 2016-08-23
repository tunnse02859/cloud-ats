package org.ats.services.project;

import org.ats.services.DataDrivenModule;
import org.ats.services.ExecutorModule;
import org.ats.services.KeywordServiceModule;
import org.ats.services.KeywordUploadServiceModule;
import org.ats.services.MixProjectModule;
import org.ats.services.OrganizationServiceModule;
import org.ats.services.PerformanceServiceModule;
import org.ats.services.data.DatabaseModule;
import org.ats.services.event.EventModule;
import org.ats.services.event.EventService;
import org.ats.services.organization.event.AbstractEventTestCase;
import org.ats.services.schedule.ScheduleEventActor;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.inject.Guice;

public class EventScheduleTest extends AbstractEventTestCase {
  
  @BeforeMethod
  public void init() throws Exception {
    String host = "localhost";
    String name = "cloud-ats-v1";
    int port = 27017;
    this.injector = Guice.createInjector(new DatabaseModule(host, port, name), 
        new KeywordServiceModule(), new PerformanceServiceModule(), 
        new KeywordUploadServiceModule(), new MixProjectModule(), 
        new EventModule(), new OrganizationServiceModule(), new DataDrivenModule(),
        new ExecutorModule());
    
    eventService = injector.getInstance(EventService.class);
    eventService.setInjector(injector);
    eventService.start();
    eventService.schedule(ScheduleEventActor.class, 60000);
  }
  
  @Test
  public void test() throws Exception {
    Thread.sleep(1500000);
    System.out.println("Test");
  }
  
}
