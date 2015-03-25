/**
 * 
 */
package org.ats.services.event;

import com.google.inject.assistedinject.Assisted;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Mar 23, 2015
 */
public interface EventFactory {

  public Event create(@Assisted("source") Object source, @Assisted("eventName") String eventName);
}
