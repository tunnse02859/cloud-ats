/**
 * 
 */
package org.ats.jmeter;

import java.util.Collections;
import java.util.Map;

import javax.xml.xpath.XPathConstants;

import org.ats.common.html.HtmlParser;
import org.ats.common.html.XPathUtil;
import org.ats.jmeter.models.JMeterArgument;
import org.ats.jmeter.models.JMeterSampler;
import org.ats.jmeter.models.JMeterSampler.Method;
import org.ats.jmeter.models.JMeterScript;
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
  
  /** .*/
  private Map<String, String> templates;
  
  JMeterParser(String source, Map<String, String> templates) throws Exception {
    this.source = source;
    this.templates = templates;
  }
  
  public String getSource() {
    return source;
  }
  
  public Map<String, String> getTemplates() {
    return Collections.unmodifiableMap(this.templates);
  }
  
  public JMeterScript parse() throws Exception {
    HtmlParser parser = new HtmlParser();
    Document document = parser.parseNonWellForm(source);
    
    String testName = ((Node) XPathUtil.read(document, "//THREADGROUP", XPathConstants.NODE)).getAttributes().getNamedItem("testname").getNodeValue();

    int loops = Integer.parseInt(((Node) XPathUtil.read(document, "//STRINGPROP[@name=\"LoopController.loops\"]", XPathConstants.NODE)).getTextContent());
    
    int numberThreads = Integer.parseInt(((Node) XPathUtil.read(document, "//STRINGPROP[@name=\"ThreadGroup.num_threads\"]", XPathConstants.NODE)).getTextContent());
    
    int ramUp = Integer.parseInt(((Node) XPathUtil.read(document, "//STRINGPROP[@name=\"ThreadGroup.ramp_time\"]", XPathConstants.NODE)).getTextContent());
    
    boolean scheduler = Boolean.parseBoolean(((Node) XPathUtil.read(document, "//BOOLPROP[@name=\"ThreadGroup.scheduler\"]", XPathConstants.NODE)).getTextContent());
    
    int duration = Integer.parseInt(((Node) XPathUtil.read(document, "//STRINGPROP[@name=\"ThreadGroup.duration\"]", XPathConstants.NODE)).getTextContent());
    
  
    //Find samplers
    
    NodeList samplerNodeList = (NodeList) XPathUtil.read(document, "//HTTPSAMPLERPROXY", XPathConstants.NODESET);
    JMeterSampler[] samplers = new JMeterSampler[samplerNodeList.getLength()];

    for (int i = 0; i < samplerNodeList.getLength(); i++) {
      Node samplerNode = samplerNodeList.item(i);
      
      String samplerName = samplerNode.getAttributes().getNamedItem("testname").getNodeValue();
      
      String domain = ((Node) XPathUtil.read(samplerNode, "STRINGPROP[@name=\"HTTPSampler.domain\"]", XPathConstants.NODE)).getTextContent();
      
      String port = ((Node) XPathUtil.read(samplerNode, "STRINGPROP[@name=\"HTTPSampler.port\"]", XPathConstants.NODE)).getTextContent();
      
      String protocol = ((Node) XPathUtil.read(samplerNode, "STRINGPROP[@name=\"HTTPSampler.protocol\"]", XPathConstants.NODE)).getTextContent();
      
      String path = ((Node) XPathUtil.read(samplerNode, "STRINGPROP[@name=\"HTTPSampler.path\"]", XPathConstants.NODE)).getTextContent();
    
      Method method = Method.valueOf(((Node) XPathUtil.read(samplerNode, "STRINGPROP[@name=\"HTTPSampler.method\"]", XPathConstants.NODE)).getTextContent());
      
      String url = protocol + "://" + domain + ":" + port  + path;
      
      Node argumentsNode = (Node) XPathUtil.read(samplerNode, "ELEMENTPROP[@name='HTTPsampler.Arguments']/COLLECTIONPROP[@name='Arguments.arguments']", XPathConstants.NODE);
      
      JMeterArgument[] arguments = new JMeterArgument[] {};
      
      if (argumentsNode != null) {
        
        NodeList argumentsNodeList = (NodeList) XPathUtil.read(argumentsNode, "ELEMENTPROP", XPathConstants.NODESET);
        
        arguments = new JMeterArgument[argumentsNodeList.getLength()];
        
        for(int j = 0; j < argumentsNodeList.getLength(); j++) {
          Node argumentNode = argumentsNodeList.item(j);
          
          String paramName = ((Node)XPathUtil.read(argumentNode, "STRINGPROP[@name='Argument.name']", XPathConstants.NODE)).getTextContent();
          String paramValue = ((Node)XPathUtil.read(argumentNode, "STRINGPROP[@name='Argument.value']", XPathConstants.NODE)).getTextContent();
          
          arguments[j] = new JMeterArgument(this.templates, paramName, paramValue);
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
      
      samplers[i] = new JMeterSampler(templates, method, samplerName, url, assertionText, contantTime, arguments);
    }
    // End find samplers
    
    return new JMeterScript(templates, testName, loops, numberThreads, ramUp, scheduler, duration, samplers);
  }
}
