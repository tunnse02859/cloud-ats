/**
 * TrinhTV3@fsoft.com.vn
 */
package org.ats.services.keyword.report;

import java.util.logging.Logger;

import org.ats.services.data.MongoDBService;
import org.ats.services.keyword.report.models.StepReport;
import org.ats.services.organization.base.AbstractMongoCRUD;

import com.google.inject.Inject;
import com.mongodb.DBObject;

/**
 * @author TrinhTV3
 *
 */
public class StepReportService extends AbstractMongoCRUD<StepReport> {
  
  private final String COL_NAME = "report-step";
  
  @Inject
  StepReportService(MongoDBService mongo, Logger logger) {
    this.col = mongo.getDatabase().getCollection(COL_NAME);
    this.logger = logger;
    this.createTextIndex("name");
  }
  
  @Override
  public StepReport transform(DBObject source) {
    // TODO Auto-generated method stub
    return null;
  }

}
