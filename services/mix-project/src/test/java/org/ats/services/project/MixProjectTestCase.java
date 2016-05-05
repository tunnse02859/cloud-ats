/**
 * TrinhTV3@fsoft.com.vn
 */
package org.ats.services.project;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
import org.ats.services.datadriven.DataDriven;
import org.ats.services.datadriven.DataDrivenReference;
import org.ats.services.datadriven.DataDrivenService;
import org.ats.services.event.EventModule;
import org.ats.services.keyword.Case;
import org.ats.services.keyword.CaseService;
import org.ats.services.keyword.KeywordProject;
import org.ats.services.keyword.KeywordProjectService;
import org.ats.services.organization.base.AuthenticationService;
import org.ats.services.organization.entity.User;
import org.ats.services.performance.JMeterScript;
import org.ats.services.performance.JMeterScriptReference;
import org.ats.services.performance.JMeterScriptService;
import org.ats.services.performance.PerformanceProject;
import org.ats.services.performance.PerformanceProjectService;
import org.ats.services.upload.SeleniumUploadProject;
import org.ats.services.upload.SeleniumUploadProjectService;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.mongodb.BasicDBObject;

/**
 * @author TrinhTV3
 *
 */
public class MixProjectTestCase  {
  
  private MongoDBService mongoService;
  
  private PerformanceProjectService performanceProjectService;
  
  private KeywordProjectService keywordProjectService;
  
  private CaseService caseService;
  
  private DataDrivenService dataService;
  
  private SeleniumUploadProjectService seleniumService;
  
  private MixProjectService mpService;
  
  private MixProjectFactory mpFactory;
  
  private AuthenticationService<User> authService;
  
  private JMeterScriptService scriptService;
  
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
    this.caseService = injector.getInstance(CaseService.class);
    this.dataService = injector.getInstance(DataDrivenService.class);
    this.seleniumService = injector.getInstance(SeleniumUploadProjectService.class);
    this.mpService = injector.getInstance(MixProjectService.class);
    this.mpFactory = injector.getInstance(MixProjectFactory.class);
    this.scriptService = injector.getInstance(JMeterScriptService.class);
    //    
    this.authService = injector.getInstance(Key.get(new TypeLiteral<AuthenticationService<User>>(){}));
  }
  
  @Test
  public void migrateDataDriven() {
    this.authService.logIn("haint@cloud-ats.net", "12345");
    PageList<KeywordProject> keys = keywordProjectService.list();
    
    List<DataDriven> holder = new ArrayList<DataDriven>();
    while (keys.hasNext()) {
      
      for (KeywordProject project : keys.next()) {
        MixProject mProject = mpService.query(new BasicDBObject("keyword_id", project.getId())).next().get(0);
        PageList<Case> caseList = caseService.getCases(project.getId());
        
        while (caseList.hasNext()) {
          for (Case caze : caseList.next()) {
            DataDrivenReference ref = caze.getDataDriven();
            if (ref != null) {
              DataDriven data = ref.get();
              data.put("mix_id", mProject.getId());
              holder.add(data);
            }
          }
        }
      }
    }

    for (DataDriven data : holder) {
      dataService.update(data);
    }
  }
  
  @Test
  public void migrateDataScripts() {
    this.authService.logIn("haint@cloud-ats.net", "12345");
    
    PageList<JMeterScript> list = scriptService.list();
    List<JMeterScript> holder = new ArrayList<JMeterScript>();
    
    while (list.hasNext()) {
    	for (JMeterScript script : list.next()) {
    		
    		 if (script.get("created_date") == null) {
    			script.put("created_date",new Date());
    			holder.add(script);
    		}
    	}
    }
    for (JMeterScript jMeterScript : holder) {
    	scriptService.update(jMeterScript);
		
	}
  }
  
  @Test
  public void migrate() {
    this.mongoService.getDatabase().getCollection("mix-project").drop();
    
    PageList<PerformanceProject> pers = performanceProjectService.list();
    List<PerformanceProject> perfHolder = new ArrayList<PerformanceProject>();
    long numberPers = pers.count();
    while (pers.hasNext()) {
      for (PerformanceProject project : pers.next()) {
        BasicDBObject object = (BasicDBObject) project.get("creator");
        
        String id = UUID.randomUUID().toString();
        MixProject mp = mpFactory.create(id, project.getName(), null, project.getId(), null, object.getString("_id"));
        mpService.create(mp);
        
        project.put("mix_id", id);
        perfHolder.add(project);
      }
    }
    
    for (PerformanceProject project : perfHolder) {
      performanceProjectService.update(project);
    }
    
    PageList<MixProject> mps = mpService.list();
    Assert.assertEquals(mps.count(), numberPers);
    
    PageList<KeywordProject> keys = keywordProjectService.list();
    long numberKeys = keys.count();
    
    List<KeywordProject> keywordHolder = new ArrayList<KeywordProject>();
    while (keys.hasNext()) {
      for (KeywordProject project : keys.next()) {
        BasicDBObject object = (BasicDBObject) project.get("creator");
        String id = UUID.randomUUID().toString();
        MixProject mp = mpFactory.create(id, project.getString("name"), project.getId(), null, null, object.getString("_id"));
        mpService.create(mp);
        
        project.put("mix_id", id);
        keywordHolder.add(project);
      }
    }
    for (KeywordProject project : keywordHolder) {
      keywordProjectService.update(project);
    }
    
    Assert.assertEquals(mpService.list().count(), numberKeys + numberPers);
    
    PageList<SeleniumUploadProject> uploads = seleniumService.list();
    List<SeleniumUploadProject> uploadHolder = new ArrayList<SeleniumUploadProject>();
    long numberUploads = uploads.count();
    while (uploads.hasNext()) {
      for (SeleniumUploadProject project : uploads.next()) {
        BasicDBObject object = (BasicDBObject) project.get("creator");
        String id = UUID.randomUUID().toString();
        MixProject mp = mpFactory.create(id, project.getString("name"), null, null, project.getId(), object.getString("_id"));
        mpService.create(mp);
        
        project.put("mix_id", id);
        uploadHolder.add(project);
      }
    }
    for (SeleniumUploadProject project : uploadHolder) {
      seleniumService.update(project);
    }
    Assert.assertEquals(mpService.list().count(), numberKeys + numberPers + numberUploads);
    
  }
  
}
