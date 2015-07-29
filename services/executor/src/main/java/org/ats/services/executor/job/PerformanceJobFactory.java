/**
 * 
 */
package org.ats.services.executor.job;

import java.util.List;

import javax.annotation.Nullable;

import org.ats.services.executor.job.AbstractJob.Status;
import org.ats.services.performance.JMeterScriptReference;

import com.google.inject.assistedinject.Assisted;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Jul 21, 2015
 */
public interface PerformanceJobFactory {

  public PerformanceJob create(@Assisted("id") String id, 
      @Assisted("projectId") String projectId,
      @Assisted("scripts") List<JMeterScriptReference> scripts,
      @Nullable @Assisted("vmachineId") String vmachineId, 
      @Assisted("status") Status status);
  
}
