/**
 * 
 */
package org.ats.services.executor.job;

import org.ats.services.data.common.Reference;
import org.ats.services.executor.ExecutorService;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Jul 22, 2015
 */
public class PerformanceJobReference extends Reference<PerformanceJob> {

  @Inject ExecutorService service;
  
  @Inject
  PerformanceJobReference(@Assisted("id") String id) {
    super(id);
  }

  @Override
  public PerformanceJob get() {
    return (PerformanceJob) service.get(id);
  }

  @Override
  public PerformanceJob get(String... mixins) {
    return (PerformanceJob) service.get(id, mixins);
  }
}
