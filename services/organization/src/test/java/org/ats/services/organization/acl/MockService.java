/**
 * 
 */
package org.ats.services.organization.acl;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Mar 16, 2015
 */
@Authenticated
public class MockService {

  @Authorized(tenant = "fsoft", feature = "fooFeature", action = "fooAction")
  public String foo() {
    return "foo";
  }
  
  @Authorized(tenant = "fsoft", feature = "barFeature", action = "barAction")
  public String bar() {
    return "bar";
  }
  
  @Authorized
  public String publicMethod() {
    return "public";
  }
  
  @Authorized(tenant = "viettel", feature = "barFeature", action = "barAction")
  public String viettel() {
    return "viettel";
  }
}
