/**
 * 
 */
package org.ats.services.executor.job;

import java.util.Map;

import org.ats.common.MapBuilder;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Jul 21, 2015
 */
@SuppressWarnings("serial")
public class KeywordJob extends AbstractJob<KeywordJob> {

  @Inject
  KeywordJob(@Assisted("id") String id, @Assisted("projectId") String projectId, @Assisted("vmachineId") String vmachineId, @Assisted("status") Status status) {
    super(id, projectId, vmachineId);
    setStatus(status);
    this.put("type", Type.Keyword.toString());
  }

  @Override
  public Map<String, String> getRawDataOutput() {
    return this.getString("report") != null ? new MapBuilder<String, String>("report", this.getString("report")).build() : null;
  }

  @Override
  public Type getType() {
    return Type.Keyword;
  }

}
