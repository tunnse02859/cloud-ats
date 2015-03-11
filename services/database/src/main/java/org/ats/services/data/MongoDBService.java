/**
 * 
 */
package org.ats.services.data;

import java.net.UnknownHostException;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mongodb.DB;
import com.mongodb.MongoClient;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Mar 9, 2015
 */
public class MongoDBService implements DatabaseService<DB> {
  
  private final String host;
  
  private final Integer port;
  
  private final String dbName;
  
  private final MongoClient client;

  @Inject
  MongoDBService(@Named("ats.cloud.db.host") String host, 
      @Named("ats.cloud.db.port") Integer port, 
      @Named("ats.cloud.db.name") String dbName) throws UnknownHostException {
    
    this.host = host;
    this.port = port;
    this.dbName = dbName;
    MongoClient client = new MongoClient(host, port);
    this.client = client;
  }

  public DB getDatabase() {
    return this.client.getDB(dbName);
  }
  
  public String getDatabaseName() {
    return dbName;
  }

  public void dropDatabase() {
    this.client.dropDatabase(dbName);
  }

  public String getHost() {
    return host;
  }

  public int getPort() {
    return port;
  }

  public String getEngine() {
    return "MongoDB";
  }
}
