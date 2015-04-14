/**
 * 
 */
package org.ats.services.functional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.ats.common.StringUtil;
import org.rythmengine.Rythm;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Apr 8, 2015
 */
public class Suite implements ITemplate {
  
  private String suiteName;
  
  private String packageName;
  
  private String extraImports;
  
  private String driverVar;
  
  private String initDriver;
  
  private List<Case> cases = new ArrayList<Case>();

  Suite(String packageName, String extraImports, String suiteName, String driverVar, String initDriver, List<Case> cases) {
    this.packageName = packageName;
    this.extraImports = extraImports;
    this.suiteName = suiteName;
    this.driverVar = driverVar;
    this.initDriver = initDriver;
    this.cases = cases;
  }
  
  public String transform() throws IOException {
    String suite = StringUtil.readStream(Thread.currentThread().getContextClassLoader().getResourceAsStream("suite.java.tmpl"));
    StringBuilder sb = new StringBuilder();
    for (Case caze : cases) {
      sb.append(caze.transform());
    }
    return Rythm.render(suite, packageName, extraImports, suiteName, driverVar, initDriver, sb.toString());
  }
  
  public static class SuiteBuilder {
    
    public static final String DEFAULT_DRIVER_VAR = "FirefoxDriver wd;";
    
    public static final String DEFAULT_INIT_DRIVER = "wd = new FirefoxDriver();";  
    
    private String suiteName;
    
    private String packageName;
    
    private String extraImports;
    
    private String driverVar;
    
    private String initDriver;
    
    private List<Case> cases = new ArrayList<Case>();
    
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
    
    public SuiteBuilder suiteName(String name) {
      this.suiteName = name;
      return this;
    }
    
    public SuiteBuilder addCases(Case...cases) {
      for (Case caze : cases) {
        this.cases.add(caze);
      }
      return this;
    }
    
    public Suite build() {
      return new Suite(packageName, extraImports, suiteName, driverVar, initDriver, cases);
    }
  }
}
