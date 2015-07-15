/**
 * 
 */
package org.ats.services.keyword.action;

import org.ats.services.keyword.AbstractTemplate;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Apr 8, 2015
 */
public abstract class AbstractAction extends AbstractTemplate {

  private static final long serialVersionUID = 1L;
  
  public abstract String getAction();
  
  public DBObject toJson() {
    return new BasicDBObject();
  }
}
