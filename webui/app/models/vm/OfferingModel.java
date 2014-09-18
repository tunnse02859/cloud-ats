/**
 * 
 */
package models.vm;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Sep 17, 2014
 */
public class OfferingModel extends BasicDBObject {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  
  public OfferingModel(String id, String name, Integer cpuNumber, Integer cpuSpeed, Integer memory) {
    this.put("_id", id);
    this.put("name", name);
    this.put("cpu_number", cpuNumber);
    this.put("cpu_speed", cpuSpeed);
    this.put("memory", memory);
  }
  
  public OfferingModel() {
    this(null, null, null, null, null);
  }
  
  public String getId() {
    return this.getString("_id");
  }
  
  public String getName() {
    return this.getString("name");
  }
  
  public int getNumberOfCpu() {
    return this.getInt("cpu_number");
  }
  
  public int getCpuSpeed() {
    return this.getInt("cpu_speed");
  }
  
  public int getMemory() {
    return this.getInt("memory");
  }

  public OfferingModel from(DBObject source) {
    this.put("_id", source.get("_id"));
    this.put("group_id", source.get("group_id"));
    this.put("name", source.get("name"));
    this.put("cpu_speed", source.get("cpu_speed"));
    this.put("cpu_number", source.get("cpu_number"));
    this.put("memory", source.get("memory"));
    this.put("disabled", source.get("disabled"));
    return this;
  }
}
