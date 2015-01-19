/**
 * 
 */
package org.ats.component.usersmgt;

import java.net.UnknownHostException;

import com.mongodb.DB;
import com.mongodb.MongoClient;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Jul 7, 2014
 */
public class DataFactory {

  private static final MongoClient client;
  
  static {
    try {
      client = new MongoClient("cloud-ats.cloudapp.net");
    } catch (UnknownHostException e) {
      throw new RuntimeException(e);
    }
  }
  
  public static DB getDatabase(String dbName) {
    return client.getDB(dbName);
  }
  
  public static void dropDatabase(String dbName) {
    client.dropDatabase(dbName);
  }
}
