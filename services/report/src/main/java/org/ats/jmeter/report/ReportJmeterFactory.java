package org.ats.jmeter.report;

import com.google.inject.assistedinject.Assisted;

public interface ReportJmeterFactory {
  public JtlHandler create(@Assisted("performaneJobId") String performaneJobId);

}
