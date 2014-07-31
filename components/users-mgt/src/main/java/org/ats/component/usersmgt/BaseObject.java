/**
 * 
 */
package org.ats.component.usersmgt;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Jul 7, 2014
 */
public abstract class BaseObject<E extends BaseObject<E>> extends BasicDBObject {

  private static final long serialVersionUID = 1L;

  public BaseObject() {
    this(UUID.randomUUID().toString());
  }
  
  public BaseObject(String _id) {
    this.put("_id", _id);
  }
  
  public String getId() {
    return (String)this.get("_id");
  }
  
  public abstract E from(DBObject obj);
  
  public String setToStringID(Set<String> set) {
    
    if (set == null) throw new NullPointerException("The set of id is not allowed nullable.");
    
    if (set.isEmpty()) return null;
    
    StringBuilder sb = new StringBuilder();
    
    for (Iterator<String> i = set.iterator(); i.hasNext(); ) {
      sb.append(i.next());
      if (i.hasNext()) sb.append("::");
    }
    
    return sb.toString();
  }
  
  public Set<String> stringIDtoSet(String ids) {
    
    if (ids == null) throw new NullPointerException("The ids is not allowed nullable.");
    
    if (ids.isEmpty()) throw new IllegalArgumentException("The ids must contains at least one UUID.");
    
    String[] ids_array = ids.split("::");
    Set<String> set = new HashSet<String>();
    for (String id : ids_array) {
      set.add(id);
    }
    
    return set;
  }
}
