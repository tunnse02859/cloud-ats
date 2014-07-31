/**
 * 
 */
package org.ats.component.usersmgt;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Jul 7, 2014
 */
public interface EventListener  {

  public void execute(Event event) throws EventExecutedException; 
  
}
