/**
 * TrinhTV3@fsoft.com.vn
 */
package org.ats.services.keyword.report.models;

import java.util.List;

import com.google.inject.assistedinject.Assisted;

/**
 * @author TrinhTV3
 *
 */
public interface SuiteReportFactory {
  
  public SuiteReport create(@Assisted("jobId") String jobId, @Assisted("name") String name, @Assisted("totalPass") int totalPass, @Assisted("totalFail") int totalFail, @Assisted("totalSkip") int totalSkip, @Assisted("totalCase") int totalCase, @Assisted("cases") List<CaseReportReference> cases);
}
