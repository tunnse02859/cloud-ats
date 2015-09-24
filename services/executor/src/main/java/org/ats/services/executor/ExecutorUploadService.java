/**
 * 
 */
package org.ats.services.executor;

import java.util.Date;
import java.util.UUID;
import java.util.logging.Logger;

import org.ats.services.data.MongoDBService;
import org.ats.services.event.Event;
import org.ats.services.event.EventFactory;
import org.ats.services.executor.job.AbstractJob;
import org.ats.services.executor.job.AbstractJob.Status;
import org.ats.services.executor.job.KeywordUploadJob;
import org.ats.services.executor.job.KeywordUploadJobFactory;
import org.ats.services.organization.base.AbstractMongoCRUD;
import org.ats.services.upload.KeywordUploadProject;
import org.ats.services.upload.KeywordUploadProjectService;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mongodb.DBObject;

/**
 * @author NamBV2
 *
 * Sep 18, 2015
 */
@Singleton
public class ExecutorUploadService extends AbstractMongoCRUD<AbstractJob<?>> {

  @Inject
  private KeywordUploadProjectService keywordUploadService;
  
  @Inject
  private KeywordUploadJobFactory keywordUploadFactory;

  @Inject
  private EventFactory eventFactory;

  private final String COL_NAME = "job";

  @Inject
  public ExecutorUploadService(MongoDBService mongoService, Logger logger) {
    this.col = mongoService.getDatabase().getCollection(COL_NAME);
    this.logger = logger;
  }

  public KeywordUploadJob execute(KeywordUploadProject project) throws Exception {
    project.setStatus(KeywordUploadProject.Status.RUNNING);
    keywordUploadService.update(project);
    
    String projectHash = project.getId().substring(0, 8) + "-" + UUID.randomUUID().toString().substring(0, 8);
    KeywordUploadJob job = keywordUploadFactory.create(projectHash, project.getId(), null, Status.Queued);
    create(job);

    Event event = eventFactory.create(job, "upload-job-tracking");
    event.broadcast();
    return job;
    
  }

  @Override
  public AbstractJob<?> transform(DBObject source) {
    String id = (String) source.get("_id");
    String projectId = (String) source.get("project_id");
    String vmachineId = (String) source.get("vm_id");
    AbstractJob.Status status = AbstractJob.Status.valueOf((String) source.get("status"));
    Date createdDate = (Date) source.get("created_date");

    String projectHash = projectId.substring(0, 8);
    KeywordUploadJob keywordJob = keywordUploadFactory.create(projectHash, projectId, vmachineId, status);
    keywordJob.put("_id", id);
    keywordJob.put("created_date", createdDate);
    keywordJob.put("report", source.get("report"));
    keywordJob.put("log", source.get("log"));

    return keywordJob;
  }
}