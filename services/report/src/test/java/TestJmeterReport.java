import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.ats.common.PageList;
import org.ats.jmeter.ReportModule;
import org.ats.jmeter.report.Report;
import org.ats.jmeter.report.ReportJmeterFactory;
import org.ats.jmeter.report.ReportService;
import org.ats.services.DataDrivenModule;
import org.ats.services.ExecutorModule;
import org.ats.services.GeneratorModule;
import org.ats.services.KeywordServiceModule;
import org.ats.services.OrganizationServiceModule;
import org.ats.services.PerformanceServiceModule;
import org.ats.services.VMachineServiceModule;
import org.ats.services.data.DatabaseModule;
import org.ats.services.event.EventModule;
import org.ats.services.executor.ExecutorService;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class TestJmeterReport {
  private Injector injector;
  private ExecutorService executorService;  
  private ReportJmeterFactory reportJmeterFactory;
  private ReportService reportService;

  @BeforeClass
  public void init() throws FileNotFoundException, IOException {
    this.injector = Guice.createInjector(
        new DatabaseModule(),
        new EventModule(), 
        new OrganizationServiceModule(),
        new DataDrivenModule(),
        new KeywordServiceModule(),
        new PerformanceServiceModule(),
        new GeneratorModule(),
        new VMachineServiceModule("src/test/resources/iaas.conf"),
        new ExecutorModule(), 
        new ReportModule());
    this.executorService = injector.getInstance(ExecutorService.class);
    this.reportJmeterFactory = injector.getInstance(ReportJmeterFactory.class);
    this.reportService = injector.getInstance(ReportService.class);
  }

  @Test
  public void testParseJtlContent() throws SAXException, IOException, ParserConfigurationException {    
    PageList<Report> list = reportService.getList("97d9d449", "performance");
    System.out.println("LIST:" + list.count());
    Assert.assertTrue(list.count() > 0);   
       
  }

}
