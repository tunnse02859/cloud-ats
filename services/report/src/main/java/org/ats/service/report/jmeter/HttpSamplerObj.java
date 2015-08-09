package org.ats.service.report.jmeter;

import java.util.Map;

import org.xml.sax.Attributes;

/**
 * 
 * <strong>de</strong>  Data Encoding
 * <strong>dt</strong>  Data type
 * <strong>ec</strong>  Error count (0 or 1, unless multiple samples are aggregated)
 * <strong>hn</strong>  Hostname where the sample was generated
 * <strong>it</strong>   Idle Time = time not spent sampling (milliseconds) (generally 0)
 * <strong>lb</strong>   Label
 * <strong>lt</strong>   Latency = time to initial response (milliseconds) - not all samplers support this
 * <strong>ct</strong>  Connect Time = time to establish the connection (milliseconds) - not all samplers support this
 * <strong>na</strong>  Number of active threads for all thread groups
 * <strong>ng</strong>  Number of active threads in this group
 * <strong>rc</strong>  Response Code (e.g. 200)
 * <strong>rm</strong> Response Message (e.g. OK)
 * <strong>s</strong>   Success flag (true/false)
 * <strong>sc</strong> Sample count (1, unless multiple samples are aggregated)
 * <strong>t</strong>   Elapsed time (milliseconds)
 * <strong>tn</strong> Thread Name
 * <strong>ts</strong> timeStamp (milliseconds since midnight Jan 1, 1970 UTC)
 * 
 * 
 */
public class HttpSamplerObj {
 
  private  int elapsedTime =  0;
  private  int latencyTime = 0; 
  private  long timeStamp = 0; 
  
  private  String successFlag =  ""; 
  private  String lable = "";
  private  String reponseCode = "";
  private  String reponseMessage = "";
  private  String threadName = "";
  private  String dataType = "";
  private  String dataEncoding = "";
  private  String failureMessage = null;
  
  private  int bytes = 0;
  private  int sampleCount = 0;
  private  int errorCount = 0;
  private  int numberActiveGroup = 0;
  private  int numberActiveAllGroup = 0;
  
  
  
  
  
 
  public HttpSamplerObj(Attributes attributes) {
    this.elapsedTime = Integer.parseInt(attributes.getValue("t"));
    this.latencyTime = Integer.parseInt(attributes.getValue("lt"));   
    this.timeStamp = Long.parseLong(attributes.getValue("ts"));
    
    this.bytes = Integer.parseInt(attributes.getValue("by"));
    this.sampleCount = Integer.parseInt(attributes.getValue("sc"));
    this.errorCount = Integer.parseInt(attributes.getValue("ec"));
    this.numberActiveGroup = Integer.parseInt(attributes.getValue("ng"));
    this.numberActiveAllGroup = Integer.parseInt(attributes.getValue("na"));
    
    this.reponseCode = attributes.getValue("rc");
    this.successFlag = attributes.getValue("s");
    this.lable = attributes.getValue("lb");
    this.reponseMessage = attributes.getValue("rm");
    this.threadName = attributes.getValue("tn");
    this.dataType = attributes.getValue("dt");
    this.dataEncoding = attributes.getValue("de");
  }
  
  
  public HttpSamplerObj(Map<String,String> dataReport){  
    
    this.elapsedTime = Integer.parseInt(dataReport.get("elapsed"));
    this.latencyTime = Integer.parseInt(dataReport.get("Latency"));   
    this.timeStamp = Long.parseLong(dataReport.get("timeStamp"));
    
    this.bytes = Integer.parseInt(dataReport.get("bytes"));
    this.sampleCount = Integer.parseInt(dataReport.get("SampleCount"));
    this.errorCount = Integer.parseInt(dataReport.get("ErrorCount"));
    this.numberActiveGroup = Integer.parseInt(dataReport.get("grpThreads"));
    this.numberActiveAllGroup = Integer.parseInt(dataReport.get("allThreads"));
    
    this.reponseCode = dataReport.get("responseCode");
    this.successFlag = dataReport.get("success");
    this.lable = dataReport.get("label");
    this.reponseMessage = dataReport.get("responseMessage");
    this.threadName = dataReport.get("threadName");
    this.dataType = dataReport.get("dataType");
    this.dataEncoding = dataReport.get("Encoding");
    
    //fix for parse csv file 
    
    this.failureMessage = dataReport.get("failureMessage");
  }
  
  public String getFailureMessage() {
    return failureMessage;
  }


  public void setFailureMessage(String failureMessage) {
    this.failureMessage = failureMessage;
  }


  public int getElapsedTime() {
    return elapsedTime;
  }
  
  public void setElapsedTime(int elapsedTime) {
    this.elapsedTime = elapsedTime;
  }
  
  public int getLatencyTime() {
    return latencyTime;
  }
  
  public void setLatencyTime(int latencyTime) {
    this.latencyTime = latencyTime;
  }
  
  public long getTimeStamp() {
    return timeStamp;
  }
  
  public void setTimeStamp(long timeStamp) {
    this.timeStamp = timeStamp;
  }
  
  public String getSuccessFlag() {
    return successFlag;
  }
  
  public void setSuccessFlag(String successFlag) {
    this.successFlag = successFlag;
  }
  
  public String getLable() {
    return lable;
  }
  
  public void setLable(String lable) {
    this.lable = lable;
  }
  
  public String getReponseCode() {
    return reponseCode;
  }
  
  public void setReponseCode(String reponseCode) {
    this.reponseCode = reponseCode;
  }
  
  public String getReponseMessage() {
    return reponseMessage;
  }
  
  public void setReponseMessage(String reponseMessage) {
    this.reponseMessage = reponseMessage;
  }
  
  public String getThreadName() {
    return threadName;
  }
  
  public void setThreadName(String threadName) {
    this.threadName = threadName;
  }
  
  public String getDataType() {
    return dataType;
  }
  
  public void setDataType(String dataType) {
    this.dataType = dataType;
  }
  
  public String getDataEncoding() {
    return dataEncoding;
  }
  
  public void setDataEncoding(String dataEncoding) {
    this.dataEncoding = dataEncoding;
  }
  
  public int getBytes() {
    return bytes;
  }
  
  public void setBytes(int bytes) {
    this.bytes = bytes;
  }
  
  public int getSampleCount() {
    return sampleCount;
  }
  
  public void setSampleCount(int sampleCount) {
    this.sampleCount = sampleCount;
  }
  
  public int getErrorCount() {
    return errorCount;
  }
  
  public void setErrorCount(int errorCount) {
    this.errorCount = errorCount;
  }
  
  public int getNumberActiveGroup() {
    return numberActiveGroup;
  }
  
  public void setNumberActiveGroup(int numberActiveGroup) {
    this.numberActiveGroup = numberActiveGroup;
  }
  
  public int getNumberActiveAllGroup() {
    return numberActiveAllGroup;
  }
  
  public void setNumberActiveAllGroup(int numberActiveAllGroup) {
    this.numberActiveAllGroup = numberActiveAllGroup;
  }
}
