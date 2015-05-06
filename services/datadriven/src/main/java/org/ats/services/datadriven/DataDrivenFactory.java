/**
 * 
 */
package org.ats.services.datadriven;

import com.google.inject.assistedinject.Assisted;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * May 6, 2015
 */
public interface DataDrivenFactory {

  public DataDriven create(@Assisted("name") String name, @Assisted("dataSource") String dataSource);
}
