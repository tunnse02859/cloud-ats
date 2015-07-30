package org.ats.jmeter.report;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class Summary {
  private int _millisPerBucket;

  private static final String DECIMAL_PATTERN = "#,##0.0##";
  private static final double MILLIS_PER_SECOND = 1000.0;
  private static final int KB_TO_BY = 1024;
  public String lable = "";
  public int count = 0;
  public long total_t = 0;
  public int max_t = 0;
  public int min_t = Integer.MAX_VALUE;
  public int total_conn = 0;
  public int max_conn = 0;
  public int min_conn = Integer.MAX_VALUE;
  public int failures = 0;
  public long first_ts = Long.MAX_VALUE;
  public long last_time_fn = 0;
  public long last_ts = 0;
  public double std_dev = 0;
  public double avg = 0;
  public long total_by = 0;
  public double throughtput = 0;
  public Map<Long, Integer> hitsPersecond = new TreeMap<Long, Integer>();
  public Map<Long, Integer> transPersecond = new TreeMap<Long, Integer>();

  public Summary() {
  }

  public Summary(int _millisPerBucket, String lable) {
    this._millisPerBucket = _millisPerBucket;
    this.lable = lable;
  }

  public Report toJsonNode() {
    
    ObjectMapper objectMapper = new ObjectMapper();
    Report objNode = new Report();
    DecimalFormat df2 = new DecimalFormat(DECIMAL_PATTERN);
    
    objNode.put("lable", this.lable);
    objNode.put("Samples", count);   
    objNode.put("Average", df2.format((double)total_t / count));
    objNode.put("Min", this.min_t);
    objNode.put("Max", this.max_t);
    objNode.put("Std.Dev", df2.format(this.std_dev));
    objNode.put("Error%", df2.format((double) this.failures * 100 / this.count));
    objNode.put("Throughput",  df2.format(count*MILLIS_PER_SECOND / (last_time_fn - first_ts) ));
    objNode.put("KB/sec", df2.format(total_by*MILLIS_PER_SECOND/KB_TO_BY/ (last_time_fn - first_ts)));
    objNode.put("Avg.Bytes", df2.format((double) total_by/count));

    ObjectNode objHits = objectMapper.createObjectNode();
    Iterator<Map.Entry<Long, Integer>> hitsIterator = hitsPersecond.entrySet().iterator();
    while (hitsIterator.hasNext()) {
      Map.Entry<Long, Integer> entry = hitsIterator.next();
      long minMillis = entry.getKey() * _millisPerBucket;
      Date date = new Date(minMillis);
      String strDate = new SimpleDateFormat("YYYYMMdd HH:mm:ss").format(date);
      objHits.put(strDate, entry.getValue());
    }
    objNode.put("hitsPersecond", objHits);

    ObjectNode objTrans = objectMapper.createObjectNode();
    Iterator<Map.Entry<Long, Integer>> transIterator = hitsPersecond.entrySet().iterator();
    while (transIterator.hasNext()) {
      Map.Entry<Long, Integer> entry = transIterator.next();
      long minMillis = entry.getKey() * _millisPerBucket;
      Date date = new Date(minMillis);
      String strDate = new SimpleDateFormat("YYYYMMdd HH:mm:ss").format(date);
      objTrans.put(strDate, entry.getValue());
    }
    objNode.put("transPersecond", objTrans);

    return objNode;

  }

}
