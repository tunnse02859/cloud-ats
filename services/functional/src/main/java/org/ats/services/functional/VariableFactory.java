/**
 * 
 */
package org.ats.services.functional;

import java.util.HashMap;
import java.util.Map;

import com.google.inject.Inject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Apr 18, 2015
 */
public class VariableFactory {

  private final Map<String, String> variableStore;
  
  @Inject
  public VariableFactory() {
    this.variableStore = new HashMap<String, String>();
  }
  
  public String getVariable(DataType type, String name) {
    String normalize = type.getNormalize() + " " + name;
    if (variableStore.get(normalize) == null) {
      variableStore.put(normalize, normalize);
      return normalize;
    }
    return name;
  }
  
  public static enum DataType {
    STRING("String"), BOOLEAN("boolean");
    
    private String normalize;
    
    DataType(String normalize) {
      this.normalize = normalize;
    }
    
    public String getNormalize() {
      return normalize;
    }
  }
}
