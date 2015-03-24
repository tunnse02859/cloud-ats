/**
 * 
 */
package org.ats.services.event;

import com.google.inject.Inject;


/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Mar 23, 2015
 */
public class MockService {

  @Inject
  MockService() {
  }

  public String foo() {
    return "foo";
  }
}
