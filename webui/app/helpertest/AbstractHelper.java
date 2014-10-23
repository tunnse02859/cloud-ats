/**
 * 
 */
package helpertest;

import org.ats.component.usersmgt.DataFactory;

import com.mongodb.DB;

import controllers.Application;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Oct 22, 2014
 */
public class AbstractHelper {

  /** .*/
  protected static final String performanceColumn = "performance";
  
  /** .*/
  protected static final String functionalColumn = "functional";
  
  protected static DB getDatabase() {
    return DataFactory.getDatabase(Application.dbName);
  }
  
}
