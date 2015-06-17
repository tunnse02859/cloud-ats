/**
 * 
 */
package org.ats.services.performance;

import org.ats.services.data.common.Reference;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Jun 11, 2015
 */
public class JMeterScriptReference extends Reference<JMeterScript> {
  
  @Inject
  private JMeterScriptService service;

  @Inject
  JMeterScriptReference(@Assisted("id") String id) {
    super(id);
  }

  @Override
  public JMeterScript get() {
    return service.get(id);
  }

}
