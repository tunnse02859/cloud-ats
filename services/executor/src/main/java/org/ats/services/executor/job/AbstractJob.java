/**
 * 
 */
package org.ats.services.executor.job;

import java.util.Date;
import java.util.Map;

import com.mongodb.BasicDBObject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Jul 21, 2015
 */
@SuppressWarnings("serial")
public abstract class AbstractJob<T extends AbstractJob<T>> extends BasicDBObject {
  
  public AbstractJob(String id, String projectId, String vmachineId) {
    this.put("_id", id);
    this.put("project_id", projectId);
    this.put("vm_id", vmachineId);
    this.put("status", Status.Running.toString());
    this.put("created_date", new Date(System.currentTimeMillis()));
  }
  
  public String getId() {
    return this.getString("_id");
  }
  
  public String getProjectId() {
    return this.getString("project_id");
  }
  
  public void setVMachineId(String vmachineId) {
    this.put("vm_id", vmachineId);
  }
  
  public String getTestVMachineId() {
    return this.getString("vm_id");
  }
  
  public void setStatus(Status status) {
    this.put("status", status.toString());
  }
  
  public Status getStatus() {
    return Status.valueOf(this.getString("status"));
  }
  
  public Date getCreatedDate() {
    return this.getDate("created_date");
  }
  
  public void appendLog(String log) {
    StringBuilder sb = new StringBuilder(getLog() == null ? "" : getLog());
    sb.append(log);
    this.put("log", sb.toString());
  }
  
  public String getLog() {
    return this.getString("log");
  }
  
  public String getResult() {
   return this.getString("result"); 
  }
  
  public abstract Map<String, String> getRawDataOutput();
  
  public abstract Type getType();

  public static enum Status {
    Completed, Running, Queued;
  }
  
  public static enum Type {
    Performance, Keyword
  }
}
