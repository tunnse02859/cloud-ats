/**
 * 
 */
package org.ats.services.keyword;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.ats.common.PageList;
import org.ats.services.data.MongoDBService;
import org.ats.services.datadriven.DataDrivenReference;
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
 * Jun 22, 2015
 */
@Singleton
public class CaseService extends AbstractMongoCRUD<Case>{

  /** .*/
  private final String COL_NAME = "func-case";
  
  @Inject
  private ReferenceFactory<DataDrivenReference> drivenRefFactory;
  
  @Inject
  private SuiteService suiteService;
  
  @Inject
  private ReferenceFactory<CaseReference> caseRefFactory;
  
  @Inject
  private CaseFactory caseFactory;
  
  @Inject
  CaseService(MongoDBService mongo, Logger logger) {
    this.col = mongo.getDatabase().getCollection(COL_NAME);
    this.logger = logger;

    this.createTextIndex("name");
  }
  
  @Override
  public Case transform(DBObject source) {
    BasicDBObject sel1 = (BasicDBObject) source;
    ObjectMapper mapper = new ObjectMapper();

    DataDrivenReference driven = null;
    String info = null;
    if (sel1.get("data_driven") != null) {
      driven = drivenRefFactory.create(((BasicDBObject)sel1.get("data_driven")).getString("_id"));
    }
    if(sel1.get("info") != null) {
      info = sel1.getString("info");
    }
    
    Case caze = caseFactory.create(sel1.getString("name"), driven,info);
    caze.put("_id", sel1.get("_id"));
    
    if (sel1.get("actions") != null) {
      BasicDBList actions = (BasicDBList) sel1.get("actions");
      for (Object bar : actions) {
        try {
          caze.addAction(mapper.readTree(bar.toString()));
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
    //super.delete(obj);
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
    super.deleteBy(new BasicDBObject("_id", obj.getId()));
  }
  
  @Override
  public void delete(String id) {
    Case caze = get(id);
    delete(caze);
  }
}
