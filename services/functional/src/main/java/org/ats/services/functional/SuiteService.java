/**
 * 
 */
package org.ats.services.functional;

import java.io.IOException;
import java.util.logging.Logger;

import org.ats.common.PageList;
import org.ats.services.data.MongoDBService;
import org.ats.services.functional.Suite.SuiteBuilder;
import org.ats.services.organization.base.AbstractMongoCRUD;
import org.ats.services.organization.entity.fatory.ReferenceFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
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
  private final String COL_NAME = "func-suite";
  
  @Inject
  private CaseFactory caseFactory;
  
  @Inject
  private FunctionalProjectService projectService;
  
  @Inject
  private ReferenceFactory<SuiteReference> suiteRefFactory;
  
  @Inject
  SuiteService(MongoDBService mongo, Logger logger) {
    this.col = mongo.getDatabase().getCollection(COL_NAME);
    this.logger = logger;
    
    //create text index
    this.createTextIndex("suite_name");
  }
  
  @Override
  public Suite transform(DBObject source) {
    
    ObjectMapper mapper = new ObjectMapper();
    BasicDBObject obj = (BasicDBObject) source;
    SuiteBuilder builder = new SuiteBuilder();
    
    builder.packageName(obj.getString("package_name"))
      .suiteName(obj.getString("suite_name"))
      .driverVar(obj.getString("driver_var"))
      .initDriver(obj.getString("init_driver"))
      .timeoutSeconds(obj.getInt("timeout_seconds"))
      .raw((DBObject) obj.get("raw"));
    
    if (obj.get("cases") == null) {
      Suite suite = builder.build();
      suite.put("_id", source.get("_id"));
      return suite;
    }
    
    BasicDBList cases = (BasicDBList) obj.get("cases");
    for (Object foo : cases) {
      BasicDBObject sel1 = (BasicDBObject) foo;
      Case caze = caseFactory.create(sel1.getString("name"), sel1.getString("data_provider"), sel1.getString("data_provider_name"));
      
      if (sel1.get("actions") == null) {
        builder.addCases(caze);
        continue;
      }
      
      BasicDBList actions = (BasicDBList) sel1.get("actions");
      for (Object bar : actions) {
        try {
          caze.addAction(mapper.readTree(bar.toString()));
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
      
      builder.addCases(caze);
    }
    
    Suite suite = builder.build();
    suite.put("_id", source.get("_id"));
    return suite;
  }
  
  @Override
  public void delete(Suite obj) {
    if (obj == null) return;
    super.delete(obj);
    
    SuiteReference suiteRef = suiteRefFactory.create(obj.getId());
    PageList<FunctionalProject> list = projectService.findIn("suites", suiteRef);
    FunctionalProject project = list.next().get(0);
    project.removeSuite(suiteRef);
    projectService.update(project);
  }
  
  @Override
  public void delete(String id) {
    Suite suite = get(id);
    delete(suite);
  }

}
