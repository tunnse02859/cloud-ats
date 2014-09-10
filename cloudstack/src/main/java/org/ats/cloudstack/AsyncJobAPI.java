/**
 * 
 */
package org.ats.cloudstack;

import java.io.IOException;

import org.ats.cloudstack.model.Job;
import org.json.JSONObject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Apr 25, 2014
 */
public class AsyncJobAPI extends CloudStackAPI {

  @Deprecated
  public static Job queryAsyncJobResult(String jobId) throws IOException   {
    return queryAsyncJobResult(CloudStackClient.getInstance(), jobId);
  }
  
  public static Job queryAsyncJobResult(CloudStackClient client, String jobId) throws IOException   {
    StringBuilder sb = new StringBuilder("command=queryAsyncJobResult&response=json&jobid=").append(jobId);
    String response = request(client, sb.toString());
    JSONObject json = new JSONObject(response).getJSONObject("queryasyncjobresultresponse");
    return buildModel(Job.class, json);
  }
}
