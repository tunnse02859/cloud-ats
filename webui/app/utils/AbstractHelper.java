/**
 * 
 */
package utils;

import org.ats.component.usersmgt.DataFactory;

import com.mongodb.DB;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Sep 18, 2014
 */
public abstract class AbstractHelper {
  
  /** .*/
  protected static String databaseName = "cloud-ats-vm";
  
  /** .*/
  protected static String vmColumn = "vm";
  
  /** .*/
  protected static String offeringColumn = "offering";
  
  /** .*/
  protected static String groupOfferingColumn = "group-offering";
  
  /** .*/
  protected static String propertiesColumn = "properties";
  
  protected static DB getDatabase() {
    return DataFactory.getDatabase(databaseName);
  }

}
