/**
 * 
 */
package org.ats.services;

import org.ats.services.functional.ActionFactory;

import com.google.inject.AbstractModule;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Apr 8, 2015
 */
public class FunctionalModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(ActionFactory.class);
  }

}
