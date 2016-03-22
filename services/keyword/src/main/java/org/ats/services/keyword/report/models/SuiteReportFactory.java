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
  
  public SuiteReport create(@Assisted("created_date") long date, @Assisted("jobId") String jobId, @Assisted("suiteId") String suiteId, @Assisted("name") String name, @Assisted("totalPass") int totalPass, @Assisted("totalFail") int totalFail, @Assisted("totalSkip") int totalSkip, @Assisted("totalCase") int totalCase, @Assisted("cases") List<CaseReportReference> cases, @Assisted("duration") long duration);
}
