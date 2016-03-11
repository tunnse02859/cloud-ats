/**
 * TrinhTV3@fsoft.com.vn
 */
package org.ats.services.keyword.report;

import com.google.inject.Inject;

/**
 * @author TrinhTV3
 *
 */
public class KeywordReportService {
  
  @Inject CaseReportService caseReportService;
  
  @Inject SuiteReportService suiteReportService;
  
  @Inject StepReportService stepReportService;
  
  
}
