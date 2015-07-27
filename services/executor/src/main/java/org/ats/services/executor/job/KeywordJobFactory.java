/**
 * 
 */
package org.ats.services.executor.job;

import org.ats.services.executor.job.AbstractJob.Status;

import com.google.inject.assistedinject.Assisted;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Jul 21, 2015
 */
public interface KeywordJobFactory {

  public KeywordJob create(@Assisted("id") String id, @Assisted("projectId") String projectId, @Assisted("vmachineId") String vmachineId, @Assisted("status") Status status);
  
}
