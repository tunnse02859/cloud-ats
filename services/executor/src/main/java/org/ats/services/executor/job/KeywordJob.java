/**
 * 
 */
package org.ats.services.executor.job;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.ats.common.MapBuilder;
import org.ats.services.keyword.SuiteReference;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Jul 21, 2015
 */
@SuppressWarnings("serial")
public class KeywordJob extends AbstractJob<KeywordJob> {

  private List<SuiteReference> suites;
  
  @Inject
  KeywordJob(
      @Assisted("id") String id, 
      @Assisted("projectId") String projectId,
      @Assisted("suites") List<SuiteReference> suites,
      @Assisted("options") BasicDBObject options,
      @Nullable @Assisted("vmachineId") String vmachineId, 
      @Assisted("status") Status status) {
    
    super(id, projectId, vmachineId);
    setStatus(status);
    this.put("type", Type.Keyword.toString());
    this.suites = suites;
    BasicDBList list = new BasicDBList();
    for (SuiteReference ref : suites) {
      list.add(ref.toJSon());
    }
    this.put("suites", list);
    this.put("options", options);
  }

  public BasicDBObject getOptions() {
    return (BasicDBObject) this.get("options");
  }

  @Override
  public Map<String, String> getRawDataOutput() {
    return this.getString("report") != null ? new MapBuilder<String, String>("report", this.getString("report")).build() : null;
  }
  
  public byte[] getRawData() {
    return this.get("raw_data") != null ? (byte[])this.get("raw_data") : null;
  }

  @Override
  public Type getType() {
    return Type.Keyword;
  }

  public List<SuiteReference> getSuites() {
    return this.suites;
  }
}
