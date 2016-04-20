/**
 * 
 */
package org.ats.services.keyword;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.ats.common.MapBuilder;
import org.ats.common.StringUtil;
import org.ats.services.organization.entity.fatory.ReferenceFactory;
import org.rythmengine.RythmEngine;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Apr 8, 2015
 */
@SuppressWarnings("serial")
public class Suite extends AbstractTemplate {
  
  @Inject ReferenceFactory<CaseReference> caseRefFactory;
  
  @Inject
  Suite(@Assisted("projectId") String projectId, 
      @Assisted("suiteName") String suiteName, 
      @Assisted("initDriver") String initDriver,
      @Assisted("cases") List<CaseReference> cases,
      @Assisted("creator") String creator) {
    
    this.put("_id", UUID.randomUUID().toString());
    this.put("name", suiteName);
    this.put("init_driver", initDriver);
    this.put("created_date", new Date());
    this.put("creator", creator);
    
    BasicDBList list = new BasicDBList();
    for (CaseReference caze : cases) {
      list.add(caze.toJSon());
    }
    this.put("cases", list);
    this.put("project_id", projectId);
  }
  
  public String getId() {
    return this.getString("_id");
  }
  
  public String getProjectId() {
    return this.getString("project_id");
  }
  
  public String getName() {
    return this.getString("name");
  }
  
  public Date getCreatedDate() {
    return this.getDate("created_date");
  }
  
  public List<CaseReference> getCases() {
    BasicDBList list = (BasicDBList) this.get("cases");
    List<CaseReference> cases = new ArrayList<CaseReference>();
    for (Object obj : list) {
      cases.add(caseRefFactory.create(((BasicDBObject) obj).getString("_id")));
    }
    return Collections.unmodifiableList(cases);
  }
  
  public void removeCase(CaseReference caseRef) {
    BasicDBList list = new BasicDBList();
    BasicDBList cases = (BasicDBList) this.get("cases");
    for (Object obj : cases) {
      BasicDBObject caze = (BasicDBObject) obj;
      if (caze.getString("_id").equals(caseRef.getId())) continue;
      list.add(caze);
    }
    this.put("cases", list);
  }
  
  public void setMode(boolean mode) {
    this.put("sequence_mode",mode);
  }
  
  public boolean getMode() {
    return this.getString("sequence_mode") != null ? this.getBoolean("sequence_mode") : false;
  }
  
  public String transform() throws IOException {
    return transform(null, 0, false);
  }
  
  public String getCreator() {
    return this.getString("creator");
  }
  
  public String transform(String jobId, int valueDelay,boolean sequenceMode) throws IOException {
    String suite = "";
    if(sequenceMode) {
      suite = StringUtil.readStream(Thread.currentThread().getContextClassLoader().getResourceAsStream("suite.with.priority.java.tmpl"));
    } else {
      suite = StringUtil.readStream(Thread.currentThread().getContextClassLoader().getResourceAsStream("suite.java.tmpl"));
    }
    StringBuilder sbCase = new StringBuilder();
    int order = 0;
    
    BasicDBList list = this.get("cases") != null ? (BasicDBList) this.get("cases") : new BasicDBList();
    for (Object obj : list) {
      order ++;
      CaseReference caze = caseRefFactory.create(((BasicDBObject) obj).getString("_id"));
      sbCase.append(caze.get().transform(valueDelay,sequenceMode,order));
    }
    
    RythmEngine engine = new RythmEngine(new MapBuilder<String, Boolean>("codegen.compact", false).build());

    return engine.render(suite, this.getId(), jobId, StringUtil.normalizeName((String) this.get("name")), this.getString("init_driver"), sbCase.toString());
  }
  
}
