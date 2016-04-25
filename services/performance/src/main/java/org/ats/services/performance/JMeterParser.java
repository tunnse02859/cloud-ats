/**
 * 
 */
package org.ats.services.performance;

import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPathConstants;

import org.ats.common.html.HtmlParser;
import org.ats.common.html.XPathUtil;
import org.ats.services.performance.JMeterSampler.Method;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Oct 22, 2014
 */
public class JMeterParser {

  /** .*/
  private final String source;
  
  private String projectId;
  JMeterParser(String source, String projectId) throws Exception {
    this.source = source;
    this.projectId = projectId;
  }
  
  public String getSource() {
    return source;
  }
  
  public String getProjectId() {
    return projectId;
  }
  
  public JMeterScript parse() throws Exception {
    HtmlParser parser = new HtmlParser();
    Document document = parser.parseNonWellForm(source);
    
    String testName = ((Node) XPathUtil.read(document, "//THREADGROUP", XPathConstants.NODE)).getAttributes().getNamedItem("testname").getNodeValue();

    Object obj = XPathUtil.read(document, "//INTPROP[@name=\"LoopController.loops\"]", XPathConstants.NODE);
    if (obj == null) obj = XPathUtil.read(document, "//STRINGPROP[@name=\"LoopController.loops\"]", XPathConstants.NODE);
    
    int loops = 0;
    if (obj != null) {
      String loopStr = ((Node) obj).getTextContent();
      loops = (loopStr == null || loopStr.isEmpty()) ? 0 : Integer.parseInt(loopStr);
    }
    
    obj = XPathUtil.read(document, "//STRINGPROP[@name=\"ThreadGroup.num_threads\"]", XPathConstants.NODE);
    if (obj == null) obj = XPathUtil.read(document, "//INTPROP[@name=\"ThreadGroup.num_threads\"]", XPathConstants.NODE);
    
    int numberThreads = 1;
    if (obj != null) {
      String numberThreadsStr = ((Node) obj).getTextContent();
      numberThreads = (numberThreadsStr == null || numberThreadsStr.isEmpty()) ? 0 : Integer.parseInt(numberThreadsStr);
    }
    
    obj = XPathUtil.read(document, "//STRINGPROP[@name=\"ThreadGroup.ramp_time\"]", XPathConstants.NODE);
    if (obj == null) obj = XPathUtil.read(document, "//INTPROP[@name=\"ThreadGroup.ramp_time\"]", XPathConstants.NODE);
    
    int ramUp = 5;
    if (obj != null) {
      String ramUpStr = ((Node) obj).getTextContent();
      ramUp = (ramUpStr == null || ramUpStr.isEmpty()) ? 0 : Integer.parseInt(ramUpStr);
    }
    
    boolean scheduler = false;
    obj = XPathUtil.read(document, "//BOOLPROP[@name=\"ThreadGroup.scheduler\"]", XPathConstants.NODE);
    if (obj != null) {
      String schedulerStr = ((Node) obj).getTextContent();
      scheduler = (schedulerStr == null || schedulerStr.isEmpty()) ? false : Boolean.parseBoolean(schedulerStr);
    }
    
    int duration = 0;
    obj = XPathUtil.read(document, "//STRINGPROP[@name=\"ThreadGroup.duration\"]", XPathConstants.NODE);
    if (obj != null) {
      String durationStr = ((Node) obj).getTextContent();
      duration = (durationStr == null || durationStr.isEmpty()) ? 0 : Integer.parseInt(durationStr);
    }
    
    //Find samplers
    
    NodeList samplerNodeList = (NodeList) XPathUtil.read(document, "//HTTPSAMPLERPROXY", XPathConstants.NODESET);
    List<JMeterSampler> samplers = new ArrayList<JMeterSampler>();

    for (int i = 0; i < samplerNodeList.getLength(); i++) {
      Node samplerNode = samplerNodeList.item(i);
      
      String samplerName = samplerNode.getAttributes().getNamedItem("testname").getNodeValue();
      
      String domain = ((Node) XPathUtil.read(samplerNode, "STRINGPROP[@name=\"HTTPSampler.domain\"]", XPathConstants.NODE)).getTextContent();
      
      String port = "80";
      obj = XPathUtil.read(samplerNode, "STRINGPROP[@name=\"HTTPSampler.port\"]", XPathConstants.NODE);
      if (obj == null) {
        obj = XPathUtil.read(samplerNode, "INTPROP[@name=\"HTTPSampler.port\"]", XPathConstants.NODE);
      }
      if (obj != null) port = ((Node) obj).getTextContent();
      
      String protocol = ((Node) XPathUtil.read(samplerNode, "STRINGPROP[@name=\"HTTPSampler.protocol\"]", XPathConstants.NODE)).getTextContent();
      
      String path = ((Node) XPathUtil.read(samplerNode, "STRINGPROP[@name=\"HTTPSampler.path\"]", XPathConstants.NODE)).getTextContent();
    
      Method method = Method.valueOf(((Node) XPathUtil.read(samplerNode, "STRINGPROP[@name=\"HTTPSampler.method\"]", XPathConstants.NODE)).getTextContent());
      
      String url = protocol + "://" + domain + ":" + port  + path;
      
      Node argumentsNode = (Node) XPathUtil.read(samplerNode, "ELEMENTPROP[@name='HTTPsampler.Arguments']/COLLECTIONPROP[@name='Arguments.arguments']", XPathConstants.NODE);
      
      List<JMeterArgument> arguments = new ArrayList<JMeterArgument>();
      
      if (argumentsNode != null) {
        
        NodeList argumentsNodeList = (NodeList) XPathUtil.read(argumentsNode, "ELEMENTPROP", XPathConstants.NODESET);
        
        for(int j = 0; j < argumentsNodeList.getLength(); j++) {
          Node argumentNode = argumentsNodeList.item(j);
          
          String paramName = ((Node)XPathUtil.read(argumentNode, "STRINGPROP[@name='Argument.name']", XPathConstants.NODE)).getTextContent();
          String paramValue = ((Node)XPathUtil.read(argumentNode, "STRINGPROP[@name='Argument.value']", XPathConstants.NODE)).getTextContent();
          
          arguments.add(new JMeterArgument(paramName, paramValue));
        }
      }
      
      Node sibling = samplerNode.getNextSibling();
      while (!(sibling instanceof Element) && sibling != null) {
        sibling = sibling.getNextSibling();
      }
      
      //Find Assertion Text
      Node assertionTextNode = (Node) XPathUtil.read(sibling, "RESPONSEASSERTION", XPathConstants.NODE);
      String assertionText = null;
      
      if (assertionTextNode != null) {
        assertionText = ((Node)XPathUtil.read(assertionTextNode, "COLLECTIONPROP[@name='Asserion.test_strings']/STRINGPROP", XPathConstants.NODE)).getTextContent();
      }
      //
      
      //Find Contant Time
      Node contantTimeNode = (Node) XPathUtil.read(sibling, "CONSTANTTIMER", XPathConstants.NODE);
      long contantTime = 0;
      if (contantTimeNode != null) {
        contantTime = Long.parseLong(((Node)XPathUtil.read(contantTimeNode, "STRINGPROP[@name='ConstantTimer.delay']", XPathConstants.NODE)).getTextContent());
      }
      //
      
      samplers.add(new JMeterSampler(method, samplerName, url, assertionText, contantTime, arguments));
    }
    // End find samplers
    
    return new JMeterScript(testName, loops, numberThreads, ramUp, scheduler, duration, getProjectId(), "", samplers);
  }
}
