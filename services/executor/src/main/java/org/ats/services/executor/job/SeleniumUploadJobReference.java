/**
 * 
 */
package org.ats.services.executor.job;

import org.ats.services.data.common.Reference;
import org.ats.services.executor.ExecutorService;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

/**
 * @author NamBV2
 *
 * Sep 18, 2015
 */
public class SeleniumUploadJobReference extends Reference<SeleniumUploadJob>{
  
  @Inject ExecutorService service;
  
  @Inject
  SeleniumUploadJobReference(@Assisted("id") String id) {
    super(id);
  }

  @Override
  public SeleniumUploadJob get() {
    return (SeleniumUploadJob) service.get(id);
  }
}
