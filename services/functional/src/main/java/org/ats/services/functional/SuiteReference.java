/**
 * 
 */
package org.ats.services.functional;

import org.ats.services.data.common.Reference;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * May 4, 2015
 */
public class SuiteReference extends Reference<Suite> {

  @Inject
  private SuiteService service;
  
  @Inject
  SuiteReference(@Assisted("id") String id) {
    super(id);
  }

  @Override
  public Suite get() {
    return service.get(id);
  }

}
