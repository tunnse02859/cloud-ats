/**
 * 
 */
package org.ats.services.functional.locator;

import java.io.IOException;

import org.ats.services.functional.AbstractTemplate;
import org.ats.services.functional.Value;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Apr 8, 2015
 */
@SuppressWarnings("serial")
public abstract class AbstractLocator extends AbstractTemplate {

  protected Value locator;
  
  AbstractLocator(Value locator) {
    this.locator = locator;
  }
  
  public abstract String transform() throws IOException;
  
  public DBObject toJson() {
    return new BasicDBObject();
  }

  public Value getLocator()  {
    return locator;
  }
}
