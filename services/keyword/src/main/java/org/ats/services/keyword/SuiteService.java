/**
 * 
 */
package org.ats.services.keyword;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.ats.common.PageList;
import org.ats.services.data.MongoDBService;
import org.ats.services.organization.base.AbstractMongoCRUD;
import org.ats.services.organization.entity.fatory.ReferenceFactory;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import com.google.inject.Inject;
import com.google.inject.Singleton;
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
  private SuiteFactory suiteFactory;
  
  @Inject
  SuiteService(MongoDBService mongo, Logger logger) {
    this.col = mongo.getDatabase().getCollection(COL_NAME);
    this.logger = logger;
    
    //create text index
    this.createTextIndex("name");
    
    this.col.createIndex(new BasicDBObject("created_date", 1));
    this.col.createIndex(new BasicDBObject("project_id", 1));
  }
  
  public PageList<Suite> getSuites(String projectId) {
    return query(new BasicDBObject("project_id", projectId));
  }
  
  @SuppressWarnings("rawtypes")
  @Override
  public Suite transform(DBObject source) {
    
    BasicDBObject obj = (BasicDBObject) source;
    
    List<CaseReference> list = new ArrayList<CaseReference>();
    
    ArrayList cases = (ArrayList)  obj.get("cases");
    for (Object foo : cases) {
      if (foo instanceof Map) {
        CaseReference caseRef = caseRefFactory.create((String)((Map)foo).get("_id"));
        list.add(caseRef);
      } else if (foo instanceof DBObject) {
        CaseReference caseRef = caseRefFactory.create(((BasicDBObject)foo).getString("_id"));
        list.add(caseRef);
      }
    }
    
    Suite suite = suiteFactory.create(obj.getString("project_id"), obj.getString("name"), obj.getString("init_driver"), list);

    suite.put("_id", source.get("_id"));
    
    Object date = obj.get("created_date"); 
    if (date instanceof Date) {
      suite.put("created_date", date);
    } else if (date instanceof Map) {
      Object value = ((Map) date).get("$date");
      DateTimeFormatter parser = ISODateTimeFormat.dateTime();
      suite.put("created_date", parser.parseDateTime(value.toString()).toDate());
    }
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
