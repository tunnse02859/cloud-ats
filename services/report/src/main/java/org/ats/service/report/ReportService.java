package org.ats.service.report;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

import org.ats.common.PageList;
import org.ats.service.report.function.ReportTestNgFactory;
import org.ats.service.report.function.SuiteReport;
import org.ats.service.report.function.TestNgHandler;
import org.ats.service.report.jmeter.JtlHandler;
import org.ats.service.report.jmeter.PointReport;
import org.ats.service.report.jmeter.ReportJmeterFactory;
import org.ats.service.report.jmeter.SummaryReport;
import org.ats.services.data.MongoDBService;
import org.ats.services.organization.base.AbstractMongoCRUD;

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
  private ReportTestNgFactory reportTestNgFactory;

  @Inject
  public ReportService(MongoDBService mongoService, Logger logger) {
    this.col = mongoService.getDatabase().getCollection(COL_NAME);
    this.col.createIndex(new BasicDBObject("performane_job_id", 1));
    this.logger = logger;
  }

  @Override
  public Report transform(DBObject source) {
    Report report = new Report();
    report.put("_id", source.get("_id"));
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

    BasicDBObject dbSuiteReports = (BasicDBObject) source.get("suite_reports");
    if (dbSuiteReports != null) {

      Map<String, SuiteReport> suiteMap = new TreeMap<String, SuiteReport>();
      for (String key : suiteMap.keySet()) {
        BasicDBObject suitedb = (BasicDBObject) suiteMap.get(key);
        SuiteReport suiteReport = new SuiteReport();
        suiteReport.setName(suitedb.getString("name"));
        suiteReport.setRunningTime(suitedb.getDate("running_time"));
        suiteReport.setTotalTestCase(suitedb.getInt("total_test_case"));
        suiteReport.setTotalFail(suitedb.getInt("total_fail"));
        suiteReport.setTotalPass(suitedb.getInt("total_pass"));
        suiteReport.setTotalSkip(suitedb.getInt("total_skip"));
        suiteReport.setTestResult(suitedb.getBoolean("test_result"));
        suiteMap.put(suiteReport.getName(), suiteReport);
      }
      report.setSuiteReports(suiteMap);
    }

    return report;
  }

  public PageList<Report> getList(String jobId, Type jobType, String scriptId) throws Exception {
    PageList<Report> list = null;
    if (jobType == Type.PERFORMANCE) {
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
          create(report);
        }
      }
      return list;
    } else if (jobType == Type.FUNCTIONAL) {
      list = query(new BasicDBObject("functional_job_id", jobId));
      if (list.count() == 0) {
        TestNgHandler testNgHandler = reportTestNgFactory.create(jobId);
        testNgHandler.startParsing();
        Map<String, SuiteReport> listSuiteReport = testNgHandler.getListSuiteReport();
        Report report = new Report();
        report.setLabel("Function report of " + jobId);
        report.setFunctionalJobId(jobId);
        report.setSuiteReports(listSuiteReport);
        create(report);
      }
      return list;
    }
    return null;

  }

  public static enum Type {
    PERFORMANCE, FUNCTIONAL;
  }
}
