package org.ats.service.report.jmeter;

import java.util.Date;
import java.util.Map;

public class Calculation {
  
  private static final double MILLIS_PER_SECOND = 1000.0;
  
  private static final int KB_TO_BY = 1024;
  
  private static int millisPerBucket = 1000;

  public static void calculateAssertion(Report report, boolean failureValue, boolean errorValue) {
    SummaryReport summary = report.getSummaryReport();
    summary.setNumberAssertion(summary.getNumberAssertion() + 1);
    if (failureValue && errorValue) {
      summary.setNumberFailuresAssertion(summary.getNumberFailuresAssertion() + 1);
    }
    summary.setPercentFailuresAssertion(summary.getNumberFailuresAssertion() * 100.0 / summary.getNumberAssertion());
  }

  public static void calculate(Report report, HttpSamplerObj httpSamplerObj) {

    SummaryReport summary = report.getSummaryReport();
    Map<Long, PointReport> hitsPerSecond = report.getHitsPerSecond();
    Map<Long, PointReport> transPerSecond = report.getTransPerSecond();

    summary.setSamples(summary.getSamples() + 1);
    long timeStamp = httpSamplerObj.getTimeStamp();
    summary.lastTimestamp = Math.max(summary.lastTimestamp, timeStamp);
    summary.firsTimestamp = Math.min(summary.firsTimestamp, timeStamp);

    int time = httpSamplerObj.getElapsedTime();
    summary.totalTimestamp += time;
    summary.setMaxTime(Math.max(summary.maxTime, time));
    summary.setMinTime(Math.min(summary.minTime, time));

    int conn = time - httpSamplerObj.getLatencyTime();
    summary.connectionTime += conn;
    summary.maxConnectionTime = Math.max(summary.maxConnectionTime, conn);
    summary.minConnectionTime = Math.max(summary.minConnectionTime, conn);

    summary.lastTimeFinal = Math.max(summary.lastTimeFinal, timeStamp + time);
    summary.totalBytes += httpSamplerObj.getBytes();

    // His per second
    {
      Long bucketHis = new Long(timeStamp / millisPerBucket * millisPerBucket);
      PointReport point = hitsPerSecond.get(bucketHis);
      if (point == null) {
        point = new PointReport();
        point.setTimestamp(bucketHis);
        point.setDate(new Date(bucketHis.longValue()));
        point.setValue(0);
      }
      point.setValue(point.getValue() + 1);
      hitsPerSecond.put(bucketHis, point);
    }

    // tran per second
    {
      long responeTime = timeStamp + time;
      Long bucketTrans = new Long(responeTime / millisPerBucket * millisPerBucket);
      PointReport point = transPerSecond.get(bucketTrans);
      if (point == null) {
        point = new PointReport();
        point.setTimestamp(bucketTrans);
        point.setDate(new Date(bucketTrans.longValue()));
        point.setValue(0);
      }
      point.setValue(point.getValue() + 1);
      transPerSecond.put(bucketTrans, point);
    }

    // standard deviation

    double prev_avg = summary.average;
    summary.setAverage((double) summary.totalTimestamp / summary.samples);

    summary.setStandardDeviation(calculateStDev(summary.samples - 1, time, prev_avg, summary.standardDeviation, summary.average));

    // count false
    if (!httpSamplerObj.getSuccessFlag().equalsIgnoreCase("true")) {
      summary.setErrorPercent(summary.failures / summary.samples);
    }
    // error pecent
    summary.setErrorPercent(summary.failures * 100.0 / summary.samples);

    // throughtput
    summary.setThroughtput(summary.samples * MILLIS_PER_SECOND / (summary.lastTimeFinal - summary.firsTimestamp));

    // kb per second
    summary.setKbPerSecond(summary.totalBytes * MILLIS_PER_SECOND / KB_TO_BY / (summary.lastTimeFinal - summary.firsTimestamp));

    // averageBytes
    summary.setAverageBytes((double)summary.totalBytes / summary.samples);
  }

  private static double calculateStDev(int number, int elementN1, double prev_avg, double pre_std_dev, double current_avg) {
    double delta = current_avg - prev_avg;
    double variance = (number * delta * delta + (elementN1 - current_avg) * (elementN1 - current_avg) + number * pre_std_dev * pre_std_dev) / (number + 1);
    return Math.sqrt(variance);
  }
}
