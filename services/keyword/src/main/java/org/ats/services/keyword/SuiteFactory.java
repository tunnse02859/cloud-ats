/**
 * 
 */
package org.ats.services.keyword;

import java.util.List;

import com.google.inject.assistedinject.Assisted;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Aug 17, 2015
 */
public interface SuiteFactory {
  
  public static final String DEFAULT_INIT_DRIVER = "wd = new FirefoxDriver();";
  public static final String DEFAULT_INIT_VERSION_SELENIUM = "2.48.2";
  
  public Suite create(@Assisted("projectId") String projectId, 
      @Assisted("suiteName") String suiteName, 
      @Assisted("initDriver") String initDriver,
      @Assisted("versionSelenium") String versionSelenium,
      @Assisted("cases") List<CaseReference> cases);
}
