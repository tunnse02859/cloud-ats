/**
 * 
 */
package org.ats.services.keyword;

import org.ats.services.OrganizationContext;

import com.google.inject.assistedinject.Assisted;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * May 5, 2015
 */
public interface KeywordProjectFactory {

  public KeywordProject create(@Assisted("context") OrganizationContext context, @Assisted("name") String name);
  
}
