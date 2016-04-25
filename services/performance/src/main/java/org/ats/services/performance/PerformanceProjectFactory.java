/**
 * 
 */
package org.ats.services.performance;

import com.google.inject.assistedinject.Assisted;

/**
 * @author NamBV2
 *
 * Jun 17, 2015
 */
public interface PerformanceProjectFactory {
  
  public PerformanceProject create(@Assisted("name") String name, @Assisted("mix_id") String mix_id);
}
