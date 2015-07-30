package org.ats.service.report.jmeter;

import com.mongodb.BasicDBObject;

@SuppressWarnings("serial")
public class SummaryReport extends BasicDBObject  { 

  public long firsTimestamp = Long.MAX_VALUE, lastTimestamp = 0, lastTimeFinal = 0, totalTimestamp = 0, connectionTime = 0, totalBytes = 0;

  public int maxConnectionTime = 0, minConnectionTime = Integer.MAX_VALUE;

  public int samples = 0, maxTime = 0, minTime = Integer.MAX_VALUE, numberFailuresAssertion = 0, numberAssertion = 0, failures = 0;

  public double average = 0, standardDeviation = 0, errorPercent = 0, throughtput = 0, kbPerSecond = 0, averageBytes = 0, percentFailuresAssertion = 0;

  public String label = "";

  public SummaryReport(){
    this(null);
  }

  public SummaryReport(String label){
    this.label = label;
    this.put("label", label);
    this.put("samples", samples);
    this.put("average", average);
    this.put("max_time", maxTime);
    this.put("min_time", minTime);
    this.put("standard_deviation", standardDeviation);
    this.put("error_percent", errorPercent);
    this.put("throughtput", throughtput);
    this.put("kb_per_second", kbPerSecond);
    this.put("average_bytes", averageBytes);

    this.put("number_assertion", numberAssertion);
    this.put("number_failures_assertion", numberFailuresAssertion);
    this.put("percent_failures_assertion", percentFailuresAssertion);
  } 

  public String getLabel(){
    return this.getString("label");
  }

  public int getSamples(){
    return this.getInt("samples");
  }

  public double getAverage(){
    return this.getDouble("average");
  }

  public int getMinTime(){
    return this.getInt("min_time");
  }

  public int getMaxTime(){
    return this.getInt("max_time");
  }

  public double getStandardDeviation(){
    return this.getDouble("standard_deviation");      
  }

  public double getErrorPercent(){
    return this.getDouble("error_percent");
  }
  public double getThroughtput(){
    return this.getDouble("throughtput");
  }
  public double getKbPerSecond(){
    return this.getDouble("kb_per_second");
  }
  public double getAverageBytes(){
    return this.getDouble("average_bytes");
  }  

  public void setLabel(String label) {
    this.label = label;
    this.put("label", label);
  }

  public void setSamples(int samples) {
    this.samples = samples;
    this.put("samples", samples);
  }

  public void setAverage(double average) {
    this.average = average;
    this.put("average", average);
  }

  public void setMaxTime(int maxTime) {
    this.maxTime = maxTime;
    this.put("max_time", maxTime);
  }

  public void setMinTime(int minTime) {
    this.minTime = minTime;
    this.put("min_time", minTime);
  }

  public void setStandardDeviation(double standardDeviation) {
    this.standardDeviation = standardDeviation;
    this.put("standard_deviation", standardDeviation);
  }

  public void setErrorPercent(double errorPercent) {
    this.errorPercent = errorPercent;
    this.put("error_percent", errorPercent);
  }

  public void setThroughtput(double throughtput) {
    this.throughtput = throughtput;
    this.put("throughtput", throughtput);
  }

  public void setKbPerSecond(double kbPerSecond) {
    this.kbPerSecond = kbPerSecond;
    this.put("kb_per_second", kbPerSecond);
  }

  public void setAverageBytes(double averageBytes) {
    this.averageBytes = averageBytes;
    this.put("average_bytes", averageBytes);
  }

  public int getNumberFailuresAssertion() {
    return numberFailuresAssertion;
  }

  public void setNumberFailuresAssertion(int numberFailuresAssertion) {
    this.numberFailuresAssertion = numberFailuresAssertion;
    this.put("number_failures_assertion", numberFailuresAssertion);
  }

  public int getNumberAssertion() {
    return numberAssertion;
  }

  public void setNumberAssertion(int numberAssertion) {
    this.numberAssertion = numberAssertion;
    this.put("number_assertion", numberAssertion);
  }

  public double getPercentFailuresAssertion() {
    return percentFailuresAssertion;
  }

  public void setPercentFailuresAssertion(double percentFailuresAssertion) {
    this.percentFailuresAssertion = percentFailuresAssertion;
    this.put("percent_failures_assertion", percentFailuresAssertion);
  }
}
