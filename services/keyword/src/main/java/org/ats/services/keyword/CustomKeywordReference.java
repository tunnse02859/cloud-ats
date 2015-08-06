/**
 * 
 */
package org.ats.services.keyword;

import org.ats.services.data.common.Reference;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

/**
 * @author NamBV2
 *
 * Aug 3, 2015
 */
public class CustomKeywordReference extends Reference<CustomKeyword>{
  
  @Inject
  CustomKeywordService service;
  
  @Inject
  CustomKeywordReference(@Assisted("id") String id) {
    super(id);
  }
  
  public CustomKeyword get() {
    return service.get("id");
  }
}
