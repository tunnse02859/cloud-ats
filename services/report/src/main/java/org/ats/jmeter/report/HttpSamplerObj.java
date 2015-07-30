package org.ats.jmeter.report;

import org.xml.sax.Attributes;

public class HttpSamplerObj {
 

  /**
  by  Bytes
  de  Data encoding
  dt  Data type
  ec  Error count (0 or 1, unless multiple samples are aggregated)
  hn  Hostname where the sample was generated
  it  Idle Time = time not spent sampling (milliseconds) (generally 0)
  lb  Label
  lt  Latency = time to initial response (milliseconds) - not all samplers support this
  ct  Connect Time = time to establish the connection (milliseconds) - not all samplers support this
  na  Number of active threads for all thread groups
  ng  Number of active threads in this group
  rc  Response Code (e.g. 200)
  rm  Response Message (e.g. OK)
  s Success flag (true/false)
  sc  Sample count (1, unless multiple samples are aggregated)
  t Elapsed time (milliseconds)
  tn  Thread Name
  ts  timeStamp (milliseconds since midnight Jan 1, 1970 UTC)
  **/
   
  
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
  
  private  int bytes = 0;
  private  int sampleCount = 0;
  private  int errorCount = 0;
  private  int numberActiveGroup = 0;
  private  int numberActiveAllGroup = 0;
 
  public HttpSamplerObj() {
    
  }
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
