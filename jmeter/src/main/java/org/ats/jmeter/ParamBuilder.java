/**
 * 
 */
package org.ats.jmeter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Oct 9, 2014
 */
public class ParamBuilder {

  private Map<String, Object> map;
  
  private ParamBuilder() {
    this.map = new HashMap<String, Object>();
  }
  
  public ParamBuilder put(String name, Object value) {
    this.map.put(name, value);
    return this;
  }
  
  public Map<String, Object> build() {
    return map;
  }
  
  public static ParamBuilder start() {
    return new ParamBuilder();
  }
}
