/**
 * 
 */
package org.ats.component.usersmgt.feature;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ats.component.usersmgt.BaseObject;
import org.ats.component.usersmgt.UserManagementException;
import org.ats.component.usersmgt.role.Role;

import com.mongodb.DBObject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Jul 9, 2014
 */
public class Feature extends BaseObject<Feature> {

  private static final long serialVersionUID = 1L;
  
  public Feature(String name) {
    super();
    this.put("name", name);
  }
  
  public Feature(DBObject obj) {
    this.from(obj);
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
    
    if (this.get("operation_ids") == null) {
      return Collections.emptyList();
    }
     
    String[] op_ids = this.getString("operation_ids").split("::");
    Set<Operation> operations = new HashSet<Operation>();

    try {
      for (String op_id : op_ids) {
        Operation operation = OperationDAO.INSANCE.findOne(op_id);
        operations.add(operation);
      }
    } catch (UserManagementException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
    
    List<Operation> list = new ArrayList<Operation>(operations);
    Collections.sort(list, new Comparator<Operation>() {
      public int compare(Operation o1, Operation o2) {
        return o1.getString("name").compareTo(o2.getString("name"));
      }
    });
    return list;
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
