/**
 * 
 */
package org.ats.services.keyword;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.ats.common.MapBuilder;
import org.ats.common.StringUtil;
import org.ats.services.datadriven.DataDriven;
import org.ats.services.datadriven.DataDrivenReference;
import org.rythmengine.RythmEngine;

import com.mongodb.BasicDBList;
import com.mongodb.DBObject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Apr 8, 2015
 */
@SuppressWarnings("serial")
public class Suite extends AbstractTemplate {
  
  private Map<String, CaseReference> cases = new HashMap<String, CaseReference>();
  
  private List<DataDrivenReference> dataDrivens = new ArrayList<DataDrivenReference>();
  
  Suite(String packageName, String extraImports, String suiteName, String driverVar, String initDriver, int timeoutSeconds, Map<String, CaseReference> cases , List<DataDrivenReference> dataDrivens, DBObject raw) { 
    this.put("_id", UUID.randomUUID().toString());
    this.put("package_name", packageName);
    this.put("extra_imports", extraImports);
    this.put("suite_name", suiteName);
    this.put("driver_var", driverVar);
    this.put("init_driver", initDriver);
    this.put("timeout_seconds", timeoutSeconds);
    
    this.cases = cases;
    BasicDBList list = new BasicDBList();
    for (CaseReference caze : cases.values()) {
      list.add(caze.toJSon());
    }
    this.put("cases", list);
    
    this.dataDrivens = dataDrivens;
    
    this.put("raw", raw);
  }
  
  public String getId() {
    return this.getString("_id");
  }
  
  public void removeCase(CaseReference caseRef) {
    this.cases.remove(caseRef.getId());
    BasicDBList list = new BasicDBList();
    for (CaseReference caze : cases.values()) {
      list.add(caze);
    }
    this.put("cases", list);
  }

  public Collection<CaseReference> getCases() {
    return Collections.unmodifiableCollection(cases.values());
  }
  
  public String transform() throws IOException {
    String suite = StringUtil.readStream(Thread.currentThread().getContextClassLoader().getResourceAsStream("suite.java.tmpl"));
    StringBuilder sbCase = new StringBuilder();
    StringBuilder sbDataDriven = new StringBuilder();
    
    for (CaseReference caze : cases.values()) {
      sbCase.append(caze.get().transform());
    }
    for(DataDrivenReference ref : dataDrivens) {
      DataDriven data = ref.get();
      sbDataDriven.append(data.transform());
    }
    RythmEngine engine = new RythmEngine(new MapBuilder<String, Boolean>("codegen.compact", false).build());

    return engine.render(suite, this.get("package_name"), 
        this.get("extra_imports"), 
        this.get("suite_name"), 
        this.get("driver_var"), 
        this.get("init_driver"), 
        this.get("timeout_seconds"), 
        sbCase.toString(),
        sbDataDriven.toString());
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
    
    private Map<String, CaseReference> cases = new HashMap<String, CaseReference>();
    
    private List<DataDrivenReference> dataDrivens = new ArrayList<DataDrivenReference>();

    private DBObject raw;
    
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
    
    public SuiteBuilder addCases(CaseReference...cases) {
      for (CaseReference caze : cases) {
        this.cases.put(caze.getId(), caze);
      }
      return this;
    }
    
    public SuiteBuilder addDataDrivens(DataDrivenReference... refs) {
      for(DataDrivenReference ref : refs) {
        this.dataDrivens.add(ref);
      }
      return this;
    }

    public SuiteBuilder raw(DBObject raw) {
      this.raw = raw;
      return this;
    }
    
    public Suite build() {
      return new Suite(packageName, extraImports, suiteName, driverVar, initDriver, timeoutSeconds, cases, dataDrivens, raw);
    }
    
  }
}
