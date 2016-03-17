/**
 * TrinhTV3@fsoft.com.vn
 */
package org.ats.services.keyword.report;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.ats.services.data.MongoDBService;
import org.ats.services.keyword.report.models.CaseReportReference;
import org.ats.services.keyword.report.models.SuiteReport;
import org.ats.services.keyword.report.models.SuiteReportFactory;
import org.ats.services.organization.base.AbstractMongoCRUD;
import org.ats.services.organization.entity.fatory.ReferenceFactory;

import com.google.inject.Inject;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * @author TrinhTV3
 *
 */
public class SuiteReportService extends AbstractMongoCRUD<SuiteReport> {
  
  private final String COL_NAME = "report-suite";
  
  @Inject private ReferenceFactory<CaseReportReference> caseReportRefFactory;
  
  @Inject private SuiteReportFactory suiteReportFactory;
  
  @Inject
  public SuiteReportService(MongoDBService mongo, Logger logger) {
    this.col = mongo.getDatabase().getCollection(COL_NAME);
    this.logger = logger;
    
    this.createTextIndex("name");
  }
  
  public void createSuites(List<SuiteReport> report) {
    SuiteReport[] suites = new SuiteReport [report.size()];
    report.toArray(suites);
    this.create(suites);
  }
  
  @SuppressWarnings("rawtypes")
  @Override
  public SuiteReport transform(DBObject source) {
    
    String name = source.get("name").toString();
    int totalPass = (Integer) source.get("totalPass");
    int totalFail = (Integer) source.get("totalFail");
    int totalSkip = (Integer) source.get("totalSkip");
    int totalCase = (Integer) source.get("totalCase");
    
    String jobId = source.get("jobId").toString();
    
    List cases = (ArrayList) source.get("cases");
    List<CaseReportReference> list = new ArrayList<CaseReportReference>();
    
    for (Object obj : cases) {
      if (obj instanceof DBObject) {
        CaseReportReference ref = caseReportRefFactory.create(((BasicDBObject) obj).getString("_id"));
        list.add(ref);
      }
    }
    
    SuiteReport report = suiteReportFactory.create(jobId, name, totalPass, totalFail, totalSkip, totalCase, list);
    String id = source.get("_id").toString();
    report.put("_id", id);
    return report;
    
  }

}
