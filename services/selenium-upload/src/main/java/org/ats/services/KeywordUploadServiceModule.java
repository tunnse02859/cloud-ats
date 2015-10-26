/**
 * 
 */
package org.ats.services;

import org.ats.services.upload.SeleniumUploadProjectFactory;
import org.ats.services.upload.SeleniumUploadProjectService;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

/**
 * @author NamBV2
 *
 * Sep 17, 2015
 */
public class KeywordUploadServiceModule extends AbstractModule{

  @Override
  protected void configure() {
    bind(SeleniumUploadProjectService.class);
    
    install(new FactoryModuleBuilder().build(SeleniumUploadProjectFactory.class));
  }

}
