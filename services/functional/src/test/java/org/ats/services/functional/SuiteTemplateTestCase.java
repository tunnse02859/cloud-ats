/**
 * 
 */
package org.ats.services.functional;

import org.ats.services.functional.Suite.SuiteBuilder;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Apr 8, 2015
 */
public class SuiteTemplateTestCase {

  @Test
  public void test() throws Exception {
    SuiteBuilder builder = new SuiteBuilder();
    builder.packageName("org.ats.generated")
      .suiteName("Test")
      .driverVar(SuiteBuilder.DEFAULT_DRIVER_VAR)
      .initDriver(SuiteBuilder.DEFAULT_INIT_DRIVER)
      .timeoutSeconds(SuiteBuilder.DEFAULT_TIMEOUT_SECONDS);
    try {
      System.out.println(builder.build().transform());
    } catch (Exception e) {
      Assert.fail();
    }
  }
}
