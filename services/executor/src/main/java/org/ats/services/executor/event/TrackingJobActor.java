/**
 * 
 */
package org.ats.services.executor.event;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.ats.common.PageList;
import org.ats.common.ssh.SSHClient;
import org.ats.jenkins.JenkinsMaster;
import org.ats.jenkins.JenkinsMavenJob;
import org.ats.service.blob.BlobService;
import org.ats.services.event.Event;
import org.ats.services.event.EventFactory;
import org.ats.services.executor.ExecutorService;
import org.ats.services.executor.job.AbstractJob;
import org.ats.services.executor.job.AbstractJob.Status;
import org.ats.services.executor.job.KeywordJob;
import org.ats.services.executor.job.PerformanceJob;
import org.ats.services.executor.job.SeleniumUploadJob;
import org.ats.services.generator.GeneratorService;
import org.ats.services.iaas.IaaSService;
import org.ats.services.iaas.IaaSServiceProvider;
import org.ats.services.keyword.KeywordProject;
import org.ats.services.keyword.KeywordProjectService;
import org.ats.services.keyword.report.KeywordReportService;
import org.ats.services.keyword.report.SuiteReportService;
import org.ats.services.keyword.report.models.CaseReport;
import org.ats.services.keyword.report.models.CaseReportReference;
import org.ats.services.keyword.report.models.StepReport;
import org.ats.services.keyword.report.models.StepReportReference;
import org.ats.services.keyword.report.models.SuiteReport;
import org.ats.services.organization.entity.fatory.ReferenceFactory;
import org.ats.services.organization.entity.reference.SpaceReference;
import org.ats.services.organization.entity.reference.TenantReference;
import org.ats.services.performance.JMeterScriptReference;
import org.ats.services.performance.JMeterScriptService;
import org.ats.services.performance.PerformanceProject;
import org.ats.services.performance.PerformanceProjectService;
import org.ats.services.upload.SeleniumUploadProject;
import org.ats.services.upload.SeleniumUploadProjectService;
import org.ats.services.vmachine.VMachine;
import org.ats.services.vmachine.VMachineReference;
import org.ats.services.vmachine.VMachineService;

import akka.actor.UntypedActor;

import com.google.inject.Inject;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.Session;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Jul 23, 2015
 */
public class TrackingJobActor extends UntypedActor {

  @Inject ExecutorService executorService;
  
  @Inject GeneratorService generatorService;
  
  @Inject KeywordProjectService keywordService;
  
  @Inject PerformanceProjectService perfService;
  
  @Inject SeleniumUploadProjectService keywordUploadService;
  
  @Inject VMachineService vmachineService;
  
  @Inject IaaSServiceProvider iaasProvider;
  
  @Inject EventFactory eventFactory;
  
  @Inject Logger logger;
  
  @Inject ReferenceFactory<VMachineReference> vmRefFactory;
  
  @Inject JMeterScriptService jmeterService;
  
  @Inject BlobService blobService;
  
  @Inject KeywordReportService reportService;
  
  @Inject SuiteReportService suiteReportService;
  
  
  ConcurrentHashMap<String, JenkinsMavenJob> cache = new ConcurrentHashMap<String, JenkinsMavenJob>();
  
  @Override
  public void onReceive(Object obj) throws Exception {
    if (obj instanceof Event) {
      Event event = (Event) obj;
      if ("keyword-job-tracking".equals(event.getName())) {
        KeywordJob job = (KeywordJob) event.getSource();
        processKeywordJob(job);
      } else if ("performance-job-tracking".equals(event.getName())) {
        PerformanceJob job = (PerformanceJob) event.getSource();
        processPerformanceJob(job);
      } else  if ("upload-job-tracking".equals(event.getName())) {
        SeleniumUploadJob job = (SeleniumUploadJob) event.getSource();
        processUploadJob(job);
      }
    }
  }

  private void processPerformanceJob(PerformanceJob job) throws Exception {
    try {
      PerformanceProject project = perfService.get(job.getProjectId());
      switch (job.getStatus()) {
      case Queued:
        doExecutePerformanceJob(job, project);
        break;
      case Running:
        doTrackingPerformanceJob(job, project);
        break;
      default:
        break;
      }
    } catch(Exception e) {
      job = (PerformanceJob) executorService.get(job.getId(), "vms");
      job.setStatus(Status.Completed);
      
      for (VMachineReference ref : job.getVMs()) {
        VMachine vm = ref.get();
        if (vm != null) {
          vm.setStatus(VMachine.Status.Started);
          vmachineService.update(vm);
        }
      }
      
      PerformanceProject project = perfService.get(job.getProjectId());
      project.setStatus(PerformanceProject.Status.READY);
      perfService.update(project);
      
      
      executorService.update(job);
      
      //bubble event
      Event event = eventFactory.create(job, "performance-job-tracking");
      event.broadcast();
      throw e;
    }
  }
  
  private void doTrackingPerformanceJob(PerformanceJob job, PerformanceProject project) throws Exception {
    
    TenantReference tenant = project.getTenant();
    SpaceReference space = project.getSpace();
    
    VMachine systemVM = vmachineService.getSystemVM(tenant, space);
    JenkinsMaster jkMaster = new JenkinsMaster(systemVM.getPublicIp(), "http", "jenkins", 8080);
    
    JenkinsMavenJob jkJob = cache.get(job.getId());
    if (jkJob == null) {
      jkJob = new JenkinsMavenJob(jkMaster, job.getId(), null, null, null);
      cache.put(job.getId(), jkJob);
    }
    
    boolean isBuilding = jkJob.isBuilding(1, System.currentTimeMillis(), 60 * 1000);
    
    if (isBuilding) {
      
      updateLog(job, jkJob);
      
      Thread.sleep(15000L);
      
      Event event  = eventFactory.create(job, "performance-job-tracking");
      event.broadcast();
      
    } else {
      
      updateLog(job, jkJob);
      
      //Download result
      BasicDBList list = new BasicDBList();
      for (JMeterScriptReference ref : job.getScripts()) {
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        
        SSHClient.getFile(systemVM.getPublicIp(), 22, "cloudats", "#CloudATS", 
            "/home/cloudats/projects/" + job.getId() + "/target/" + ref.getId() + ".jtl",  bos);
        
        if (bos.size() > 0) {
          list.add(new BasicDBObject("_id", ref.getId()).append("content", new String(bos.toByteArray())));
        }
      }
      job.put("report", list);
      //End download result

      job.setStatus(AbstractJob.Status.Completed);
      executorService.update(job);
      
      //Reset test vm status and release floating ip
    //  VMachine testVM = vmachineService.get(job.getTestVMachineId());
      List<VMachineReference> pages = job.getVMs();
      for (VMachineReference ref : pages) {
        VMachine testVM = ref.get();
        testVM.setStatus(VMachine.Status.Started);
        vmachineService.update(testVM);
      }
      //testVM.setStatus(VMachine.Status.Started);
      //vmachineService.update(testVM);
      
      project.setStatus(PerformanceProject.Status.READY);
      perfService.update(project);
      
      jkJob.delete();
      cache.remove(job.getId());
      
      Event event  = eventFactory.create(job, "performance-job-tracking");
      event.broadcast();
    }
  }
  
  private void doExecutePerformanceJob(PerformanceJob job, PerformanceProject project) throws Exception {
    
    VMachine jenkinsVM = vmachineService.getSystemVM(project.getTenant(), project.getSpace());
    PageList<VMachine> listVM = vmachineService.query(new BasicDBObject("system", false).append("ui", false));
    
    int totalEngines = 0;
    
    for (JMeterScriptReference ref : job.getScripts()) {
      
      int numberEngine = jmeterService.get(ref.getId(), "number_engines").getNumberEngines();

      totalEngines = Math.max(totalEngines, numberEngine);
    }
    
    
    //Create lacking VM
    if (listVM.count() < totalEngines) {
      Thread.sleep(5000);
      updateLog(job, "Lack "+(totalEngines - listVM.count())+" VM to run test");
      updateLog(job, "Creating new VMs (about 4-8 minutes for one VM) ...");

      // create new test VM
      for (int i = 0; i < totalEngines - listVM.count(); i ++) {
        
        VMachine testVM = iaasProvider.get().createTestVMAsync(project.getTenant(),  project.getSpace(),  false, false);
        updateLog(job, "Created new VM "+ testVM);
        
        job.addVMachine(vmRefFactory.create(testVM.getId()));
        executorService.update(job);
        
      }
      
      Event event = eventFactory.create(job, "performance-job-tracking");
      event.broadcast();
      return;
    }
    //End create lacking VM
    
    PageList<VMachine> listVMStarted = vmachineService.query(new BasicDBObject("system", false).append("ui", false).append("status", VMachine.Status.Started.toString()));
    
    if (listVM.count() == totalEngines && listVM.count() != listVMStarted.count() ) {
      while (listVM.hasNext()) {
        List<VMachine> page = listVM.next();
        
        for (VMachine vm : page) {
          if (vm.getStatus() == VMachine.Status.Initializing) {
            Thread.sleep(5000);
            updateLog(job, "Waiting Test VM " + vm.getPrivateIp() + " intializing ...");
          }
        }
        Event event  = eventFactory.create(job, "performance-job-tracking");
        event.broadcast();
        return;
      }
    } else {
      updateLog(job, "Generating performance project");
      String path = generatorService.generatePerformance("/tmp", job.getId(), true, job.getScripts());

      updateLog(job, "Uploading to VM Test");
      SSHClient.sendFile(jenkinsVM.getPublicIp(), 22, "cloudats", "#CloudATS", "/home/cloudats/projects", job.getId() + ".zip", new File(path));
      
      Thread.sleep(3000);
      
      SSHClient.execCommand(jenkinsVM.getPublicIp(), 22, "cloudats", "#CloudATS", 
          "cd /home/cloudats/projects && unzip " +  job.getId() + ".zip", null, null);
      
      //get list of VM (max = 10 VM)
      //TODO: What will happen if need more than 10 VMs ?
      List<VMachine> listVMs = null;
      while (listVM.hasNext()) {
        listVMs = listVM.next();
      } 
      
      List<JMeterScriptReference> listScript = job.getScripts();
      StringBuilder goalsBuilder = new StringBuilder("clean test ");
      for (JMeterScriptReference ref : listScript) {
        
        goalsBuilder.append("-Djmeter.").append(ref.getId()).append("=");
        int engines = jmeterService.get(ref.getId(), "number_engines").getNumberEngines();
        
        int count = 0;
        for (int j = 0; j < engines; j ++) {
          VMachine testVm = listVMs.get(j);
          goalsBuilder.append(testVm.getPrivateIp());
          job.addVMachine(vmRefFactory.create(testVm.getId()));
          testVm.setStatus(VMachine.Status.InProgress);
          vmachineService.update(testVm);
          
          count ++;
          
          if (count < engines) {
            goalsBuilder.append(",");
          }
        }
        goalsBuilder.append(" ");
        executorService.update(job);
        uploadCSVData(ref, job);
      }
      
      JenkinsMaster jenkinsMaster = new JenkinsMaster(jenkinsVM.getPublicIp(), "http", "/jenkins", 8080);
      JenkinsMavenJob jenkinsJob = new JenkinsMavenJob(jenkinsMaster, job.getId(), 
          "master" , "/home/cloudats/projects/" + job.getId() + "/pom.xml", goalsBuilder.toString());

      jenkinsJob.submit();
      updateLog(job, "Submitted Jenkins job");

      job.setStatus(Status.Running);
      
    }
    
    executorService.update(job);

    Event event  = eventFactory.create(job, "performance-job-tracking");
    event.broadcast();
  }
  
  private void uploadCSVData(JMeterScriptReference script, PerformanceJob job) throws Exception {
    List<GridFSDBFile> files = blobService.find(new BasicDBObject("script_id", script.getId()));
    if (files == null || files.size() == 0) return;
    IaaSService service = iaasProvider.get();
    for (VMachineReference ref : job.getVMs()) {

      VMachine vm = ref.get();
      try {
        vm = service.allocateFloatingIp(vm);
        
        ZipOutputStream outDir = new ZipOutputStream(new FileOutputStream("/tmp/" + job.getId() + ".zip"));
        for (GridFSDBFile file : files) {
          
          byte[] buffer = new byte[4096]; // Create a buffer for copying
          int bytes_read;

          InputStream is = file.getInputStream();
          ZipEntry zip = new ZipEntry(file.getFilename());
          outDir.putNextEntry(zip);
          while ((bytes_read = is.read(buffer)) != -1) {
            outDir.write(buffer, 0, bytes_read);
          }
          is.close();
        }
        outDir.close();
        
        SSHClient.checkEstablished(vm.getPublicIp(), 22, 300);
        logger.log(Level.INFO, "Connection to  " + vm.getPublicIp() + " is established");
        
        updateLog(job, "Synchronizing /tmp/" + job.getId() + ".zip to " + vm.getPublicIp() + ":/home/cloudats/projects/"  + job.getId() + "/src/test/resources");
        SSHClient.sendFile(vm.getPublicIp(), 22, "cloudats", "#CloudATS", 
            "/home/cloudats/projects/" + job.getId() + "/src/test/resources", 
            "data.zip", new File("/tmp/" + job.getId() + ".zip"));
        
        Thread.sleep(3000);
        
        SSHClient.execCommand(vm.getPublicIp(), 22, "cloudats", "#CloudATS", 
            "cd /home/cloudats/projects/" + job.getId() + "/src/test/resources" + " && unzip data.zip", null, null);
      } finally {
        service.deallocateFloatingIp(vm);
      }
    }
  }

  private void processKeywordJob(KeywordJob job) throws Exception {
    try {
      KeywordProject project = keywordService.get(job.getProjectId(),"show_action","value_delay","version_selenium");
      switch (job.getStatus()) {
      case Queued:
        doExecuteKeywordJob(job, project);
        break;
      case Running:
        doTrackingKeywordJob(job, project);
        break;
      default:
        break;
      }
    } catch (Exception e) {
      job.setStatus(Status.Completed);
      executorService.update(job);
      
      VMachine vm = vmachineService.get(job.getTestVMachineId());
      if (vm != null) {
        vm.setStatus(VMachine.Status.Started);
        if (vm.getPublicIp() != null) iaasProvider.get().deallocateFloatingIp(vm);
        vmachineService.update(vm);
      }
      
      KeywordProject project = keywordService.get(job.getProjectId());
      project.setStatus(KeywordProject.Status.READY);
      keywordService.update(project);
      
      //bubble event
      Event event = eventFactory.create(job, "keyword-job-tracking");
      event.broadcast();
      throw e;
    }
  }
  
  private void doTrackingKeywordJob(KeywordJob job, KeywordProject project) throws Exception {
    
    TenantReference tenant = project.getTenant();
    SpaceReference space = project.getSpace();
    
    VMachine systemVM = vmachineService.getSystemVM(tenant, space);
    VMachine testVM = vmachineService.get(job.getTestVMachineId());
    
    JenkinsMaster jkMaster = new JenkinsMaster(systemVM.getPublicIp(), "http", "jenkins", 8080);
    JenkinsMavenJob jkJob = cache.get(job.getId());
    if (jkJob == null) {
      jkJob = new JenkinsMavenJob(jkMaster, job.getId(), null, null, null);
      cache.put(job.getId(), jkJob);
    }
    
    boolean isBuilding = jkJob.isBuilding(1, System.currentTimeMillis(), 60 * 1000);
    if (isBuilding) {
      
      updateLog(job, jkJob);
      
      Thread.sleep(5000L);
      
      Event event  = eventFactory.create(job, "keyword-job-tracking");
      event.broadcast();
      
    } else {
      updateLog(job, jkJob);
      //parse log
      byte[] logBytes = job.getLog().getBytes();
      reportService.processLog(logBytes);
      
      //Download target resource
      
      //Zip report
      String cmd =  "cd /home/cloudats/projects/"+job.getId()+" && tar -czvf resource.tar.gz target src pom.xml";
      Session session = SSHClient.getSession(testVM.getPublicIp(), 22, "cloudats", "#CloudATS");              
      ChannelExec channel = (ChannelExec) session.openChannel("exec");
      channel.setCommand(cmd);
      channel.connect();
      
      while(true) {
        if (channel.isClosed()) {
          logger.info("Compress project target status: " + channel.getExitStatus());
          break;
        }
      }
      
      channel.disconnect();
      session.disconnect();
      
      
      ByteArrayOutputStream bosTarget = new ByteArrayOutputStream();
      SSHClient.getFile(testVM.getPublicIp(), 22, "cloudats", "#CloudATS", 
          "/home/cloudats/projects/" + job.getId() + "/resource.tar.gz",  bosTarget);
      
      //Download result
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      SSHClient.getFile(testVM.getPublicIp(), 22, "cloudats", "#CloudATS", 
          "/home/cloudats/projects/" + job.getId() + "/target/surefire-reports/testng-results.xml",  bos);
      
      if (bos.size() > 0)
        job.put("report", new String(bos.toByteArray()));
      
      //create raw_data file
      if (bosTarget.size() > 0) {
        GridFSInputFile project_file = blobService.create(bosTarget.toByteArray());
        project_file.put("job_project_id", job.getId());
        
        blobService.save(project_file);
      }
      
      //End download result
      job.setStatus(AbstractJob.Status.Completed);
      job.put("log", "");
      executorService.update(job);
      
      //save log to file 
      GridFSInputFile file = blobService.create(logBytes);
      file.put("job_log_id", job.getId());
      blobService.save(file);
      // get raw data to tmp folder
      String path = "/tmp/"+job.getProjectId().substring(0, 8);
      File folder = new File(path);
      if(!folder.exists()) {
        folder.mkdir();
      }
      byte[] report = bosTarget.toByteArray();
      FileOutputStream fileOut;
      String filePath = null;
      try {
        filePath = path+"/resource-"+job.getId()+".tar.gz";
        fileOut = new FileOutputStream(filePath);
        fileOut.write(report);
        fileOut.close();
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      }
       catch (IOException e) {
        e.printStackTrace();
      }
      
      String destPath = path+"/images-"+job.getId();
      // extract tar file
      extractTarFile(filePath, destPath);
      
      File imagesFolder = new File(destPath);
      
      if (imagesFolder.listFiles() != null) {
        for (File image : imagesFolder.listFiles()) {
          
          String name = image.getName();
          int index = name.indexOf("_");
          int last = name.lastIndexOf("_");
          long timeStamp = Long.parseLong(name.substring(index + 1, last));
          
          saveImage(image, timeStamp, job.getId());
        }
      }
      //Reset vm status and release floating ip
      testVM.setStatus(VMachine.Status.Started);
      vmachineService.update(testVM);
      
      //TODO:Need review it
      // testVM = openstackService.deallocateFloatingIp(testVM);
      
      project.setStatus(KeywordProject.Status.READY);
      keywordService.update(project);
      
      jkJob.delete();
      cache.remove(job.getId());
      
      Event event  = eventFactory.create(job, "keyword-job-tracking");
      event.broadcast();
    }
  }
  
  private void saveImage(File file, long timeStamp, String jobId) throws IOException {
    GridFSInputFile image = blobService.create(file);
    PageList<SuiteReport> suites = suiteReportService.query(new BasicDBObject("jobId", jobId));
    while (suites.hasNext()) {
      for (SuiteReport suite : suites.next()) {
        List<CaseReportReference> cases = suite.getCases();
        
        for (CaseReportReference ref : cases) {
          CaseReport caseReport = ref.get();
          List<StepReportReference> steps = caseReport.getSteps();
          for (StepReportReference step : steps) {
            StepReport report = step.get();
            if (timeStamp == report.getStartTime()) {
              image.put("timestamp", report.getStartTime());
              image.put("step_report_id", report.getId());
            }
          }
        }
      }
    }
    
    blobService.save(image);
  }
  
  private void extractTarFile(String path, String destFolderPath) throws IOException {
    //read tar file into tarachiveinputstream
    TarArchiveInputStream tarFile = new TarArchiveInputStream(new GzipCompressorInputStream(new BufferedInputStream(new FileInputStream(path))));
    //read individual tar file
    TarArchiveEntry entry = null;
    int offset;
    FileOutputStream outputFile = null;
    File dest = new File(destFolderPath);
    
    if (!dest.exists()) {
      dest.mkdirs();
    }
    
    while ((entry = tarFile.getNextTarEntry()) != null) {
      File file = null;
      String name = entry.getName();
      if (name.contains(".png") && name.contains("error_")) {
        name = entry.getName();
        int index = name.lastIndexOf("/");
        name = name.substring(index);
        file = new File(destFolderPath+name);
        byte[] content = new byte [(int) entry.getSize()];
        offset = 0;
        tarFile.read(content, offset, content.length - offset);
        // define output stream for writing the file
        outputFile = new FileOutputStream(file);
        outputFile.write(content);
        outputFile.close();
      }
      
    }
    
    tarFile.close();
  }
     
  private void doExecuteKeywordJob(KeywordJob job, KeywordProject project) throws Exception {
    VMachine jenkinsVM = vmachineService.getSystemVM(project.getTenant(), project.getSpace());
    BasicDBObject options = job.getOptions();
    String browser = options.getString("browser");
    boolean isWindows = "ie".equals(browser);
    
    VMachine testVM = job.getTestVMachineId() == null ? 
        (isWindows ? vmachineService.findOneWindowsAvailable(project.getTenant(), project.getSpace()) : 
          vmachineService.getTestVMAvailabel(project.getTenant(), project.getSpace(), true)) : 
          vmachineService.get(job.getTestVMachineId());

    if (testVM == null) {
      updateLog(job, "None VM available.");
      updateLog(job, "Creating new VM (about 4-8 minutes).... ");
      
      //We will create new test vm async
      testVM = iaasProvider.get().createTestVMAsync(project.getTenant(), project.getSpace(), true, isWindows);
      updateLog(job, "Created new VM " + testVM);
      
      job.setVMachineId(testVM.getId());
      executorService.update(job);
      
      //And broadcast that the job has served a vm
      Event event  = eventFactory.create(job, "keyword-job-tracking");
      event.broadcast();
      return;
    }
    
    if (testVM.getStatus() == VMachine.Status.Initializing) {
      Thread.sleep(5000);
      updateLog(job, "Waiting Test VM intializing...");
      Event event  = eventFactory.create(job, "keyword-job-tracking");
      event.broadcast();
      return;
    } else if (testVM.getStatus() == VMachine.Status.Started) {
      updateLog(job, "Generating keyword project");
      String path = generatorService.generateKeyword("/tmp", job.getId(), true, job.getSuites(), project.getValueDelay(), project.getVersionSelenium());

      if(testVM.getPublicIp() == null) {
        testVM = iaasProvider.get().allocateFloatingIp(testVM);
        vmachineService.update(testVM);
        Thread.sleep(15 * 1000);
        SSHClient.checkEstablished(testVM.getPublicIp(), 22, 300);
        logger.log(Level.INFO, "Connection to  " + testVM.getPublicIp() + " is established");
      }
      SSHClient.sendFile(testVM.getPublicIp(), 22, "cloudats", "#CloudATS", "/home/cloudats/projects", job.getId() + ".zip", new File(path));
      
      Thread.sleep(3000);
      
      updateLog(job, "Uploading to VM Test");
      SSHClient.execCommand(testVM.getPublicIp(), 22, "cloudats", "#CloudATS", 
          "cd /home/cloudats/projects && unzip " +  job.getId() + ".zip", null, null);

      StringBuilder goalsBuilder = new StringBuilder("clean test");

      JenkinsMaster jenkinsMaster = new JenkinsMaster(jenkinsVM.getPublicIp(), "http", "/jenkins", 8080);
      JenkinsMavenJob jenkinsJob = new JenkinsMavenJob(jenkinsMaster, job.getId(), 
          testVM.getPrivateIp() , (isWindows ? "C:/cygwin64/home/cloudats/projects/" : "/home/cloudats/projects/") + job.getId() + "/pom.xml", goalsBuilder.toString());

      jenkinsJob.submit();
      updateLog(job, "Submitted Jenkins job");

      testVM.setStatus(VMachine.Status.InProgress);
      vmachineService.update(testVM);

      job.setStatus(Status.Running);
      job.setVMachineId(testVM.getId());
      executorService.update(job);
      
      Event event  = eventFactory.create(job, "keyword-job-tracking");
      event.broadcast();
    }
  }
  
  private void processUploadJob(SeleniumUploadJob job) throws Exception {
    try {
      SeleniumUploadProject project = keywordUploadService.get(job.getProjectId(),"raw");
      switch (job.getStatus()) {
      case Queued:
        doExecuteUploadJob(job, project);
        break;
      case Running:
        doTrackingUploadJob(job, project);
        break;
      default:
        break;
      }
    } catch (Exception e) {
      job.setStatus(Status.Completed);
      executorService.update(job);
      
      VMachine vm = vmachineService.get(job.getTestVMachineId());
      if (vm != null) {
        vm.setStatus(VMachine.Status.Started);
        if (vm.getPublicIp() != null) iaasProvider.get().deallocateFloatingIp(vm);
        vmachineService.update(vm);
      }
      
      SeleniumUploadProject project = keywordUploadService.get(job.getProjectId(),"raw");
      project.setStatus(SeleniumUploadProject.Status.READY);
      keywordUploadService.update(project);
      
      //bubble event
      Event event = eventFactory.create(job, "upload-job-tracking");
      event.broadcast();
      throw e;
    }
  }
  
private void doTrackingUploadJob(SeleniumUploadJob job, SeleniumUploadProject project) throws Exception {
    TenantReference tenant = project.getTenant();
    SpaceReference space = project.getSpace();
    
    VMachine systemVM = vmachineService.getSystemVM(tenant, space);
    VMachine testVM = vmachineService.get(job.getTestVMachineId());
    
    JenkinsMaster jkMaster = new JenkinsMaster(systemVM.getPublicIp(), "http", "jenkins", 8080);
    JenkinsMavenJob jkJob = cache.get(job.getId());
    if (jkJob == null) {
      jkJob = new JenkinsMavenJob(jkMaster, job.getId(), null, null, null);
      cache.put(job.getId(), jkJob);
    }
    
    boolean isBuilding = jkJob.isBuilding(1, System.currentTimeMillis(), 60 * 1000);
    if (isBuilding) {
      
      updateLog(job, jkJob);
      
      Thread.sleep(5000L);
      
      Event event = eventFactory.create(job, "upload-job-tracking");
      event.broadcast();
      
    } else {
      updateLog(job, jkJob);
      
      //Download result
      
      //Zip report
      SSHClient.execCommand(testVM.getPublicIp(), 22, "cloudats", "#CloudATS", 
          "cd /home/cloudats/projects/"+job.getId()+" && tar -czvf target.tar.gz -C target .", null, null);
      ByteArrayOutputStream bosReport = new ByteArrayOutputStream();
      SSHClient.getFile(testVM.getPublicIp(), 22, "cloudats", "#CloudATS", 
          "/home/cloudats/projects/" + job.getId() + "/target.tar.gz",  bosReport);
      
      if (bosReport.size() > 0) {
        job.put("raw_report", bosReport.toByteArray());
        job.put("result", jkJob.getStatus(1));
      }
      //End download result

      job.setStatus(AbstractJob.Status.Completed);
      executorService.update(job);
      
      //Reset vm status and release floating ip
      testVM.setStatus(VMachine.Status.Started);
      vmachineService.update(testVM);
      
      project.setStatus(SeleniumUploadProject.Status.READY);
      keywordUploadService.update(project);
      
      jkJob.delete();
      cache.remove(job.getId());
      
      Event event = eventFactory.create(job, "upload-job-tracking");
      event.broadcast();
    }
  }
  
  private void doExecuteUploadJob(SeleniumUploadJob job, SeleniumUploadProject project) throws Exception {

    VMachine jenkinsVM = vmachineService.getSystemVM(project.getTenant(), project.getSpace());
    VMachine testVM = job.getTestVMachineId() == null ? 
        vmachineService.getTestVMAvailabel(project.getTenant(), project.getSpace(), true) : vmachineService.get(job.getTestVMachineId());
        
    if (testVM == null) {
      updateLog(job, "None VM available.");
      updateLog(job, "Creating new VM (about 4-8 minitues).... ");
      
      //We will create new test vm async
      testVM = iaasProvider.get().createTestVMAsync(project.getTenant(), project.getSpace(), true, false);
      updateLog(job, "Created new VM " + testVM);
      
      job.setVMachineId(testVM.getId());
      executorService.update(job);
      
      //And broadcast that the job has served a vm
      Event event  = eventFactory.create(job, "upload-job-tracking");
      event.broadcast();
      return;
    }
    
    if (testVM.getStatus() == VMachine.Status.Initializing) {
      Thread.sleep(5000);
      updateLog(job, "Waiting Test VM intializing...");
      Event event  = eventFactory.create(job, "upload-job-tracking");
      event.broadcast();
      return;
    }
    
    if (testVM.getPublicIp() == null) {
      testVM = iaasProvider.get().allocateFloatingIp(testVM);
      vmachineService.update(testVM);
      Thread.sleep(15 * 1000);
      SSHClient.checkEstablished(testVM.getPublicIp(), 22, 300);
      logger.log(Level.INFO, "Connection to  " + testVM.getPublicIp() + " is established");
    } else if (testVM.getStatus() == VMachine.Status.Started) {
      byte [] bFile = project.getRawData();
      String fileName = job.getId();
      String path = "/tmp/"+fileName+".zip";
      FileOutputStream fileOut = new FileOutputStream(path);
      fileOut.write(bFile);
      fileOut.close();
      
      SSHClient.sendFile(testVM.getPublicIp(), 22, "cloudats", "#CloudATS", "/home/cloudats/projects/"+fileName, fileName + ".zip", new File(path));
      Thread.sleep(3000);
      updateLog(job, "Uploading to VM Test");

      SSHClient.execCommand(testVM.getPublicIp(), 22, "cloudats", "#CloudATS", 
          "cd /home/cloudats/projects/"+fileName+" && unzip " +  fileName + ".zip", null, null);
      StringBuilder goalsBuilder = new StringBuilder("clean test");

      JenkinsMaster jenkinsMaster = new JenkinsMaster(jenkinsVM.getPublicIp(), "http", "/jenkins", 8080);
      JenkinsMavenJob jenkinsJob = new JenkinsMavenJob(jenkinsMaster, job.getId(), testVM.getPrivateIp() , "/home/cloudats/projects/" + fileName + "/pom.xml", goalsBuilder.toString());
      jenkinsJob.submit();
      updateLog(job, "Submitted Jenkins job");
      
      testVM.setStatus(VMachine.Status.InProgress);
      vmachineService.update(testVM);

      job.setStatus(Status.Running);
      job.setVMachineId(testVM.getId());
      executorService.update(job);
      
      Event event = eventFactory.create(job, "upload-job-tracking");
      event.broadcast();
    }
  }
  
  private void updateLog(AbstractJob<?> job, String log) {
    StringBuilder sb = new StringBuilder("[").append(new Date()).append("]").append("[INFO] ");
    job.appendLog(sb.append(log).append("\n").toString());
    executorService.update(job);
  }
  
  private void updateLog(AbstractJob<?> job, JenkinsMavenJob jkJob) throws IOException {
    int start = job.getLog() == null ? 0 : job.getLog().length();
    if (job.getLog() != null) {
      int _index = job.getLog().indexOf("Submitted Jenkins job");
      if (_index != -1) {
        _index += "Submitted Jenkins job\n".length();
        start -= _index;
      }
    }
    byte[] bytes = jkJob.getConsoleOutput(1, start); 
    byte[] next = new byte[bytes.length - start];
    System.arraycopy(bytes, start, next, 0, next.length);
    if (next.length > 0) {
      job.appendLog(new String(next));
      executorService.update(job);
    }
  }
  
  @SuppressWarnings("unused")
  private void updateLogToFile(AbstractJob<?> job, JenkinsMavenJob jkJob) throws IOException {
    int start = job.getLog() == null ? 0 : job.getLog().length();
    if (job.getLog() != null) {
      int _index = job.getLog().indexOf("Submitted Jenkins job");
      if (_index != -1) {
        _index += "Submitted Jenkins job\n".length();
        start -= _index;
      }
    }
    byte[] bytes = jkJob.getConsoleOutput(1, start); 
    byte[] next = new byte[bytes.length - start];
    System.arraycopy(bytes, start, next, 0, next.length);
    
    GridFSDBFile file = new GridFSDBFile();
    if (next.length > 0) {
      job.appendLog(new String(next));
      executorService.update(job);
    }
  }
  
  
}
