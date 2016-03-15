/**
 * TrinhTV3@fsoft.com.vn
 */
package org.ats.services.keyword.report;

import java.util.List;

import org.ats.services.keyword.report.models.CaseReport;
import org.ats.services.keyword.report.models.StepReport;
import org.ats.services.keyword.report.models.SuiteReport;

import com.google.inject.Inject;

/**
 * @author TrinhTV3
 *
 */
public class KeywordReportService {
  
  @Inject CaseReportService caseReportService;
  
  @Inject SuiteReportService suiteReportService;
  
  @Inject StepReportService stepReportService;
  
  
  public void createSuiteReport(SuiteReport report) {
    suiteReportService.create(report);
  }
  
  public void createSuitesReport(List<SuiteReport> report) {
    SuiteReport[] suites = new SuiteReport [report.size()];
    report.toArray(suites);
    suiteReportService.create(suites);
  }
  
  public void createCasesReport(List<CaseReport> report) {
    CaseReport[] cases = new CaseReport [report.size()];
    report.toArray(cases);
    caseReportService.create(cases);
  }
  
  public void createStepsReport(List<StepReport> report) {
    StepReport[] steps = new StepReport [report.size()];
    report.toArray(steps);
    stepReportService.create(steps);
  }
}
