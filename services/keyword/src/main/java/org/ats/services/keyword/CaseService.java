/**
 * 
 */
package org.ats.services.keyword;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.ats.common.PageList;
import org.ats.services.OrganizationContext;
import org.ats.services.data.MongoDBService;
import org.ats.services.datadriven.DataDrivenReference;
import org.ats.services.organization.base.AbstractMongoCRUD;
import org.ats.services.organization.entity.fatory.ReferenceFactory;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Jun 22, 2015
 */
@Singleton
public class CaseService extends AbstractMongoCRUD<Case>{

  /** .*/
  private final String COL_NAME = "keyword-case";
  
  @Inject
  private ReferenceFactory<DataDrivenReference> drivenRefFactory;
  
  @Inject
  private SuiteService suiteService;
  
  @Inject
  private ReferenceFactory<CaseReference> caseRefFactory;
  
  @Inject
  private CaseFactory caseFactory;
  
  @Inject 
  private OrganizationContext context;
  
  @Inject
  CaseService(MongoDBService mongo, Logger logger) {
    this.col = mongo.getDatabase().getCollection(COL_NAME);
    this.logger = logger;

    this.createTextIndex("name");
    
    this.col.createIndex(new BasicDBObject("created_date", 1));
    this.col.createIndex(new BasicDBObject("project_id", 1));
  }
  
  public PageList<Case> getCases(String projectId) {
    return super.query(new BasicDBObject("project_id", projectId));
  }
  
  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Override
  public Case transform(DBObject source) {
    BasicDBObject dbObj = (BasicDBObject) source;
    ObjectMapper mapper = new ObjectMapper();
    DataDrivenReference driven = null;
    if (dbObj.get("data_driven") != null) {
      HashMap<String, Object> map = (HashMap<String, Object>) dbObj.get("data_driven");
      JSONObject json = new JSONObject(map);
      driven = drivenRefFactory.create(json.getString("_id"));
    }
    
    String creator = source.get("creator") == null ? context.getUser().getEmail() : (String) source.get("creator");
    Case caze = caseFactory.create(dbObj.getString("project_id"), dbObj.getString("name"), driven, creator);
    caze.put("_id", dbObj.get("_id"));
    
    Object date = dbObj.get("created_date");
    if (date instanceof Date) {
      caze.put("created_date", date);
    } else if (date instanceof Map) {
      Object value = ((Map) date).get("$date");
      DateTimeFormatter parser = ISODateTimeFormat.dateTime();
      caze.put("created_date", parser.parseDateTime(value.toString()).toDate());
    }
    
    if (dbObj.get("steps") != null) {
      ArrayList actions = (ArrayList) dbObj.get("steps");
      for (Object bar : actions) {
        try {
          if (bar instanceof Map) {
            JsonNode json = mapper.valueToTree(bar);
            caze.addAction(mapper.readTree(json.toString()));
          } else if (bar instanceof DBObject) {
            caze.addAction(mapper.readTree(bar.toString()));
          }
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
    }
    //
    return caze;
  }

  @Override
  public void delete(Case obj) {
    if (obj == null) return;
    super.delete(obj);
    
    CaseReference caseRef = caseRefFactory.create(obj.getId());
    PageList<Suite> list = suiteService.findIn("cases", caseRef);
    List<Suite> holder = new ArrayList<Suite>();
    
    while(list.hasNext()) {
      List<Suite> page = list.next();
      for (Suite suite : page) {
        suite.removeCase(caseRef);
        holder.add(suite);
      }
    }
    
    for (Suite suite : holder) {
      suiteService.update(suite);
    }
  }
  
  @Override
  public void delete(String id) {
    Case caze = get(id);
    delete(caze);
  }
}
