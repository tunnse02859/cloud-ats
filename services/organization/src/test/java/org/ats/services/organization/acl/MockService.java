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
  
  @Authorized(tenant = "viettel", feature = "featureViettel", action = "barAction")
  public String viettel() {
    return "viettel";
  }
  
  @Authorized(tenant = "viettel", feature = "featureViettel1", action = "barAction")
  public String viettel1() {
    return "viettel1";
  }
  
  @Authorized(tenant = "viettel", feature = "featureViettel", action = "fooAction")
  public String viettel2() {
    return "viettel2";
  }
  
  @Authorized(tenant = "viettel", feature = "featureViettel", action = "actionViettel")
  public String viettel3() {
    return "viettel3";
  }
  
  @Authorized(feature = "featureViettel", action = "actionViettel")
  public String viettelDefaultTenant() {
    return "viettelDefault";
  }
  
  @Authorized(tenant = "viettel", feature = "featureViettel")
  public String viettelDefaultAction() {
    return "DefaultAction";
  }
  
  @Authorized(tenant = "viettel", action = "actionViettel")
  public String viettelDefaultFeature() {
    return "DefaultFeature";
  }
  
  @Authorized(tenant = "viettel", feature = "featureViettel", action = "barAction")
  public String defaultspace() {
    return "space";
  }
  
}






