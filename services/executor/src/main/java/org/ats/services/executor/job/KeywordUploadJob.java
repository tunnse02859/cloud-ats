/**
 * 
 */
package org.ats.services.executor.job;

import java.util.Map;

import javax.annotation.Nullable;

import org.ats.common.MapBuilder;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

/**
 * @author NamBV2
 *
 * Sep 18, 2015
 */
@SuppressWarnings("serial")
public class KeywordUploadJob extends AbstractJob<KeywordUploadJob>{

  @Inject
  public KeywordUploadJob(
      @Assisted("id") String id, 
      @Assisted("projectId") String projectId,
      @Nullable @Assisted("vmachineId") String vmachineId, 
      @Assisted("status") Status status) {
    
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

  public byte[] getRawData() {
    return this.get("raw_report") != null ? (byte[])this.get("raw_report") : null;
  }
  
}
