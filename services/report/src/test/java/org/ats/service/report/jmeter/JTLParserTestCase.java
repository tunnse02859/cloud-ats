/**
 * 
 */
package org.ats.service.report.jmeter;

import java.io.FileInputStream;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.ats.common.PageList;
import org.ats.common.StringUtil;
import org.ats.service.ReportModule;
import org.ats.service.report.Report;
import org.ats.service.report.ReportService;
import org.ats.service.report.ReportService.Type;
import org.ats.services.DataDrivenModule;
import org.ats.services.ExecutorModule;
import org.ats.services.GeneratorModule;
import org.ats.services.KeywordServiceModule;
import org.ats.services.OrganizationServiceModule;
import org.ats.services.PerformanceServiceModule;
import org.ats.services.VMachineServiceModule;
import org.ats.services.data.DatabaseModule;
import org.ats.services.data.MongoDBService;
import org.ats.services.event.EventModule;
import org.ats.services.event.EventService;
import org.ats.services.executor.ExecutorService;
import org.ats.services.executor.job.AbstractJob;
import org.ats.services.executor.job.PerformanceJob;
import org.ats.services.executor.job.PerformanceJobFactory;
import org.ats.services.iaas.openstack.OpenStackService;
import org.ats.services.organization.base.AuthenticationService;
import org.ats.services.organization.entity.User;
import org.ats.services.organization.entity.fatory.ReferenceFactory;
import org.ats.services.organization.event.AbstractEventTestCase;
import org.ats.services.performance.JMeterScriptReference;
import org.ats.services.performance.JMeterScriptService;
import org.ats.services.performance.PerformanceProjectFactory;
import org.ats.services.performance.PerformanceProjectService;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import com.google.inject.Guice;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Aug 13, 2015
 */
public class JTLParserTestCase extends AbstractEventTestCase {
  
  private PerformanceProjectService perfService;
  private PerformanceProjectFactory perfFactory;
  private PerformanceJobFactory perfJobFactory;
  private JMeterScriptService jmeterService;
  private ReferenceFactory<JMeterScriptReference> jmeterScriptRef;
  private ExecutorService executorService;
  private OpenStackService openstackService;
  private ReportService reportService;
  
  @BeforeClass
  public void init() throws Exception {
    System.setProperty(EventModule.EVENT_CONF, "src/test/resources/event.conf");

    this.injector = Guice.createInjector(new DatabaseModule(), new EventModule(), new OrganizationServiceModule(), new DataDrivenModule(),
        new KeywordServiceModule(), new PerformanceServiceModule(), new GeneratorModule(), new VMachineServiceModule("src/test/resources/iaas.conf"),
        new ExecutorModule(), new ReportModule());

    this.mongoService = injector.getInstance(MongoDBService.class);
    this.mongoService.dropDatabase();

    // performance
    this.perfFactory = injector.getInstance(PerformanceProjectFactory.class);
    this.perfJobFactory = injector.getInstance(PerformanceJobFactory.class);
    this.perfService = injector.getInstance(PerformanceProjectService.class);
    this.jmeterScriptRef = this.injector.getInstance(Key.get(new TypeLiteral<ReferenceFactory<JMeterScriptReference>>() {
    }));
    this.jmeterService = this.injector.getInstance(JMeterScriptService.class);

    this.executorService = injector.getInstance(ExecutorService.class);
    this.openstackService = injector.getInstance(OpenStackService.class);
    this.reportService = injector.getInstance(ReportService.class);

    // start event service
    this.eventService = injector.getInstance(EventService.class);
    this.eventService.setInjector(injector);
    this.eventService.start();

    initService();
  }
  
  @AfterClass
  public void shutdown() throws Exception {
    this.mongoService.dropDatabase();
  }
  
  @Test
  public void testParse() throws Exception {
    String projectId = UUID.randomUUID().toString();
    String projectHash = projectId.substring(0, 8) + "-" + UUID.randomUUID().toString().substring(0, 8);
    PerformanceJob job = perfJobFactory.create(projectHash, projectId, Collections.<JMeterScriptReference>emptyList(), "fake", AbstractJob.Status.Completed);
    String content = StringUtil.readStream(new FileInputStream("src/test/resources/result.jtl"));
    BasicDBList list = new BasicDBList();
    list.add(new BasicDBObject("_id", "fake").append("content", content));
    job.put("report", list);
    executorService.create(job);
    PageList<Report> pages = reportService.getList(projectHash, Type.PERFORMANCE, "fake");
    Assert.assertEquals(pages.count(), 3);
    List<Report> reports = pages.next();
    for (Report report : reports) {
      if ("Homepage".equals(report.getLabel())) {
        SummaryReport summary = report.getSummaryReport();
        Assert.assertEquals(summary.getSamples(), 200);
        Assert.assertEquals((int)summary.getAverage(), 18819);
        Assert.assertEquals(summary.getMinTime(), 7306);
        Assert.assertEquals(summary.getMaxTime(), 29733);
        Assert.assertEquals(round(summary.getStandardDeviation(), 2), round(3745.40, 2));
        Assert.assertEquals(round(summary.getErrorPercent(), 2), round(100, 2));
        Assert.assertEquals(round(summary.getThroughtput(),1), 6.2);
        Assert.assertEquals(round(summary.getKbPerSecond(), 2), 224.78);
        Assert.assertEquals(round(summary.getAverageBytes(), 1), round(37363.0, 1));
      }
    }
  }
  
  private double round(double value, int places) {
    if (places < 0) throw new IllegalArgumentException();

    long factor = (long) Math.pow(10, places);
    value = value * factor;
    long tmp = Math.round(value);
    return (double) tmp / factor;
  }
}
