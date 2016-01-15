/**
 * 
 */
package org.ats.services.executor.job;

import org.ats.services.data.common.Reference;
import org.ats.services.executor.ExecutorService;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Jul 22, 2015
 */
public class KeywordJobReference extends Reference<KeywordJob> {

  @Inject ExecutorService service;
  
  @Inject
  KeywordJobReference(@Assisted("id") String id) {
    super(id);
  }

  @Override
  public KeywordJob get() {
    return (KeywordJob) service.get(id);
  }

  @Override
  public KeywordJob get(String... mixins) {
    return (KeywordJob) service.get(id, mixins);
  }

}
