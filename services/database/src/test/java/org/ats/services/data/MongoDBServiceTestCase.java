/**
 * 
 */
package org.ats.services.data;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Mar 9, 2015
 */
public class MongoDBServiceTestCase {
  
  @Before
  public void init() {
    System.setProperty(DatabaseModule.DB_CONF, "");
  }

  @Test
  public void testDefault() throws Exception {
    Injector injector = Guice.createInjector(new DatabaseModule());
    MongoDBService dbService = injector.getInstance(MongoDBService.class);
    Assert.assertEquals("localhost", dbService.getHost());
    Assert.assertEquals(27017, dbService.getPort());
    Assert.assertEquals("test-db", dbService.getDatabaseName());
  }
  
  @Test
  public void testExternalConfig() throws Exception {
    System.setProperty(DatabaseModule.DB_CONF, "src/test/resources/conf.properties");
    
    Injector injector = Guice.createInjector(new DatabaseModule());
    MongoDBService dbService = injector.getInstance(MongoDBService.class);
    Assert.assertEquals("cloud-ats.net", dbService.getHost());
    Assert.assertEquals(72071, dbService.getPort());
    Assert.assertEquals("cloud-ats", dbService.getDatabaseName());
  }
}
