package org.ats.service.report.function;

import com.google.inject.assistedinject.Assisted;

public interface ReportTestNgFactory {
  public TestNgHandler create(@Assisted("functionalJobId") String functionalJobId);
}
