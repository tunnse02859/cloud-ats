/**
 * 
 */
package org.ats.services.organization;

import org.ats.services.data.DatabaseModule;
import org.ats.services.data.MongoDBService;
import org.junit.Before;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Mar 10, 2015
 */
public class AbstractTestCase {
  
  /** .*/
  protected Injector injector;
  
  /** .*/
  protected MongoDBService mongoService;
  
  @Before
  public void init() throws Exception {
    System.setProperty(DatabaseModule.DB_CONF, "");
    Injector injector = Guice.createInjector(new DatabaseModule(), new OrganizationServiceModule());
    this.mongoService = injector.getInstance(MongoDBService.class);
    this.injector = injector;
    
    //cleanup database
    this.mongoService.dropDatabase();
  }
}
