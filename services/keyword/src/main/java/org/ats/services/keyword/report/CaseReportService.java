/**
 * TrinhTV3@fsoft.com.vn
 */
package org.ats.services.keyword.report;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.ats.services.data.MongoDBService;
import org.ats.services.keyword.report.models.CaseReport;
import org.ats.services.keyword.report.models.CaseReportFactory;
import org.ats.services.keyword.report.models.StepReportReference;
import org.ats.services.organization.base.AbstractMongoCRUD;
import org.ats.services.organization.entity.fatory.ReferenceFactory;

import com.google.inject.Inject;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * @author TrinhTV3
 *
 */
public class CaseReportService extends AbstractMongoCRUD<CaseReport> {
  
  private final String COL_NAME = "report-case";
  
  @Inject private ReferenceFactory<StepReportReference> stepReportRefFactory;
  
  @Inject private CaseReportFactory caseReportFactory;
  
  @Inject
  public CaseReportService(MongoDBService mongo, Logger logger) {
    this.col = mongo.getDatabase().getCollection(COL_NAME);
    this.logger = logger;
    
    //this.createTextIndex("name");
  }
  
  public void createCases(List<CaseReport> report) {
    CaseReport[] cases = new CaseReport [report.size()];
    report.toArray(cases);
    this.create(cases);
  }
  
  @SuppressWarnings("rawtypes")
  @Override
  public CaseReport transform(DBObject source) {
    
    String id = source.get("_id").toString();
    String name = source.get("name").toString();
    String suite_report_id = source.get("suite_report_id").toString();
    String case_id = source.get("case_id").toString();
    String data_source = source.get("data_source").toString();
    ArrayList list = (ArrayList) source.get("steps");
    
    List<StepReportReference> steps = new ArrayList<StepReportReference>();
    
    for (Object obj : list) {
      if (obj instanceof DBObject) {
        StepReportReference ref = stepReportRefFactory.create(((BasicDBObject) obj).getString("_id"));
        steps.add(ref);
      }
    }
    CaseReport report = caseReportFactory.create(suite_report_id, data_source, name, case_id, steps);
    report.put("_id", id);
    
    return report;
  }

}
