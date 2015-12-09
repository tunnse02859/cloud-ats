package org.ats.services.vmachine;

/**
 * @author TrinhTV3
 *
 */

import org.ats.services.data.common.Reference;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Mar 13, 2015
 */
public class VMachineReference extends Reference<VMachine>{

  /** .*/
  private VMachineService service;
  
  @Inject
  VMachineReference(VMachineService service, @Assisted("id") String id) {
    super(id);
    this.service = service;
  }

  @Override
  public VMachine get() {
    return service.get(id);
  }

}
