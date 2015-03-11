/**
 * 
 */
package org.ats.component.usersmgt;

import java.util.UUID;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Jul 7, 2014
 */
public abstract class Event extends BasicDBObject {

  static final long serialVersionUID = 1L;

  /** .*/
  private final String dbName;

  /** .*/
  protected BasicDBObject source;
  
  public Event(BaseObject<?> source, String dbName) {
    this.source = source;
    this.dbName = dbName;
    
    this.put("_id", UUID.randomUUID().toString());
    this.put("source", source);
    this.put("dbName", dbName);
  }
  
  public Event(DBObject obj) {
    if (obj instanceof BaseObject) {
      this.source = (BasicDBObject) obj;
    } else {
      this.source = (BasicDBObject) obj.get("source");
    }
    this.dbName = (String) obj.get("dbName");
    
    this.put("_id", obj.get("_id"));
    this.put("source", this.source);
    this.put("type", obj.get("type"));
    this.put("dbName", obj.get("dbName"));
  }
  
  public BasicDBObject getSource() {
    return source;
  }
  
  public String getDbName() {
    return dbName;
  }
  
  public String getId() {
    return this.getString("_id");
  }
  
  public abstract String getType();
  
  public void broadcast() {
    this.put("type", getType());
    try {
      EventExecutor.getInstance(dbName).getQueue().put(this);
      DB db = DataFactory.getDatabase(dbName);
      DBCollection col = db.getCollection("event");
      col.insert(this);
    } catch (InterruptedException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }
  
  public void dequeue() {
    DB db = DataFactory.getDatabase(dbName);
    DBCollection col = db.getCollection("event");
    col.remove(new BasicDBObject("_id", this.get("_id")));
  }
}
