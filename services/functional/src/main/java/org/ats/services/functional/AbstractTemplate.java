/**
 * 
 */
package org.ats.services.functional;

import java.io.IOException;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Apr 8, 2015
 */
@SuppressWarnings("serial")
public abstract class AbstractTemplate extends BasicDBObject {

  public abstract String transform() throws IOException;
  
  public abstract DBObject toJson();
}
