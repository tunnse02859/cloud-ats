/**
 * 
 */
package org.ats.services.organization;

import org.ats.services.OrganizationServiceModule;
import org.ats.services.data.DatabaseModule;
import org.ats.services.data.MongoDBService;
import org.ats.services.event.EventModule;
import org.ats.services.event.EventService;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Mar 10, 2015
 */
public abstract class AbstractTestCase {
  
  /** .*/
  protected Injector injector;
  
  /** .*/
  protected MongoDBService mongoService;
  
  /** .*/
  protected EventService eventService;
  
  public void init(String dbName) throws Exception {
    System.setProperty(EventModule.EVENT_CONF, "src/test/resources/event.conf");
    
    String host = "localhost";
    
    int port = 27017;
    
    Injector injector = Guice.createInjector(new DatabaseModule(host, port, dbName), new EventModule(), new OrganizationServiceModule());
    
    this.mongoService = injector.getInstance(MongoDBService.class);
    this.mongoService.dropDatabase();
    this.injector = injector;
    
    //start event service
    eventService = injector.getInstance(EventService.class);
    eventService.setInjector(injector);
    eventService.start();
  }
}
