/**
 * 
 */
package org.ats.services.functional;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.ats.common.MapBuilder;
import org.ats.common.StringUtil;
import org.rythmengine.RythmEngine;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Apr 8, 2015
 */
@SuppressWarnings("serial")
public class Suite extends AbstractTemplate {
  
  private String suiteName;
  
  private String packageName;
  
  private String extraImports;
  
  private String driverVar;
  
  private String initDriver;
  
  private int timeoutSeconds;
  
  private Map<String, Case> cases = new HashMap<String, Case>();

  Suite(String packageName, String extraImports, String suiteName, String driverVar, String initDriver, int timeoutSeconds, Map<String, Case> cases) {
    this.packageName = packageName;
    this.extraImports = extraImports;
    this.suiteName = suiteName;
    this.driverVar = driverVar;
    this.initDriver = initDriver;
    this.timeoutSeconds = timeoutSeconds;
    this.cases = cases;
  }
  
  public String transform() throws IOException {
    String suite = StringUtil.readStream(Thread.currentThread().getContextClassLoader().getResourceAsStream("suite.java.tmpl"));
    StringBuilder sb = new StringBuilder();
    for (Case caze : cases.values()) {
      sb.append(caze.transform());
    }
    
    RythmEngine engine = new RythmEngine(new MapBuilder<String, Boolean>("codegen.compact", false).build());
    return engine.render(suite, packageName, extraImports, suiteName, driverVar, initDriver, timeoutSeconds, sb.toString());
  }
  
  @Override
  public DBObject toJson() {
    BasicDBObject obj = new BasicDBObject();
    obj.put("package_name", packageName);
    obj.put("extra_imports", extraImports);
    obj.put("suite_name", suiteName);
    obj.put("driver_var", driverVar);
    obj.put("init_driver", initDriver);
    obj.put("timeout_seconds", timeoutSeconds);
    BasicDBList list = new BasicDBList();
    for (Case caze : cases.values()) {
      list.add(caze.toJson());
    }
    obj.put("cases", list);
    return obj;
  }
  
  public static class SuiteBuilder {
    
    public static final String DEFAULT_DRIVER_VAR = "FirefoxDriver wd;";
    
    public static final String DEFAULT_INIT_DRIVER = "wd = new FirefoxDriver();";
    
    public static final int DEFAULT_TIMEOUT_SECONDS = 60;
    
    private String suiteName;
    
    private String packageName;
    
    private String extraImports;
    
    private String driverVar;
    
    private String initDriver;
    
    private int timeoutSeconds;
    
    private Map<String, Case> cases = new HashMap<String, Case>();
    
    public SuiteBuilder packageName(String name) {
      this.packageName = name;
      return this;
    }
    
    public SuiteBuilder extraImports(String imports) {
      this.extraImports = imports;
      return this;
    }
    
    public SuiteBuilder driverVar(String driverVar) {
      this.driverVar = driverVar;
      return this;
    }
    
    public SuiteBuilder initDriver(String initDriver) {
      this.initDriver = initDriver;
      return this;
    }
    
    public SuiteBuilder timeoutSeconds(int timeoutSeconds) {
      this.timeoutSeconds = timeoutSeconds;
      return this;
    }
    
    public SuiteBuilder suiteName(String name) {
      this.suiteName = name;
      return this;
    }
    
    public SuiteBuilder addCases(Case...cases) {
      for (Case caze : cases) {
        this.cases.put(caze.getName(), caze);
      }
      return this;
    }
    
    public Suite build() {
      return new Suite(packageName, extraImports, suiteName, driverVar, initDriver, timeoutSeconds, cases);
    }
  }
}
