/**
 * 
 */
package org.ats.services.event;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryModuleBuilder;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Mar 23, 2015
 */
public class MockModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(MockService.class);
    bind(EventService.class);
    install(new FactoryModuleBuilder().build(new TypeLiteral<EventFactory<String>>(){}));
  }

}
