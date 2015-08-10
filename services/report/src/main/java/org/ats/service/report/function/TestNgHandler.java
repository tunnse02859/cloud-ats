package org.ats.service.report.function;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.ats.services.executor.ExecutorService;
import org.ats.services.executor.job.KeywordJob;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

public class TestNgHandler extends DefaultHandler {

  private String parsingType = "STRING";
  private String content = "";
  private ExecutorService executorService;
  private Map<String, SuiteReport> listSuiteReport = new TreeMap<String, SuiteReport>();

  public Map<String, SuiteReport> getListSuiteReport() {
    return listSuiteReport;
  }

  public void setListSuiteReport(Map<String, SuiteReport> listSuiteReport) {
    this.listSuiteReport = listSuiteReport;
  }

  String tmpValue;
  boolean inclass = false;
  boolean isFirstMethod =false;
  SuiteReport tmpSuiteReport;

  @Inject
  public TestNgHandler(ExecutorService executorService, @Assisted("functionalJobId") String functionalJobId) {
    this.executorService = executorService;
    KeywordJob job = (KeywordJob) this.executorService.get(functionalJobId);
    Iterator<Map.Entry<String, String>> iterator = job.getRawDataOutput().entrySet().iterator();
    while (iterator.hasNext()) {
      Map.Entry<String, String> entry = iterator.next();
      this.content = entry.getValue();
      break;
    }
  }

  public void startParsing() throws SAXException, IOException, ParserConfigurationException {
    if (content.isEmpty()) {
      throw new ParserConfigurationException("Error content parsing is empty");
    }
    if ("STRING".equalsIgnoreCase(parsingType)) {
      SAXParserFactory factory = SAXParserFactory.newInstance();
      SAXParser saxParser = factory.newSAXParser();
      InputStream ins = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
      saxParser.parse(ins, this);
      try {
        ins.close();
      } catch (Exception e) {
        throw e;
      }
    }
  }

  @Override
  public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        
    if ("class".equalsIgnoreCase(qName)) {
      inclass = true;
      isFirstMethod = true;
      
      tmpSuiteReport = new SuiteReport();
      tmpSuiteReport.setTestResult(true);
      
      String name = attributes.getValue("name");
      if (name == null || name.isEmpty()) {
        name = "testSuite";
      }
      if (name.contains(".")) {
        name = name.substring(name.lastIndexOf(".") + 1);
      }
      tmpSuiteReport.setName(name);
      

    }
    if ("test-method".equalsIgnoreCase(qName)) {
      if (inclass) {
        if(isFirstMethod){
          try {
            tmpSuiteReport.setRunningTime(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX").parse(attributes.getValue("started-at")));
          } catch (ParseException e) {
            System.out.println(e.toString());
          }
          isFirstMethod = false;
        }
        
        tmpSuiteReport.setTotalTestCase(tmpSuiteReport.getTotalTestCase() + 1);
        if ("PASS".equalsIgnoreCase(attributes.getValue("status"))) {

          tmpSuiteReport.setTotalPass(tmpSuiteReport.getTotalPass() + 1);

        } else if ("FAIL".equalsIgnoreCase(attributes.getValue("status"))) {
          if (tmpSuiteReport.isTestResult()) {
            tmpSuiteReport.setTestResult(false);
          }
          tmpSuiteReport.setTotalFail(tmpSuiteReport.getTotalFail() + 1);
          tmpSuiteReport.setTestResult(false);
        } else if ("SKIP".equalsIgnoreCase(attributes.getValue("status"))) {

          tmpSuiteReport.setTotalSkip(tmpSuiteReport.getTotalSkip() + 1);

        } else {

        }
      }
    }

  }

  @Override
  public void endElement(String uri, String localName, String qName) throws SAXException {
    if ("class".equalsIgnoreCase(qName)) {
      listSuiteReport.put(tmpSuiteReport.getName(), tmpSuiteReport);
      inclass = false;
    }
  }

  @Override
  public void characters(char[] ch, int start, int length) throws SAXException {
    // tmpValue = new String(ch, start, length);
  }

}
