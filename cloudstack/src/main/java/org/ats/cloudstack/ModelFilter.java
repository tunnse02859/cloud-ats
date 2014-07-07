/**
 * 
 */
package org.ats.cloudstack;

import java.util.Collection;

import org.ats.cloudstack.model.AbstractModel;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Apr 26, 2014
 */
public interface ModelFilter<T extends AbstractModel> {
  
  public void doFilter(Collection<T> holder, T model);
}
