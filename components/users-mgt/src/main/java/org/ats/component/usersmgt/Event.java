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
  
  protected DBObject source;
  
  public Event(BaseObject<?> source) {
    this.source = source;
    this.put("_id", UUID.randomUUID().toString());
    this.put("source", source);
  }
  
  public Event(DBObject obj) {
    this.source = (DBObject) obj.get("source");
    this.put("_source", obj.get("source"));
    this.put("_id", obj.get("_id"));
    this.put("type", obj.get("type"));
  }
  
  public DBObject getSource() {
    return source;
  }
  
  public String getId() {
    return this.getString("_id");
  }
  
  public abstract String getType();
  
  public void broadcast() {
    this.put("type", getType());
    try {
      EventExecutor.INSTANCE.getQueue().put(this);
      DB db = DataFactory.getDatabase("cloud-ats");
      DBCollection col = db.getCollection("event");
      col.insert(this);
    } catch (InterruptedException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }
  
  public void dequeue() {
    DB db = DataFactory.getDatabase("cloud-ats");
    DBCollection col = db.getCollection("event");
    col.remove(new BasicDBObject("_id", this.get("_id")));
  }
}
