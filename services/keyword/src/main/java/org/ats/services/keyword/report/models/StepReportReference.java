/**
 * TrinhTV3@fsoft.com.vn
 */
package org.ats.services.keyword.report.models;

import org.ats.services.data.common.Reference;
import org.ats.services.keyword.report.StepReportService;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

/**
 * @author TrinhTV3
 *
 */
public class StepReportReference extends Reference<StepReport> {
  
  @Inject StepReportService service;
  
  @Inject
  public StepReportReference(@Assisted("id") String id) {
    super(id);
    // TODO Auto-generated constructor stub
  }

  public StepReport get() {
    return service.get(id);
  }

  @Override
  public StepReport get(String... mixins) {
    // TODO Auto-generated method stub
    return service.get(id, mixins);
  }

}
