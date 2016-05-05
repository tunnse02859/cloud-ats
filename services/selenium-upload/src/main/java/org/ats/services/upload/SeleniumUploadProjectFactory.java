/**
 * 
 */
package org.ats.services.upload;

import com.google.inject.assistedinject.Assisted;

/**
 * @author NamBV2
 *
 * Sep 17, 2015
 */
public interface SeleniumUploadProjectFactory {
  public SeleniumUploadProject create(@Assisted("name") String name, @Assisted("mix_id") String mix_id);
}
