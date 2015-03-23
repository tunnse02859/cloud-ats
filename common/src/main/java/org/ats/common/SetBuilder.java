/**
 * 
 */
package org.ats.common;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Mar 23, 2015
 */
public class SetBuilder<T> {

  private Set<T> set = new HashSet<T>();
  
  public SetBuilder(T ...args) {
    set.addAll(Arrays.asList(args));
  }
  
  public SetBuilder<T> append(T arg) {
    set.add(arg);
    return this;
  }
  
  public Set<T> build() {
    return set;
  }
}
