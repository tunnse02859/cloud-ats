package org.ats.service.report.function;

import java.util.Date;

import com.mongodb.BasicDBObject;

@SuppressWarnings("serial")
public class SuiteReport extends BasicDBObject{
  
  private String name = "";
  private Date runningTime;
  private int totalTestCase = 0;
  private int totalFail = 0;
  private int totalPass = 0;
  private int totalSkip =0;
  
  private boolean testResult = false;
  
  public SuiteReport() {
   this.put("total_test_case", 0);
   this.put("total_pass", 0);
   this.put("total_fail", 0);   
   this.put("total_skip", 0);
   this.put("test_result", true);
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
    this.put("name", name);
  }

  public Date getRunningTime() {
    return runningTime;
  }

  public void setRunningTime(Date runningTime) {
    this.runningTime = runningTime;
    this.put("running_time", runningTime);
  }

  public int getTotalTestCase() {
    return totalTestCase;
  }

  public void setTotalTestCase(int totalTestCase) {
    this.totalTestCase = totalTestCase;
    this.put("total_test_case", totalTestCase);
  }

  public int getTotalFail() {
    return totalFail;
  }

  public void setTotalFail(int totalFail) {
    this.totalFail = totalFail;
    this.put("total_fail", totalFail);
  }

  public int getTotalPass() {
    return totalPass;
  }

  public void setTotalPass(int totalPass) {
    this.totalPass = totalPass;
    this.put("total_pass", totalPass);
  }

  public int getTotalSkip() {
    return totalSkip;
  }

  public void setTotalSkip(int totalSkip) {
    this.totalSkip = totalSkip;
    this.put("total_skip", totalSkip);
  }

  public boolean isTestResult() {
    return testResult;
  }

  public void setTestResult(boolean testResult) {
    this.testResult = testResult;
    this.put("test_result", testResult);
  }
  
  
  
  
  
  

}
