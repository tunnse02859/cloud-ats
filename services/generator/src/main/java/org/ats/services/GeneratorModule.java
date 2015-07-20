/**
 * 
 */
package org.ats.services;

import org.ats.services.generator.GeneratorService;

import com.google.inject.AbstractModule;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Jul 14, 2015
 */
public class GeneratorModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(GeneratorService.class);
  }

}
