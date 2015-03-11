/**
 * 
 */
package org.ats.services.organization;

import org.ats.services.data.common.PageList;

import com.mongodb.DBObject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Mar 10, 2015
 */
public interface MongoCRUD<T extends DBObject> {

  public void create(T obj);
  
  public void update(T obj);
  
  public void delete(String id);
  
  public void delete(T obj);
  
  public T get(String id);
  
  public PageList<T> list();
  
  public PageList<T> list(int pageSize);
  
  public PageList<T> query(DBObject query);
  
  public PageList<T> query(DBObject query, int pageSize);
  
  public PageList<T> search(String text);
  
  public void createTextIndex(String... fields);
  
  public T transform(DBObject source);
}
