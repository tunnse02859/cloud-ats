/**
 * 
 */
package org.ats.component.usersmgt.feature;

import java.util.List;
import java.util.Set;

import org.ats.component.usersmgt.BaseObject;

import com.mongodb.DBObject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Jul 9, 2014
 */
public class Feature extends BaseObject<Feature> {

  private static final long serialVersionUID = 1L;
  
  public Feature() {}
  
  public Feature(String dbName, String name) {
    super(dbName);
    this.put("name", name);
  }

  public void addOperation(String operation_id) {
    if (this.get("operation_ids") != null) {
      
      String operation_ids = this.getString("operation_ids");
      StringBuilder sb =  new StringBuilder(operation_ids);
      sb.append("::").append(operation_id);
      this.put("operation_ids", sb.toString());
      
    } else {
      this.put("operation_ids", operation_id);
    }
    
  }
  
  public void addOperation(Operation operation) {
    this.addOperation(operation.getId());
  }
  
  public boolean removeOperation(String operation_id) {
    
    if (this.get("operation_ids") != null) {
      Set<String> operations = this.stringIDtoSet(this.getString("operation_ids"));
      boolean result = operations.remove(operation_id);
     
      String operation_ids = this.setToStringID(operations);
      this.put("operation_ids", operation_ids);
      
      return result;
    }
    
    return false;
  }
  
  public boolean removeOperation(Operation o) {
    return this.removeOperation(o.getId());
  }
  
  public List<Operation> getOperations() {
    return FeatureDAO.getInstance(getDbName()).getOperations(this);
  }
  
  public void setName(String name) {
    this.put("name", name);
  }
  
  public String getName() {
    return (String) this.get("name");
  }

  @Override
  public Feature from(DBObject obj) {
    this.putAll(obj);
    return this;
  }
}
