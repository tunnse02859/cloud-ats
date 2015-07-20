/**
 * 
 */
package org.ats.services.generator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.ats.common.MapBuilder;
import org.ats.common.StringUtil;
import org.ats.common.http.HttpURL;
import org.ats.services.keyword.KeywordProject;
import org.ats.services.keyword.KeywordProjectService;
import org.ats.services.performance.JMeterArgument;
import org.ats.services.performance.JMeterSampler;
import org.ats.services.performance.JMeterSampler.Method;
import org.ats.services.performance.JMeterScript;
import org.ats.services.performance.JMeterScriptReference;
import org.ats.services.performance.PerformanceProject;
import org.ats.services.performance.PerformanceProjectService;
import org.rythmengine.RythmEngine;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Jul 14, 2015
 */
@Singleton
public class GeneratorService {

  @Inject
  KeywordProjectService kpService;
  
  @Inject
  PerformanceProjectService perService;
  
  public void generate(String outDir, PerformanceProject project, boolean compress, List<String> remotes) throws IOException {
    File sourceDir = new File(outDir + "/src/test/java/org/ats/generated");
    sourceDir.mkdirs();
    
    File resourceDir = new File(outDir + "/src/test/resources/jmeter/bin");
    resourceDir.mkdirs();
    
    loadJMeterProperties(resourceDir);
    loadJMeterUtilities(sourceDir);
    loadJMeterPOM(outDir);
    
    String runnerTemplate = StringUtil.readStream(
        Thread.currentThread().getContextClassLoader().getResourceAsStream("jmeter/java/JMeterRunner.java.tmpl"));
    
    String scriptTemplate = StringUtil.readStream(
        Thread.currentThread().getContextClassLoader().getResourceAsStream("jmeter/java/script.tmpl"));
    
    String samplerTemplate = StringUtil.readStream(
        Thread.currentThread().getContextClassLoader().getResourceAsStream("jmeter/java/sampler.tmpl"));

    RythmEngine engine = new RythmEngine(new MapBuilder<String, Boolean>("codegen.compact", false).build());
    
    StringBuilder scriptBuilder = new StringBuilder();
    
    for (JMeterScriptReference ref : project.getScripts()) {
      JMeterScript jScript = ref.get();
      String scriptName = normalize(jScript.getName());
      int loops = jScript.getLoops();
      int numberThreads = jScript.getNumberThreads();
      int ramUp = jScript.getRamUp();
      
      StringBuilder samplerBuilder = new StringBuilder();
      
      for (JMeterSampler jSampler :jScript.getSamplers()) {
        String samplerName = jSampler.getName();
        String defaultTimeout = "120000";
        HttpURL url = new HttpURL(jSampler.getUrl());
        String domain = url.getDomain();
        int port = url.getPort();
        String protocol = url.getProtocol();
        String path = url.getFullPath();
        String method = jSampler.getMethod().toString();
        
        StringBuilder paramBuilder = new StringBuilder("new MapBuilder()");
        
        for (JMeterArgument jArg : jSampler.getArguments()) {
          paramBuilder.append(".append(\"").append(jArg.getParamName()).append("\",\"").append(jArg.getParamValue()).append("\")");
        }
        paramBuilder.append(".build()");
        String params = paramBuilder.toString();
        
        String assertionText = jSampler.getAssertionText();
        String constantTimer = Long.toString(jSampler.getConstantTime());
        samplerBuilder.append(engine.render(samplerTemplate, samplerName, defaultTimeout, domain, port, protocol, path, method, params, assertionText, constantTimer));
      }
      
      String samplers = samplerBuilder.toString();
      
      scriptBuilder.append(engine.render(scriptTemplate, scriptName, loops, numberThreads, ramUp, samplers));
      
      String runner = engine.render(runnerTemplate, scriptBuilder.toString());
      FileOutputStream os = new FileOutputStream(new File(sourceDir, "JMeterRunner.java"));
      os.write(runner.getBytes());
      os.flush();
      os.close();
    }
  }
  
  public void generate(String outDir, KeywordProject project, boolean compress) throws IOException {
  //
  }
  
  private void loadJMeterPOM(String outDir) throws IOException {
    InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("jmeter/pom.xml");
    write(is, new FileOutputStream(new File(outDir, "pom.xml")));
  }
  
  private void loadJMeterProperties(File resourceDir) throws IOException {
    InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("jmeter/bin/jmeter.properties");
    write(is, new FileOutputStream(new File(resourceDir, "jmeter.properties")));
    
    is = Thread.currentThread().getContextClassLoader().getResourceAsStream("jmeter/bin/saveservice.properties");
    write(is, new FileOutputStream(new File(resourceDir, "saveservice.properties")));
    
    is = Thread.currentThread().getContextClassLoader().getResourceAsStream("jmeter/bin/system.properties");
    write(is, new FileOutputStream(new File(resourceDir, "system.properties")));
    
    is = Thread.currentThread().getContextClassLoader().getResourceAsStream("jmeter/bin/upgrade.properties");
    write(is, new FileOutputStream(new File(resourceDir, "upgrade.properties")));
    
    is = Thread.currentThread().getContextClassLoader().getResourceAsStream("jmeter/bin/user.properties");
    write(is, new FileOutputStream(new File(resourceDir, "user.properties")));
  }
  
  private void loadJMeterUtilities(File sourceDir) throws IOException {
    InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("jmeter/java/ListenToTest.java.tmpl");
    write(is, new FileOutputStream(new File(sourceDir, "ListenToTest.java")));
    
    is = Thread.currentThread().getContextClassLoader().getResourceAsStream("jmeter/java/MapBuilder.java.tmpl");
    write(is, new FileOutputStream(new File(sourceDir, "MapBuilder.java")));
    
    is = Thread.currentThread().getContextClassLoader().getResourceAsStream("jmeter/java/SamplerBuilder.java.tmpl");
    write(is, new FileOutputStream(new File(sourceDir, "SamplerBuilder.java")));
    
    is = Thread.currentThread().getContextClassLoader().getResourceAsStream("jmeter/java/Utils.java.tmpl");
    write(is, new FileOutputStream(new File(sourceDir, "Utils.java")));    
  }
  
  private void write(InputStream is, OutputStream out) throws IOException {
    byte[] buffer = new byte[1024];
    for (int l = is.read(buffer); l != -1; l = is.read(buffer)) {
      out.write(buffer, 0, l);
    }
    is.close();
    out.close();
  }
  
  private String normalize(String name) {
    name = name.replace(' ', '_');
    name = name.replace('-', '_');
    return name;
  }
}
