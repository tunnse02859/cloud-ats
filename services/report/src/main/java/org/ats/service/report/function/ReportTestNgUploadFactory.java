/**
 * 
 */
package org.ats.service.report.function;

import com.google.inject.assistedinject.Assisted;

/**
 * @author NamBV2
 *
 * Sep 24, 2015
 */
public interface ReportTestNgUploadFactory {
  public TestNgHandlerUpload create(@Assisted("functionalJobId") String functionalJobId);
}
