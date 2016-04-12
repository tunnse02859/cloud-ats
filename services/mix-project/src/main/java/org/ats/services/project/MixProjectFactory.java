/**
 * TrinhTV3@fsoft.com.vn
 */
package org.ats.services.project;

import com.google.inject.assistedinject.Assisted;

/**
 * @author TrinhTV3
 *
 */
public interface MixProjectFactory {
  
  public MixProject create(@Assisted("_id") String id, @Assisted("name") String name, @Assisted("keyword_id") String keyword_id, @Assisted("performance_id") String performance_id, @Assisted("selenium_id") String selenium_id, @Assisted("creator") String creator);
}
