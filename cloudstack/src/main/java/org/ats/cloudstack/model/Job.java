/**
 * 
 */
package org.ats.cloudstack.model;

import org.apache.cloudstack.jobs.JobInfo;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Apr 24, 2014
 */
public class Job extends AbstractModel {

  /** .*/
  @JsonProperty("jobid")
  public String jobId;
  
  /** .*/
  @JsonProperty("jobstatus")
  public int jobStatus;
  
  public JobInfo.Status getStatus() {
    if (JobInfo.Status.IN_PROGRESS.ordinal() == jobStatus) {
      return JobInfo.Status.IN_PROGRESS;
    } else if (JobInfo.Status.SUCCEEDED.ordinal() == jobStatus) {
      return JobInfo.Status.SUCCEEDED;
    } else if (JobInfo.Status.FAILED.ordinal() == jobStatus) {
      return JobInfo.Status.FAILED;
    } else if (JobInfo.Status.CANCELLED.ordinal() == jobStatus) {
      return JobInfo.Status.CANCELLED;
    }
    return null;
  }
}
