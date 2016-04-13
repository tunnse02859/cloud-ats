/**
 * TrinhTV3@fsoft.com.vn
 */
package org.ats.services.project;

import java.util.UUID;

import org.ats.common.PageList;
import org.ats.services.DataDrivenModule;
import org.ats.services.KeywordServiceModule;
import org.ats.services.KeywordUploadServiceModule;
import org.ats.services.MixProjectModule;
import org.ats.services.OrganizationServiceModule;
import org.ats.services.PerformanceServiceModule;
import org.ats.services.data.DatabaseModule;
import org.ats.services.data.MongoDBService;
import org.ats.services.event.EventModule;
import org.ats.services.keyword.KeywordProject;
import org.ats.services.keyword.KeywordProjectService;
import org.ats.services.performance.PerformanceProject;
import org.ats.services.performance.PerformanceProjectService;
import org.ats.services.upload.SeleniumUploadProject;
import org.ats.services.upload.SeleniumUploadProjectService;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.mongodb.BasicDBObject;

/**
 * @author TrinhTV3
 *
 */
public class MixProjectTestCase {
  
  private MongoDBService mongoService;
  
  private PerformanceProjectService performanceProjectService;
  
  private KeywordProjectService keywordProjectService;
  
  private SeleniumUploadProjectService seleniumService;
  
  private MixProjectService mpService;
  
  private MixProjectFactory mpFactory;
  
  @BeforeClass
  public void init() throws Exception {
    String host = "localhost";
    String name = "cloud-ats-v1";
    int port = 27017;
    
    Injector injector = Guice.createInjector(new DatabaseModule(host, port, name), 
        new KeywordServiceModule(), new PerformanceServiceModule(), 
        new KeywordUploadServiceModule(), new MixProjectModule(), 
        new EventModule(), new OrganizationServiceModule(), new DataDrivenModule());
    
    this.mongoService = injector.getInstance(MongoDBService.class);
    this.performanceProjectService = injector.getInstance(PerformanceProjectService.class);
    this.keywordProjectService = injector.getInstance(KeywordProjectService.class);
    this.seleniumService = injector.getInstance(SeleniumUploadProjectService.class);
    this.mpService = injector.getInstance(MixProjectService.class);
    this.mpFactory = injector.getInstance(MixProjectFactory.class);
    this.mongoService.getDatabase().getCollection("mix-project").drop();
  }
  
  @Test
  public void migrate() {
    PageList<PerformanceProject> pers = performanceProjectService.list();
    long numberPers = pers.count();
    while (pers.hasNext()) {
      for (PerformanceProject project : pers.next()) {
        BasicDBObject object = (BasicDBObject) project.get("creator");
        
        String id = UUID.randomUUID().toString();
        MixProject mp = mpFactory.create(id, project.getName(), null, project.getId(), null, object.getString("_id"));
        mpService.create(mp);
      }
    }
    
    PageList<MixProject> mps = mpService.list();
    Assert.assertEquals(mps.count(), numberPers);
    
    PageList<KeywordProject> keys = keywordProjectService.list();
    long numberKeys = keys.count();
    while (keys.hasNext()) {
      for (KeywordProject project : keys.next()) {
        BasicDBObject object = (BasicDBObject) project.get("creator");
        String id = UUID.randomUUID().toString();
        MixProject mp = mpFactory.create(id, project.getString("name"), project.getId(), null, null, object.getString("_id"));
        mpService.create(mp);
      }
    }
    Assert.assertEquals(mpService.list().count(), numberKeys + numberPers);
    
    PageList<SeleniumUploadProject> uploads = seleniumService.list();
    long numberUploads = uploads.count();
    while (uploads.hasNext()) {
      for (SeleniumUploadProject project : uploads.next()) {
        BasicDBObject object = (BasicDBObject) project.get("creator");
        String id = UUID.randomUUID().toString();
        MixProject mp = mpFactory.create(id, project.getString("name"), null, null, project.getId(), object.getString("_id"));
        mpService.create(mp);
      }
    }
    Assert.assertEquals(mpService.list().count(), numberKeys + numberPers + numberUploads);
  }
  
}
