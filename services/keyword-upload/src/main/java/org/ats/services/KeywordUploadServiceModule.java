/**
 * 
 */
package org.ats.services;

import org.ats.services.upload.KeywordUploadProjectFactory;
import org.ats.services.upload.KeywordUploadProjectService;

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
    bind(KeywordUploadProjectService.class);
    
    install(new FactoryModuleBuilder().build(KeywordUploadProjectFactory.class));
  }

}
