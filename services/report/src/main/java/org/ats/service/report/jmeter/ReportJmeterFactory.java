package org.ats.service.report.jmeter;

import com.google.inject.assistedinject.Assisted;

public interface ReportJmeterFactory {
  public JtlHandler create(@Assisted("performaneJobId") String performaneJobId, @Assisted("scriptId") String scriptId);
}
