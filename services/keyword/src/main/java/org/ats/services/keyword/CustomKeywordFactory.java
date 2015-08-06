/**
 * 
 */
package org.ats.services.keyword;

import com.google.inject.assistedinject.Assisted;

/**
 * @author NamBV2
 *
 * Aug 3, 2015
 */
public interface CustomKeywordFactory {
  public CustomKeyword create(@Assisted("projectId") String projectId, @Assisted("name") String name);
}
