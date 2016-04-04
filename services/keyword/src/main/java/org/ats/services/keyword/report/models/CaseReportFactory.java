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
public interface CaseReportFactory {
  
  public CaseReport create(@Assisted("suite_report_id") String suite_report_id, @Assisted("data_source") String data_source, @Assisted("name") String name, @Assisted("case_id") String case_id, @Assisted("steps") List<StepReportReference> step, @Assisted("startTime") long start);
  
}
