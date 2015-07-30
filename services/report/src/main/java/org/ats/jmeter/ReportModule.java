package org.ats.jmeter;

import org.ats.jmeter.report.ReportJmeterFactory;
import org.ats.jmeter.report.ReportService;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public class ReportModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(ReportService.class);    
    install(new FactoryModuleBuilder().build(ReportJmeterFactory.class));
  }
  

}
