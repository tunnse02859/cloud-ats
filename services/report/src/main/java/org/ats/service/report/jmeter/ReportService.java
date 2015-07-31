package org.ats.service.report.jmeter;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;

import org.ats.common.PageList;
import org.ats.services.data.MongoDBService;
import org.ats.services.organization.base.AbstractMongoCRUD;
import org.xml.sax.SAXException;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

@Singleton
public class ReportService extends AbstractMongoCRUD<Report> {
  
  private final String COL_NAME = "report";
  
  @Inject
  private ReportJmeterFactory reportJmeterFactory;

  @Inject
  public ReportService(MongoDBService mongoService, Logger logger) {
    this.col = mongoService.getDatabase().getCollection(COL_NAME);
    this.col.createIndex(new BasicDBObject("performane_job_id", 1));
    this.logger = logger;
  }

  @Override
  public Report transform(DBObject source) {
    Report report = new Report();
    report.setLabel(((BasicDBObject) source).getString("label"));
    report.setPerformaneJobId(((BasicDBObject) source).getString("performane_job_id"));
    report.setFunctionalJobId(((BasicDBObject) source).getString("functional_job_id"));
    report.setScriptId(((BasicDBObject) source).getString("script_id"));
    BasicDBObject dbSummary = (BasicDBObject) source.get("summary");    
    if (dbSummary != null) {
      SummaryReport summaryReport = new SummaryReport();
      summaryReport.setLabel(dbSummary.getString("label"));
      summaryReport.setSamples(dbSummary.getInt("samples"));
      summaryReport.setAverage(dbSummary.getDouble("average"));
      summaryReport.setMinTime(dbSummary.getInt("min_time"));
      summaryReport.setMaxTime(dbSummary.getInt("max_time"));
      summaryReport.setStandardDeviation(dbSummary.getDouble("standard_deviation"));
      summaryReport.setErrorPercent(dbSummary.getDouble("error_percent"));
      summaryReport.setThroughtput(dbSummary.getDouble("throughtput"));
      summaryReport.setKbPerSecond(dbSummary.getDouble("kb_per_second"));
      summaryReport.setAverageBytes(dbSummary.getDouble("average_bytes"));
      summaryReport.setNumberAssertion(dbSummary.getInt("number_assertion"));
      summaryReport.setNumberFailuresAssertion(dbSummary.getInt("number_failures_assertion"));
      summaryReport.setPercentFailuresAssertion(dbSummary.getDouble("percent_failures_assertion"));      
      report.setSummaryReport(summaryReport);
    }
    
    BasicDBObject dbHits = (BasicDBObject) source.get("hits_per_second");
    if (dbHits != null) {
      Map<Long, PointReport> hitsMap = new TreeMap<Long, PointReport>();
      for (String key : dbHits.keySet()) {

        BasicDBObject dbPoint = (BasicDBObject) dbHits.get(key);
        PointReport pointReport = new PointReport();
        pointReport.setTimestamp(dbPoint.getLong("timestamp"));
        pointReport.setDate(dbPoint.getDate("date"));
        pointReport.setValue(dbPoint.getInt("value"));
        hitsMap.put(Long.parseLong(key), pointReport);
      }

      report.setHitPerSecond(hitsMap);
    }

    BasicDBObject dbTrans = (BasicDBObject) source.get("trans_per_second");
    if (dbTrans != null) {

      Map<Long, PointReport> transMap = new TreeMap<Long, PointReport>();
      for (String key : dbTrans.keySet()) {
        BasicDBObject dbPoint = (BasicDBObject) dbTrans.get(key);
        PointReport pointReport = new PointReport();
        pointReport.setTimestamp(dbPoint.getLong("timestamp"));
        pointReport.setDate(dbPoint.getDate("date"));
        pointReport.setValue(dbPoint.getInt("value"));
        transMap.put(Long.parseLong(key), pointReport);
      }

      report.setTransPersecond(transMap);
    }
    return report;
  }

  public PageList<Report> getList(String jobId, Type jobType, String scriptId) throws SAXException, IOException, ParserConfigurationException {
    PageList<Report> list = null;
    if (jobType  == Type.PERFORMANCE) {
      list = query(new BasicDBObject("performane_job_id", jobId).append("script_id", scriptId));
      if (list.count() == 0) {
        JtlHandler jtlHandler = reportJmeterFactory.create(jobId, scriptId);
        jtlHandler.startParsing();
        Iterator<Map.Entry<String, Report>> iterator = jtlHandler.getTotalUrlMap().entrySet().iterator();
        while (iterator.hasNext()) {
          Map.Entry<String, Report> entry = iterator.next();
          Report report = entry.getValue();
          report.setHitPerSecond(report.getHitsPerSecond());
          report.setTransPersecond(report.getTransPerSecond());
          create(entry.getValue());
        }
      }
      return list;
    } else if (jobType == Type.FUNCTIONAL) {
      
    }
    return null;
  }

  public static enum Type {
    PERFORMANCE, FUNCTIONAL;
  }
}
