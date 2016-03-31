/**
 * TrinhTV3@fsoft.com.vn
 */
package org.ats.services.keyword.report;

import java.util.List;
import java.util.logging.Logger;

import org.ats.services.data.MongoDBService;
import org.ats.services.keyword.report.models.StepReport;
import org.ats.services.organization.base.AbstractMongoCRUD;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mongodb.DBObject;

/**
 * @author TrinhTV3
 *
 */
@Singleton
public class StepReportService extends AbstractMongoCRUD<StepReport> {
  
  private final String COL_NAME = "report-step";
  
  @Inject
  StepReportService(MongoDBService mongo, Logger logger) {
    this.col = mongo.getDatabase().getCollection(COL_NAME);
    this.logger = logger;
    //this.createTextIndex("name");
  }
  
  public void createSteps(List<StepReport> report) {
    StepReport[] steps = new StepReport [report.size()];
    report.toArray(steps);
    this.create(steps);
  }
  
  @Override
  public StepReport transform(DBObject source) {

    String id = source.get("_id").toString();
    String name = source.get("name").toString();
    long startTime = (Long) source.get("startTime");
    StepReport report = new StepReport(name, startTime);
    report.put("_id", id);
    
    return report;
  }

}
