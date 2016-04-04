/**
 * 
 */
package org.ats.services.executor.job;

import java.util.List;

import javax.annotation.Nullable;

import org.ats.services.executor.job.AbstractJob.Status;
import org.ats.services.keyword.SuiteReference;

import com.google.inject.assistedinject.Assisted;
import com.mongodb.BasicDBObject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Jul 21, 2015
 */
public interface KeywordJobFactory {

  public KeywordJob create(
      @Assisted("id") String id, 
      @Assisted("projectId") String projectId,
      @Assisted("suites") List<SuiteReference> suites,
      @Assisted("options") BasicDBObject isWindows,
      @Nullable @Assisted("vmachineId") String vmachineId, 
      @Assisted("status") Status status);
  
}
