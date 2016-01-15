/**
 * 
 */
package org.ats.services.datadriven;

import org.ats.services.data.common.Reference;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * May 6, 2015
 */
public class DataDrivenReference extends Reference<DataDriven> {

  @Inject
  private DataDrivenService service;
  
  @Inject
  DataDrivenReference(@Assisted("id") String id) {
    super(id);
  }

  @Override
  public DataDriven get() {
    return service.get(id);
  }

  @Override
  public DataDriven get(String... mixins) {
    return service.get(id, mixins);
  }

}
