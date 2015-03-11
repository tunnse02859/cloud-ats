/**
 * 
 */
package org.ats.services.organization;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ats.services.data.common.MongoPageList;
import org.ats.services.data.common.PageList;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Mar 10, 2015
 */
public abstract class AbstractMongoCRUD<T extends DBObject> implements MongoCRUD<T> {

  /** .*/
  protected Logger logger;
  
  /** .*/
  protected DBCollection col;
  
  public void createTextIndex(String... fields) {
    String colName = col.getName();
    DBObject keys = new BasicDBObject();
    for (String field : fields) {
      keys.put(field, "text");
    }
    col.createIndex(keys);
    logger.log(Level.INFO, colName + " collection has created text index for " + keys);
  }
  
  public long count() {
    return this.col.count();
  }
  
  public void create(T obj) {
    this.col.insert(obj);
  }
  
  public void update(T obj) {
    this.col.save(obj);
  }

  public void delete(T obj) {
    this.col.remove(obj);
  }
  
  public void delete(String id) {
    this.col.remove(new BasicDBObject("_id", id)); 
   }
  
  public T get(String id) {
    DBObject source = this.col.findOne(new BasicDBObject("_id", id));
    return transform(source);
  }
  
  public PageList<T> list() {
    return list(10);
  }

  public PageList<T> list(int pageSize) {
    DBObject query = new BasicDBObject();
    return buildPageList(pageSize, col, query);
  }

  public PageList<T> query(DBObject query) {
    return query(query, 10);
  }

  public PageList<T> query(DBObject query, int pageSize) {
    return buildPageList(pageSize, col, query);
  }
  
  public PageList<T> search(String text) {
    DBObject query = new BasicDBObject("$text", new BasicDBObject("$search", text));
    return buildPageList(10, col, query);
  }
  
  
  @SuppressWarnings("serial")
  protected PageList<T> buildPageList(int pageSize, DBCollection col, DBObject query) {
    return new MongoPageList<T>(pageSize, col, query) {
      @Override
      protected List<T> get(int from) {
        DBObject sortable = null;
        
        if (this.sortableKeys != null && this.sortableKeys.size() > 0) {
          sortable = new BasicDBObject();
          for (Map.Entry<String, Boolean> entry : this.sortableKeys.entrySet()) {
            sortable.put(entry.getKey(), entry.getValue() ? 1 : -1);
          }
        }
        
        DBCursor cursor = null;
        if (sortable != null) {
          cursor = this.col.find(query).sort(sortable).skip(from).limit(pageSize);
        } else {
          cursor = this.col.find(query).skip(from).limit(pageSize);
        }
        
        List<T> list = new ArrayList<T>();
        while(cursor.hasNext()) {
          DBObject source = cursor.next();
          list.add(transform(source));
        }
        
        return list;
      }
    };
  }
}
