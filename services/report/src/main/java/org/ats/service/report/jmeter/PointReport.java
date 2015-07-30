package org.ats.service.report.jmeter;

import java.util.Date;

import com.mongodb.BasicDBObject;

@SuppressWarnings("serial")
public class PointReport extends BasicDBObject{

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
    this.put("timestamp", timestamp);
  }
  public void setDate(Date date) {
    this.put("date", date);
  }
  public void setValue(int value) {
    this.put("value",value);
  }
}
