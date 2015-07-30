package org.ats.service.report.jmeter;

import java.util.Iterator;
import java.util.Map;

import com.mongodb.BasicDBObject;

@SuppressWarnings("serial")
public class Report extends BasicDBObject {  
  
  private String performaneJobId;
  private String functionalJobId;
  private String label ;

  private SummaryReport summaryReport;

  private Map<Long, PointReport> hitsPerSecond ;
  private Map<Long, PointReport> transPersecond ;
  
  public Report() {
    this(null, null, null);
  }
  
  public Report(String label, String performaneJobId, String  functionalJobId) {
    this.label = label;
    this.put("label", label);    
    this.performaneJobId = performaneJobId;
    this.put("performane_job_id", performaneJobId);    
    this.functionalJobId = functionalJobId;
    this.put("functional_job_id", functionalJobId);
  }

  public String getPerformaneJobId() {
    return performaneJobId;
  }

  public void setPerformaneJobId(String performaneJobId) {
    this.performaneJobId = performaneJobId;
    this.put("performane_job_id", performaneJobId);
  }

  public String getFunctionalJobId() {
    return functionalJobId;
  }

  public void setFunctionalJobId(String functionalJobId) {
    this.functionalJobId = functionalJobId;
    this.put("functional_job_id", functionalJobId);
  }

  public void setSummaryReport(SummaryReport summaryReport) {
    this.summaryReport = summaryReport;
    this.put("summary", summaryReport);
  }

  public SummaryReport getSummaryReport() {
    return this.summaryReport;
  }

  public void setHitPerSecond(Map<Long, PointReport> hitsPerSecond) {
    this.hitsPerSecond = hitsPerSecond;
    BasicDBObject dbObj = new BasicDBObject();
    for (Map.Entry<Long, PointReport> entry : hitsPerSecond.entrySet()) {
      dbObj.put(entry.getKey().toString(), entry.getValue());
    }
    this.put("hits_per_second", dbObj);
  }

  public Map<Long, PointReport> getHitsPerSecond() {
    return this.hitsPerSecond;
  }

  public void setTransPersecond(Map<Long, PointReport> transPersecond) {
    this.transPersecond = transPersecond;
    BasicDBObject dbObj = new BasicDBObject();
    Iterator<Map.Entry<Long, PointReport>> iterator = transPersecond.entrySet().iterator();
    while (iterator.hasNext()) {
      Map.Entry<Long, PointReport> entry = iterator.next();
      dbObj.put(entry.getKey().toString(), entry.getValue());
    }
    this.put("trans_per_second", dbObj);
  }

  public Map<Long, PointReport> getTransPerSecond() {
    return this.transPersecond;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
    this.put("label", label);
  }
}
