/**
 * 
 */
package org.ats.services.keyword;

import com.google.inject.assistedinject.Assisted;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * May 5, 2015
 */
public interface KeywordProjectFactory {

  public KeywordProject create(@Assisted("name") String name);
  
}
