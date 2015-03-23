/**
 * 
 */
package org.ats.services.data.common;

import java.util.Set;

import org.ats.common.PageList;

import com.mongodb.DBCollection;
import com.mongodb.DBObject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Mar 10, 2015
 */
public abstract class MongoPageList<T extends DBObject> extends PageList<T> {

  /** .*/
  private static final long serialVersionUID = 1L;
  
  /** .*/
  protected final DBCollection col;
  
  /** .*/
  protected final DBObject query;
  
  /** .*/
  protected Set<String> mixins;
  
  public MongoPageList(int pageSize, DBCollection col, DBObject query) {
    super(pageSize);
    this.col = col;
    this.query = query;
  }
  
  @Override
  public long count() {
    return this.col.find(query).count();
  }
  
  public void setMixins(Set<String> mixins) {
    this.mixins = mixins;
  }
}
