/**
 * 
 */
package org.ats.component.usersmgt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Jul 7, 2014
 */
public abstract class ManagementDAO<T extends BaseObject<T>> {

  protected final String dbName;

  protected final String colName;

  public ManagementDAO(String dbName, String colName) {
    this.dbName = dbName;
    this.colName = colName;
  }
  
  protected DBCollection getColumn() {
    DB db = DataFactory.getDatabase(dbName);
    DBCollection col = db.getCollection(colName);
    return col;
  }

  public boolean create(T ... obj) throws UserManagementException {
    DB db = DataFactory.getDatabase(dbName);
    DBCollection col = db.getCollection(colName);
    WriteResult result = col.insert(obj, WriteConcern.ACKNOWLEDGED);
    boolean exist = false;
    for (DBObject index : col.getIndexInfo()) {
      if ((colName + "Index").equals(index.get("name"))) exist = true;
    }
    if (!exist) {
      col.ensureIndex(new BasicDBObject("name", "text"), colName + "Index");
      System.out.println("create " + colName + "Index");
    }
    return result.getError() == null;
  }

  public T update(T obj) throws UserManagementException {
    DB db = DataFactory.getDatabase(dbName);
    DBCollection col = db.getCollection(colName);
    col.save(obj);
    return obj;
  }

  public boolean delete(String objId) throws UserManagementException {
    DBCollection col = this.getColumn();
    T obj = this.findOne(objId);
    WriteResult result = col.remove(obj);

    if (result.getError() == null) {
      Event event = new Event(obj, dbName) {
        @Override
        public String getType() {
          return "delete-" + colName;
        }
      };

      event.broadcast();
      return true;
    }
    return false;
  }

  public boolean delete(T obj) throws UserManagementException {
    DBCollection col = this.getColumn();
    WriteResult result = col.remove(obj);

    if (result.getError() == null) {
      Event event = new Event(obj, dbName) {
        @Override
        public String getType() {
          return "delete-" + colName;
        }
      };

      event.broadcast();
      return true;
    }
    return false;
  }

  public Collection<T> find(BasicDBObject obj) throws UserManagementException {
    DBCollection col = this.getColumn();
    DBCursor cursor = col.find(obj);
    List<T> list = new ArrayList<T>();
    while (cursor.hasNext()) {
      list.add(this.transform(cursor.next()));
    }
    return list;
  }

  public T findOne(String objId) throws UserManagementException {
    DBCollection col = this.getColumn();
    DBObject obj = col.findOne(new BasicDBObject("_id", objId));
    return this.transform(obj);
  }
  
  public long count() {
    return this.getColumn().count();
  }

  /**
   * 
   * @param obj is accepted nullable
   * @return null if object source which is null
   * @throws UserManagementException
   */
  public abstract T transform(DBObject obj) throws UserManagementException;
}
