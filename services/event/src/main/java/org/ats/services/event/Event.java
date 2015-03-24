/**
 * 
 */
package org.ats.services.event;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Mar 23, 2015
 */
public class Event<T> {

  /** .*/
  protected T source;
  
  /** .*/
  private EventService service;
  
  /** .*/
  private String eventName;
  
  @Inject
  Event(EventService service, @Assisted("source") T source, @Assisted("eventName") String eventName) {
    this.source = source;
    this.service = service;
    this.eventName = eventName;
  }
  
  public void broadcast() {
    this.service.process(this);
  }
  
  public T getSource() {
    return source;
  }
  
  public String getName() {
    return eventName;
  }
}
