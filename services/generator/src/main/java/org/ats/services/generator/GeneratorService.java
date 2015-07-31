/**
 * 
 */
package org.ats.services.generator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.ats.common.MapBuilder;
import org.ats.common.StringUtil;
import org.ats.common.http.HttpURL;
import org.ats.services.keyword.KeywordProject;
import org.ats.services.keyword.KeywordProjectService;
import org.ats.services.keyword.Suite;
import org.ats.services.keyword.SuiteReference;
import org.ats.services.performance.JMeterArgument;
import org.ats.services.performance.JMeterSampler;
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
  
  /**
   * 
   * @param outDir
   * @param project
   * @param compress
   * @param scripts 
   * @return The path to the project located
   * @throws IOException
   */
  public String generate(String outDir, PerformanceProject project, boolean compress, List<JMeterScriptReference> scripts) throws IOException {
    String projectHash = project.getId().substring(0, 8);
    File sourceDir = new File(outDir + "/" + projectHash  + "/src/test/java/org/ats/generated");
    sourceDir.mkdirs();
    
    File resourceDir = new File(outDir + "/" + projectHash  + "/src/test/resources/jmeter/bin");
    resourceDir.mkdirs();
    
    loadJMeterProperties(resourceDir);
    loadJMeterUtilities(sourceDir);
    loadJMeterPOM(outDir + "/" + projectHash);
    
    String runnerTemplate = StringUtil.readStream(
        Thread.currentThread().getContextClassLoader().getResourceAsStream("jmeter/java/JMeterRunner.java.tmpl"));
    
    String scriptTemplate = StringUtil.readStream(
        Thread.currentThread().getContextClassLoader().getResourceAsStream("jmeter/java/script.tmpl"));
    
    String samplerTemplate = StringUtil.readStream(
        Thread.currentThread().getContextClassLoader().getResourceAsStream("jmeter/java/sampler.tmpl"));

    RythmEngine engine = new RythmEngine(new MapBuilder<String, Boolean>("codegen.compact", false).build());
    
    StringBuilder scriptBuilder = new StringBuilder();
    
    for (JMeterScriptReference ref : scripts) {
      
      if (!project.getScripts().contains(ref)) continue;
      
      JMeterScript jScript = ref.get();
      String scriptName = StringUtil.normalizeName(jScript.getName());
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
      scriptBuilder.append(engine.render(scriptTemplate, ref.getId(), scriptName, loops, numberThreads, ramUp, samplers));
    }
    
    Map<String, String> params = new HashMap<String, String>();
    params.put("scripts", scriptBuilder.toString());
    String runner = engine.render(runnerTemplate, params);
    
    FileOutputStream os = new FileOutputStream(new File(sourceDir, "JMeterRunner.java"));
    os.write(runner.getBytes());
    os.flush();
    os.close();
    
    if (compress) {
      compress(projectHash, outDir + "/" + projectHash, outDir + "/" + projectHash + ".zip");
      return outDir + "/" + projectHash + ".zip";
    }
    
    return outDir + "/" + projectHash;
  }
  
  /**
   * 
   * @param outDir
   * @param project
   * @param compress
   * @return The path to project located
   * @throws IOException
   */
  public String generate(String outDir, KeywordProject project, boolean compress, List<SuiteReference> suites) throws IOException {
    String projectHash = project.getId().substring(0, 8);
    File sourceDir = new File(outDir + "/" + projectHash  + "/src/test/java/org/ats/generated");
    sourceDir.mkdirs();
    
    loadKeywordPOM(outDir + "/" + projectHash);
    
    for (SuiteReference suiteRef : suites) {
      
      Suite suite = suiteRef.get();
      FileOutputStream os = new FileOutputStream(new File(sourceDir, StringUtil.normalizeName(suite.getString("suite_name") + ".java")));
      os.write(suite.transform().getBytes());
      os.flush();
      os.close();
    }
    
    if (compress) {
      compress(projectHash, outDir + "/" + projectHash,  outDir + "/" + projectHash  + ".zip");
      return outDir + "/" + projectHash  + ".zip";
    }
    
    return outDir + "/" + projectHash;
  }
  
  private void loadKeywordPOM(String outDir) throws IOException {
    InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("keyword/pom.xml");
    write(is, new FileOutputStream(new File(outDir, "pom.xml")));
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
  
  private void compress(String projectId, String from, String to) throws IOException {
    File fromDir = new File(from);
    ZipOutputStream outDir = new ZipOutputStream(new FileOutputStream(to));
    write(projectId, fromDir, outDir);
    outDir.close();
  }
  
  private void write(String projectId, File file, ZipOutputStream outDir) throws IOException {
    byte[] buffer = new byte[4096]; // Create a buffer for copying
    int bytes_read;
    
    for (String entry : file.list()) {
      File f = new File(file, entry);
      if (f.isDirectory()) {
        write(projectId, f, outDir);
        continue;
      }
      FileInputStream fis = new FileInputStream(f);
      
      String path = f.getPath().substring(f.getPath().indexOf(projectId));
      ZipEntry zip = new ZipEntry(path);
      outDir.putNextEntry(zip);
      while ((bytes_read = fis.read(buffer)) != -1) {
        outDir.write(buffer, 0, bytes_read);
      }
      fis.close();
    }
  }
  
  private void write(InputStream is, OutputStream out) throws IOException {
    byte[] buffer = new byte[1024];
    for (int l = is.read(buffer); l != -1; l = is.read(buffer)) {
      out.write(buffer, 0, l);
    }
    is.close();
    out.close();
  }
}
