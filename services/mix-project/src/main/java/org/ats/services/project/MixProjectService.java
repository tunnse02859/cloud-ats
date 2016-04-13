/**
 * TrinhTV3@fsoft.com.vn
 */
package org.ats.services.project;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import org.ats.common.PageList;
import org.ats.service.blob.BlobService;
import org.ats.services.OrganizationContext;
import org.ats.services.data.MongoDBService;
import org.ats.services.keyword.Case;
import org.ats.services.keyword.CaseReference;
import org.ats.services.keyword.CaseService;
import org.ats.services.keyword.CustomKeyword;
import org.ats.services.keyword.CustomKeywordService;
import org.ats.services.keyword.KeywordProject;
import org.ats.services.keyword.KeywordProjectService;
import org.ats.services.keyword.Suite;
import org.ats.services.keyword.SuiteService;
import org.ats.services.organization.base.AbstractMongoCRUD;
import org.ats.services.performance.JMeterScript;
import org.ats.services.performance.JMeterScriptService;
import org.ats.services.performance.PerformanceProject;
import org.ats.services.performance.PerformanceProjectService;
import org.ats.services.upload.SeleniumUploadProject;
import org.ats.services.upload.SeleniumUploadProjectService;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFSDBFile;

/**
 * @author TrinhTV3
 *
 */

@Singleton
public class MixProjectService extends AbstractMongoCRUD<MixProject> {
  
  /** .*/
  private final String COL_NAME = "mix-project";
  
  @Inject MixProjectFactory mpFactory;
  
  @Inject PerformanceProjectService performanceService;
  
  @Inject KeywordProjectService keywordService;
  
  @Inject OrganizationContext context;
  
  @Inject JMeterScriptService jmeterService;
  
  @Inject BlobService blobService;
  
  @Inject SuiteService suiteService;
  
  @Inject CaseService caseService;
  
  @Inject SeleniumUploadProjectService seleniumService;
  
  @Inject CustomKeywordService customService;
  
  @Inject
  public MixProjectService(MongoDBService mongo, Logger logger) {
    this.col = mongo.getDatabase().getCollection(COL_NAME);
    this.logger = logger;
    
    //create text index
    this.createTextIndex("name");
  }
  
  public MixProject cloneData(String id, String name) {
      
    MixProject mp = this.get(id);
    
    //clone performance project
    PerformanceProject per = performanceService.get(mp.getPerformanceId());
    if (per != null) {
      per.put("name", name);
      per.put("created_date", new Date());
      per.put("_id", UUID.randomUUID().toString());
      per.put("creator", new BasicDBObject("_id", context.getUser().getEmail()));

      performanceService.create(per);
        
      PageList<JMeterScript> scripts = jmeterService.query(new BasicDBObject("project_id", mp.getPerformanceId()));
      while (scripts.hasNext()) {
        for (JMeterScript script : scripts.next()) {

          JMeterScript jmeter = jmeterService.get(script.getId(),"number_engines", "raw_content", "raw");
          jmeter.setProjectId(per.getId());
          String newId = UUID.randomUUID().toString();
          if (jmeter.getBoolean("raw")) {

            List<GridFSDBFile> files = blobService.find(new BasicDBObject("script_id", jmeter.getId()));
            for (GridFSDBFile file : files) {
              file.put("script_id", newId);
              blobService.create(file.getInputStream());
            }
          }
          jmeter.put("created_date", new Date());
          jmeter.put("_id", newId);
          jmeterService.create(jmeter);

        }
      }

      mp.setPerformanceId(per.getId());
    }
    
    //clone keyword project
    KeywordProject keyword = keywordService.get(mp.getKeywordId(), "value_delay");
      
    if (keyword != null) {
      keyword.put("name", name);
      keyword.put("created_date", new Date());
      keyword.put("_id", UUID.randomUUID().toString());
      keyword.put("creator", new BasicDBObject("_id", context.getUser().getEmail()));

      keywordService.create(keyword);
        
      PageList<Suite> suites = suiteService.query(new BasicDBObject("project_id", mp.getKeywordId()));
      while (suites.hasNext()) {
        for (Suite suite : suites.next()) {
          suite.put("created_date", new Date());
          BasicDBList list = new BasicDBList();
          List<CaseReference> cases = suite.getCases();

          suite.removeField("cases");
          for (CaseReference ref : cases) {
            Case caze = ref.get();
            caze.put("created_date", new Date());
            caze.put("_id", UUID.randomUUID().toString());
            caze.put("project_id", keyword.getId());
            caseService.create(caze);
            list.add(new BasicDBObject("_id", caze.getId()));
          }

          suite.put("cases", list);
          suite.put("_id", UUID.randomUUID().toString());
          suite.put("project_id", keyword.getId());
          suiteService.create(suite);

        }
      }
      
      PageList<CustomKeyword> customs = customService.query(new BasicDBObject("project_id", mp.getKeywordId()));
      while (customs.hasNext()) {
        for (CustomKeyword custom : customs.next()) {
          custom.put("_id", UUID.randomUUID().toString());
          custom.put("project_id", keyword.getId());
          customService.create(custom);
        }
      }
      
      mp.setKeywordId(keyword.getId());
    }
    
    //clone selenium project
    SeleniumUploadProject selenium = seleniumService.get(mp.getSeleniumId());
      
    if (selenium != null) {
      selenium.put("_id", UUID.randomUUID().toString());
      selenium.put("created_date", new Date());
      selenium.put("name", name);
      seleniumService.create(selenium);

      mp.setSeleniumId(selenium.getId());
    }
    mp.put("_id", UUID.randomUUID().toString());
    mp.put("name", name);
    this.create(mp);
    
    return mp;
  }
  
  @Override
  public MixProject transform(DBObject source) {
    
    String name = (String) source.get("name");
    String keywordId = (String) source.get("keyword_id");
    String performanceId = (String) source.get("performance_id");
    String seleniumId = (String) source.get("selenium_id");
    String creator = (String) source.get("creator");
    String _id = (String) source.get("_id");
    
    MixProject mp = mpFactory.create(_id, name, keywordId, performanceId, seleniumId, creator);
    mp.put("created_date", source.get("created_date"));
    
    return mp;
  }

}
