/**
 * 
 */
package org.ats.services.organization.acl;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Mar 17, 2015
 */
@SuppressWarnings("serial")
public class UnAuthenticatedException extends RuntimeException {

  public UnAuthenticatedException(String msg) {
    super(msg);
  }

}
