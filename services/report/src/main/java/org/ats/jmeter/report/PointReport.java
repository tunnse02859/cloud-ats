package org.ats.jmeter.report;

import java.util.Date;

import com.mongodb.BasicDBObject;

public class PointReport extends BasicDBObject{
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private long timestamp;
  private Date date;
  private int value;
  
  public PointReport() {
   
  }
  
  public long getTimestamp(){
    return this.getLong("timestamp");
  }
  public Date getDate(){
    return this.getDate("date");
  }
  public int getValue(){    
    return this.getInt("value");
  }
  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
    this.put("timestamp", timestamp);
  }
  public void setDate(Date date) {
    this.date = date;
    this.put("date", date);
  }
  public void setValue(int value) {
    this.value = value;
    this.put("value",value);
  }
  
  
  
  

}
