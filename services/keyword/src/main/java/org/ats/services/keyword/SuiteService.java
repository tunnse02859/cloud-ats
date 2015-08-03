/**
 * 
 */
package org.ats.services.keyword;

import java.util.List;
import java.util.logging.Logger;

import org.ats.common.PageList;
import org.ats.services.data.MongoDBService;
import org.ats.services.keyword.Suite.SuiteBuilder;
import org.ats.services.organization.base.AbstractMongoCRUD;
import org.ats.services.organization.entity.fatory.ReferenceFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * May 4, 2015
 */
@Singleton
public class SuiteService extends AbstractMongoCRUD<Suite> {

  /** .*/
  private final String COL_NAME = "keyword-suite";
  
  @Inject
  private ReferenceFactory<CaseReference> caseRefFactory;
  
  @Inject
  SuiteService(MongoDBService mongo, Logger logger) {
    this.col = mongo.getDatabase().getCollection(COL_NAME);
    this.logger = logger;
    
    //create text index
    this.createTextIndex("suite_name");
    
    this.col.createIndex(new BasicDBObject("created_date", 1));
    this.col.createIndex(new BasicDBObject("project_id", 1));
  }
  
  public PageList<Suite> getSuites(String projectId) {
    return query(new BasicDBObject("project_id", projectId));
  }
  
  @Override
  public Suite transform(DBObject source) {
    
    BasicDBObject obj = (BasicDBObject) source;
    SuiteBuilder builder = new SuiteBuilder();
    
    builder.packageName(obj.getString("package_name"))
      .suiteName(obj.getString("suite_name"))
      .driverVar(obj.getString("driver_var"))
      .initDriver(obj.getString("init_driver"))
      .timeoutSeconds(obj.getInt("timeout_seconds"))
      .raw((DBObject) obj.get("raw"))
      .projectId(obj.getString("project_id"));
    
    if (obj.get("cases") == null) {
      Suite suite = builder.build();
      suite.put("_id", source.get("_id"));
      suite.put("created_date", obj.getDate("created_date"));
      return suite;
    }
    
    BasicDBList cases = (BasicDBList) obj.get("cases");
    for (Object foo : cases) {
      BasicDBObject sel1 = (BasicDBObject) foo;

      CaseReference caseRef = caseRefFactory.create(sel1.getString("_id"));
      builder.addCases(caseRef);
    }
    
    Suite suite = builder.build();
    suite.put("_id", source.get("_id"));
    suite.put("created_date", obj.getDate("created_date"));
    return suite;
  }
  
  @Override
  public void create(Suite... suites) {
    for (Suite suite : suites) {
      if (suite.getProjectId() == null) throw new IllegalArgumentException("Suite (" + suite.getId() + ") can not create without project_id ");
    }
    super.create(suites);
  }
  
  @Override
  public void create(List<DBObject> list) {
    for (DBObject obj : list) {
      if (obj.get("project_id") == null) throw new IllegalArgumentException("Suite (" + obj.get("_id") + " can not create without project_id");
    }
    super.create(list);
  }
  
  @Override
  public void update(Suite suite) {
    if (suite.getProjectId() == null) throw new IllegalArgumentException("Suite (" + suite.getId() + ") can not create without project_id ");
    super.update(suite);
  }
}
