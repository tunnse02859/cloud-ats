/**
 * 
 */
package org.ats.services.executor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import org.ats.services.data.MongoDBService;
import org.ats.services.event.Event;
import org.ats.services.event.EventFactory;
import org.ats.services.executor.job.AbstractJob;
import org.ats.services.executor.job.AbstractJob.Status;
import org.ats.services.executor.job.KeywordJob;
import org.ats.services.executor.job.KeywordJobFactory;
import org.ats.services.executor.job.PerformanceJob;
import org.ats.services.executor.job.PerformanceJobFactory;
import org.ats.services.executor.job.SeleniumUploadJob;
import org.ats.services.executor.job.SeleniumUploadJobFactory;
import org.ats.services.keyword.KeywordProject;
import org.ats.services.keyword.KeywordProjectService;
import org.ats.services.keyword.SuiteReference;
import org.ats.services.organization.base.AbstractMongoCRUD;
import org.ats.services.organization.entity.fatory.ReferenceFactory;
import org.ats.services.performance.JMeterScriptReference;
import org.ats.services.performance.PerformanceProject;
import org.ats.services.performance.PerformanceProjectService;
import org.ats.services.upload.SeleniumUploadProject;
import org.ats.services.upload.SeleniumUploadProjectService;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Jul 21, 2015
 */
@Singleton
public class ExecutorService extends AbstractMongoCRUD<AbstractJob<?>> {

  @Inject
  private KeywordProjectService keywordService;
  
  @Inject
  private KeywordJobFactory keywordFactory;

  @Inject
  private PerformanceProjectService perfService;
  
  @Inject
  private PerformanceJobFactory perfFactory;
  
  @Inject
  private SeleniumUploadProjectService keywordUploadService;
  
  @Inject
  private SeleniumUploadJobFactory keywordUploadFactory;

  @Inject
  private ReferenceFactory<JMeterScriptReference> jmeterRefFactory;

  @Inject
  private ReferenceFactory<SuiteReference> suiteRefFactory;

  @Inject
  private EventFactory eventFactory;

  private final String COL_NAME = "job";

  @Inject
  public ExecutorService(MongoDBService mongoService, Logger logger) {
    this.col = mongoService.getDatabase().getCollection(COL_NAME);
    this.logger = logger;
  }

  public PerformanceJob execute(PerformanceProject project, List<JMeterScriptReference> scripts) throws Exception {
    project.setStatus(PerformanceProject.Status.RUNNING);
    perfService.update(project);
    
    String projectHash = project.getId().substring(0, 8) + "-" + UUID.randomUUID().toString().substring(0, 8);
    PerformanceJob job = perfFactory.create(projectHash, project.getId(), scripts, null, Status.Queued);
    create(job);
    
    Event event = eventFactory.create(job, "performance-job-tracking");
    event.broadcast();
    
    return job;
  }
  
  public KeywordJob execute(KeywordProject project, List<SuiteReference> suites) throws Exception {
    project.setStatus(KeywordProject.Status.RUNNING);
    keywordService.update(project);
    
    String projectHash = project.getId().substring(0, 8) + "-" + UUID.randomUUID().toString().substring(0, 8);
    KeywordJob job = keywordFactory.create(projectHash, project.getId(), suites, null, Status.Queued);
    create(job);

    Event event = eventFactory.create(job, "keyword-job-tracking");
    event.broadcast();
    return job;
  }
  
  public SeleniumUploadJob execute(SeleniumUploadProject project) throws Exception {
    project.setStatus(SeleniumUploadProject.Status.RUNNING);
    keywordUploadService.update(project);
    
    String projectHash = project.getId().substring(0, 8) + "-" + UUID.randomUUID().toString().substring(0, 8);
    SeleniumUploadJob job = keywordUploadFactory.create(projectHash, project.getId(), null, Status.Queued);
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
    AbstractJob.Type type = AbstractJob.Type.valueOf((String) source.get("type"));

    String projectHash = projectId.substring(0, 8);
    switch (type) {

    case Keyword:
      BasicDBList list = (BasicDBList) source.get("suites");
      List<SuiteReference> suites = new ArrayList<SuiteReference>();
      for (Object obj : list) {
        BasicDBObject dbObj = (BasicDBObject) obj;
        suites.add(suiteRefFactory.create(dbObj.getString("_id")));
      }
      KeywordJob keywordJob = keywordFactory.create(projectHash, projectId, suites, vmachineId, status);
      keywordJob.put("_id", id);
      keywordJob.put("created_date", createdDate);
      keywordJob.put("report", source.get("report"));
      keywordJob.put("log", source.get("log"));

      return keywordJob;

    case Performance:
      list = (BasicDBList) source.get("scripts");
      List<JMeterScriptReference> scripts = new ArrayList<JMeterScriptReference>();
      for (Object obj : list) {
        BasicDBObject dbObj = (BasicDBObject) obj;
        scripts.add(jmeterRefFactory.create(dbObj.getString("_id")));
      }

      PerformanceJob perfJob = perfFactory.create(projectHash, projectId, scripts, vmachineId, status);
      perfJob.put("_id", id);
      perfJob.put("created_date", createdDate);
      perfJob.put("report", source.get("report"));
      perfJob.put("log", source.get("log"));

      return perfJob;
      
    case SeleniumUpload:
      SeleniumUploadJob seleniumJob = keywordUploadFactory.create(projectHash, projectId, vmachineId, status);
      seleniumJob.put("_id", id);
      seleniumJob.put("created_date", createdDate);
      seleniumJob.put("report", source.get("report"));
      seleniumJob.put("log", source.get("log"));
      seleniumJob.put("result", source.get("result"));

      return seleniumJob;

    default:
      throw new IllegalArgumentException();
    }
  }
}
