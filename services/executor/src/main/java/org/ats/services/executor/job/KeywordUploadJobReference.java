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
public class KeywordUploadJobReference extends Reference<KeywordUploadJob>{
  
  @Inject ExecutorService service;
  
  @Inject
  KeywordUploadJobReference(@Assisted("id") String id) {
    super(id);
  }

  @Override
  public KeywordUploadJob get() {
    return (KeywordUploadJob) service.get(id);
  }
}
