/**
 * 
 */
package org.ats.services.executor.job;

import javax.annotation.Nullable;

import org.ats.services.executor.job.AbstractJob.Status;

import com.google.inject.assistedinject.Assisted;

/**
 * @author NamBV2
 *
 * Sep 18, 2015
 */
public interface KeywordUploadJobFactory {
  public KeywordUploadJob create(
      @Assisted("id") String id, 
      @Assisted("projectId") String projectId,
      @Nullable @Assisted("vmachineId") String vmachineId, 
      @Assisted("status") Status status);
}
