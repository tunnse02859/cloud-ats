/**
 * 
 */
package org.ats.services.keyword;

import org.ats.services.data.common.Reference;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Jun 22, 2015
 */
public class CaseReference extends Reference<Case> {

  @Inject
  private CaseService service;
  
  @Inject
  CaseReference(@Assisted("id") String id) {
    super(id);
  }

  @Override
  public Case get() {
    return service.get(id);
  }

}
