/**
 * TrinhTV3@fsoft.com.vn
 */
package org.ats.services.keyword.report.models;

import org.ats.services.data.common.Reference;
import org.ats.services.keyword.report.CaseReportService;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

/**
 * @author TrinhTV3
 *
 */
public class CaseReportReference extends Reference<CaseReport> {

  @Inject
  private CaseReportService service;
  
  @Inject
  public CaseReportReference(@Assisted("id")String id) {
    super(id);
  }

  @Override
  public CaseReport get() {
   
    return service.get(this.id);
  }

  @Override
  public CaseReport get(String... mixins) {
    return service.get(this.id, mixins);
  }

}
