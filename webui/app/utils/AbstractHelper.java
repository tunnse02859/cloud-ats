/**
 * 
 */
package utils;

import org.ats.component.usersmgt.DataFactory;

import com.mongodb.DB;

import controllers.Application;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Sep 18, 2014
 */
public abstract class AbstractHelper {
  
  /** .*/
  protected static String vmColumn = "vm";
  
  /** .*/
  protected static String offeringColumn = "vm-offering";
  
  /** .*/
  protected static String groupOfferingColumn = "vm-group-offering";
  
  /** .*/
  protected static String propertiesColumn = "vm-properties";
  
  protected static DB getDatabase() {
    return DataFactory.getDatabase(Application.dbName);
  }

}
