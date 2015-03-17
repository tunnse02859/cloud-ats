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
public class UnAuthorizationException extends RuntimeException {

  public UnAuthorizationException(String msg) {
    super(msg);
  }
}
