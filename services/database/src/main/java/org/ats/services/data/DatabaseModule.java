/**
 * 
 */
package org.ats.services.data;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Mar 9, 2015
 */
public class DatabaseModule extends AbstractModule {
  
  /** .*/
  private Properties configuration;
  
  /** .*/
  public static final String DB_CONF = "ats.cloud.db.conf";
  
  /** .*/
  public static final String DB_HOST = "ats.cloud.db.host";
  
  /** .*/
  public static final String DB_PORT = "ats.cloud.db.port";
  
  /** .*/
  public static final String DB_NAME = "ats.cloud.db.name";
  
  public DatabaseModule(String configPath) throws FileNotFoundException, IOException {
    buildConfiguration(configPath);
  }
  
  public DatabaseModule() throws FileNotFoundException, IOException {
    String configPath = System.getProperty(DB_CONF);
    buildConfiguration(configPath);
  }
  
  private void buildConfiguration(String configPath) throws FileNotFoundException, IOException {
    Properties configuration = new Properties();
    if (configPath != null && !configPath.isEmpty()) configuration.load(new FileInputStream(configPath));
    
    if (configuration.get(DB_HOST) == null)  configuration.setProperty(DB_HOST, "localhost");
    if (configuration.get(DB_PORT) == null) configuration.setProperty(DB_PORT, "27017");
    if (configuration.get(DB_NAME) == null) configuration.setProperty(DB_NAME, "test-db");
    
    this.configuration = configuration;
  }

  @Override
  protected void configure() {
    Names.bindProperties(binder(), this.configuration);
    bind(MongoDBService.class);
  }
}
