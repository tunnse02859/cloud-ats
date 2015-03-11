/**
 * 
 */
package org.ats.common;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Mar 11, 2015
 */
public class MapBuilder<K, V> {
  
  private Map<K, V> map = new HashMap<K, V>();
  
  public MapBuilder(K key, V value) {
    map.put(key, value);
  }
  
  public MapBuilder<K, V> append(K key, V value) {
    map.put(key, value);
    return this;
  }

  public Map<K, V> build() {
    return map;
  }
}
