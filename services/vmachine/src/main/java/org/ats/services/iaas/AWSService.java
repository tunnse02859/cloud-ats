/**
 * 
 */
package org.ats.services.iaas;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ats.common.PageList;
import org.ats.common.ssh.SSHClient;
import org.ats.jenkins.JenkinsMaster;
import org.ats.jenkins.JenkinsSlave;
import org.ats.services.event.Event;
import org.ats.services.event.EventFactory;
import org.ats.services.iaas.exception.CreateVMException;
import org.ats.services.iaas.exception.DestroyTenantException;
import org.ats.services.iaas.exception.DestroyVMException;
import org.ats.services.iaas.exception.InitializeTenantException;
import org.ats.services.iaas.exception.RebuildVMException;
import org.ats.services.iaas.exception.StartVMException;
import org.ats.services.iaas.exception.StopVMException;
import org.ats.services.organization.entity.reference.SpaceReference;
import org.ats.services.organization.entity.reference.TenantReference;
import org.ats.services.vmachine.VMachine;
import org.ats.services.vmachine.VMachine.Status;
import org.ats.services.vmachine.VMachineFactory;
import org.ats.services.vmachine.VMachineService;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.DescribeInstanceStatusRequest;
import com.amazonaws.services.ec2.model.DescribeInstanceStatusResult;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceStateChange;
import com.amazonaws.services.ec2.model.InstanceStatus;
import com.amazonaws.services.ec2.model.InstanceStatusDetails;
import com.amazonaws.services.ec2.model.InstanceType;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.services.ec2.model.TerminateInstancesResult;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.mongodb.BasicDBObject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Sep 11, 2015
 */
@Singleton
class AWSService implements IaaSService {
 
  @Inject
  private Logger logger;
  
  @Inject
  private VMachineService vmachineService;
  
  @Inject
  private VMachineFactory vmachineFactory;
  
  @Inject 
  private EventFactory eventFactory;
  
  private String systemImage;
  
  private String uiImage;
  
  private String nonUIImage;
  
  private String securityGroup;
  
  private String subnet;
  
  private AmazonEC2Client client;
  
  @Inject
  AWSService(@Named("org.ats.cloud.aws.secret") final String secretKey,
      @Named("org.ats.cloud.aws.access") final String accessKey,
      @Named("org.ats.cloud.aws.endpoint") String endpoint,
      @Named("org.ats.cloud.aws.image.system") String systemImage,
      @Named("org.ats.cloud.aws.image.ui") String uiImage,
      @Named("org.ats.cloud.aws.image.nonui") String nonUIImage,
      @Named("org.ats.cloud.aws.securitygroup") String securityGroup,
      @Named("org.ats.cloud.aws.subnet") String subnet) {
    this.systemImage = systemImage;
    this.uiImage = uiImage;
    this.nonUIImage = nonUIImage;
    this.securityGroup = securityGroup;
    this.subnet = subnet;
    this.client = new AmazonEC2Client(new AWSCredentials() {
      
      @Override
      public String getAWSSecretKey() {
        return secretKey;
      }
      
      @Override
      public String getAWSAccessKeyId() {
        return accessKey;
      }
    });
    client.setEndpoint(endpoint);
  }

  @Override
  public void initTenant(TenantReference tenantRef) throws InitializeTenantException, CreateVMException {
    createSystemVM(tenantRef, null);
    logger.log(Level.INFO, "Created tenant " + tenantRef.getId());
  }

  @Override
  public void destroyTenant(TenantReference tenant) throws DestroyTenantException, DestroyVMException {
    PageList<VMachine> page = vmachineService.query(new BasicDBObject("tenant", tenant.toJSon()));
    List<String> vmIds = new ArrayList<String>();
    while (page.hasNext()) {
      List<VMachine> list = page.next();
      for (VMachine vm : list) {
        vmIds.add(vm.getId());
      }
    }
    
    TerminateInstancesRequest request = new TerminateInstancesRequest();
    request.withInstanceIds(vmIds);
    
    TerminateInstancesResult result = client.terminateInstances(request);
    for (InstanceStateChange instance : result.getTerminatingInstances()) {
      vmachineService.delete(instance.getInstanceId());
    }
    
    logger.log(Level.INFO, "Destroyed tenant " + tenant.getId());
  }

  @Override
  public VMachine createSystemVM(TenantReference tenant, SpaceReference space) throws CreateVMException {
    try {
      logger.log(Level.INFO, "Creating system vm for tenant " + tenant.getId());
      return createVM(systemImage, InstanceType.T2Small, true, false, tenant, space);
    } catch (Exception e) {
      CreateVMException ex = new CreateVMException(e.getMessage());
      ex.setStackTrace(e.getStackTrace());
      throw ex;
    }
  }
  
  @Override
  public VMachine createSystemVMAsync(TenantReference tenant, SpaceReference space) throws CreateVMException {
    return createSystemVM(tenant, space);
  }

  @Override
  public VMachine createTestVM(TenantReference tenant, SpaceReference space, boolean hasUI) throws CreateVMException {
    String imageId = hasUI ? uiImage : nonUIImage;
    try {
      return createVM(imageId, InstanceType.T2Small, false, hasUI, tenant, space);
    } catch (Exception e) {
      CreateVMException ex = new CreateVMException(e.getMessage());
      ex.setStackTrace(e.getStackTrace());
      throw ex;
    }
  }
  
  @Override
  public VMachine createTestVMAsync(TenantReference tenant, SpaceReference space, boolean hasUI) throws CreateVMException {
    String imageId = hasUI ? uiImage : nonUIImage;
    try {
      VMachine vm = createVMAsync(imageId, InstanceType.T2Small, false, hasUI, tenant, space);
      Event event = eventFactory.create(vm, "initialize-vm");
      event.broadcast();
      return vm;
    } catch (Exception e) {
      CreateVMException ex = new CreateVMException(e.getMessage());
      ex.setStackTrace(e.getStackTrace());
      throw ex;
    }
  }

  @Override
  public VMachine start(VMachine vm) throws StartVMException {
    return vm;
  }

  @Override
  public VMachine stop(VMachine vm) throws StopVMException {
    return vm;
  }

  @Override
  public VMachine rebuild(VMachine vm) throws RebuildVMException {
    return vm;
  }

  @Override
  public void destroy(VMachine vm) throws DestroyVMException {
    TerminateInstancesRequest request = new TerminateInstancesRequest().withInstanceIds(vm.getId());
    client.terminateInstances(request);
    vmachineService.delete(vm);
  }

  @Override
  public VMachine allocateFloatingIp(VMachine vm) {
    vm.setPublicIp(vm.getPrivateIp());
    return vm;
  }

  @Override
  public VMachine deallocateFloatingIp(VMachine vm) {
    vm.setPublicIp(null);
    return vm;
  }
  
  private VMachine createVMAsync(String imageId, InstanceType instanceType, boolean system, boolean hasUI,TenantReference tenant, SpaceReference space) throws CreateVMException, InterruptedException {
    StringBuilder sb = new StringBuilder(tenant.getId()).append(system ? "-system" : "").append(hasUI ? "-ui-" : "-nonui-");
    sb.append(space == null ? "public" : space.getId());
    String serverName = sb.toString();
    
    RunInstancesRequest runInstanceRequest = new RunInstancesRequest()
      .withInstanceType(instanceType)
      .withImageId(imageId)
      .withMinCount(1)
      .withMaxCount(1)
      .withSecurityGroupIds(securityGroup)
      .withSubnetId(subnet);
    
    VMachine vm = null;
    logger.info("Requesting AWS to create new instance");
    RunInstancesResult runInstances = client.runInstances(runInstanceRequest);
    
    Thread.sleep(5000); //sleep 5s to request stable
    
    for (Instance instance : runInstances.getReservation().getInstances()) {
      vm = vmachineFactory.create(instance.getInstanceId(), tenant, space, system, hasUI,  null, instance.getPrivateIpAddress(), Status.Initializing);
      
      CreateTagsRequest createTagRequest = new CreateTagsRequest();
      createTagRequest.withResources(vm.getId()).withTags(
          new Tag("Name", serverName),
          new Tag("Tenant", tenant.getId()),
          new Tag("Owner", "HaiNT21"),
          new Tag("Project", "CloudATS"),
          new Tag("Purpose", "CloudATS Demo"));
      
      logger.info("Creating tags for instance");
      client.createTags(createTagRequest);
    }
    
    if (vm == null) throw new CreateVMException("Can not create AWS EC2 Instance");
    
    vmachineService.create(vm);
    logger.info("Created VM " + vm);
    
    return vm;
  }
  
  @Override
  public VMachine initSystemVM(VMachine vm) throws CreateVMException {
    JenkinsMaster jenkinsMaster = new JenkinsMaster(vm.getPublicIp(), "http", "jenkins", 8080);
    try {
      jenkinsMaster.isReady(5 * 60 * 1000);
      logger.info("Jenkins service is ready");
      
      vm.setStatus(VMachine.Status.Started);
      vmachineService.update(vm);
      logger.info("Updated VM " + vm);
      
      return vm;
    } catch (Exception e) {
      CreateVMException ex = new CreateVMException("The jenkins vm test can not start properly");
      ex.setStackTrace(e.getStackTrace());
      throw ex;
    }
  }
  
  @Override
  public VMachine initTestVmUI(VMachine vm) throws Exception {
    VMachine systemVM = vmachineService.getSystemVM(vm.getTenant(), vm.getSpace());
    if (systemVM == null) throw new CreateVMException("Can not create test vm without system vm in space " + vm.getSpace());
    
    JenkinsMaster jenkinsMaster = new JenkinsMaster(systemVM.getPublicIp(), "http", "jenkins", 8080);
    
    try {
      jenkinsMaster.isReady(5 * 60 * 1000);
    } catch (Exception e) {
      CreateVMException ex = new CreateVMException("The jenkins vm test can not start properly");
      ex.setStackTrace(e.getStackTrace());
      throw ex;
    }
    
    if (SSHClient.checkEstablished(vm.getPublicIp(), 22, 300)) {
      logger.log(Level.INFO, "Connection to  " + vm.getPublicIp() + " is established");
      
      try {
        if (!new JenkinsSlave(jenkinsMaster, vm.getPrivateIp()).join(5 * 60 * 1000)) throw new CreateVMException("Can not create jenkins slave for test vm");
        logger.info("Created Jenkins slave by " + vm);
        
      } catch (Exception e) {
        CreateVMException ex = new CreateVMException("The vm test can not join jenkins master");
        ex.setStackTrace(e.getStackTrace());
        throw ex;
      }
      
    //register Guacamole vnc
      try {
        String command = "sudo -S -p '' /etc/guacamole/manage_con.sh " + vm.getPrivateIp() + " 5900 '#CloudATS' vnc 0";
        Session session = SSHClient.getSession(systemVM.getPublicIp(), 22, "cloudats", "#CloudATS");              
        ChannelExec channel = (ChannelExec) session.openChannel("exec");
        channel.setCommand(command);
        
        OutputStream out = channel.getOutputStream();
        channel.connect();
        
        out.write("#CloudATS\n".getBytes());
        out.flush();
        
        while(true) {
          if (channel.isClosed()) {
            logger.info("Register guacamole exit code: " + channel.getExitStatus());
            break;
          }
        }
        
        channel.disconnect();
        session.disconnect();
        logger.info("Registered guacamole node");
      } catch (Exception e) {
        CreateVMException ex = new CreateVMException("Can not register Guacamole node");
        ex.setStackTrace(e.getStackTrace());
        throw ex;
      }
      
      vm.setStatus(VMachine.Status.Started);
      vmachineService.update(vm);
      logger.info("Updated VM " + vm);
      
      return vm;
    } else {
      throw new CreateVMException("Connection to VM can not established");
    }
  }
  
  @Override
  public VMachine initTestVMNonUI(VMachine vm) throws Exception {
    if (SSHClient.checkEstablished(vm.getPublicIp(), 22, 300)) {
      logger.log(Level.INFO, "Connection to  " + vm.getPublicIp() + " is established");

      waitJMeterServerRunning(vm);
      logger.info("JMeter service is ready");
      
      vm.setStatus(VMachine.Status.Started);
      vmachineService.update(vm);
      logger.info("Updated VM " + vm);
      
      return vm;
    } else {
      throw new CreateVMException("Connection to VM can not established");
    }
  }
  
  private VMachine createVM(String imageId, InstanceType instanceType, boolean system, boolean hasUI,TenantReference tenant, SpaceReference space) throws Exception {
    
    VMachine vm = createVMAsync(imageId, instanceType, system, hasUI, tenant, space);
    
    logger.info("Waiting for instance's state is running");
    vm = waitUntilInstanceRunning(vm);

    if (system) {
      return initSystemVM(vm);
    } else if (hasUI) {
      return initTestVmUI(vm);
    } else {
      return initTestVMNonUI(vm);
    }
  }
  
  private VMachine waitUntilInstanceRunning(VMachine vm) throws InterruptedException {
    while (!isVMReady(vm)) {
      Thread.sleep(5000);
    }
    vm = vmachineService.get(vm.getId());
    return vm;
  }
  
  private void waitJMeterServerRunning(VMachine vm) throws JSchException, IOException, InterruptedException {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    Channel channel = SSHClient.execCommand(vm.getPublicIp(), 22, "cloudats", "#CloudATS", "service jmeter-2.13 status", null, null);
    SSHClient.write(bos, channel);
    String status = new String(bos.toByteArray());
    logger.info("JMeter Server is " + status);
    if (!"Running".equals(status.trim())) {
      Thread.sleep(5000);
      waitJMeterServerRunning(vm);
    }
  }

  @Override
  public void addCredential(String tenant, String username, String password) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void addCredential(String tenant) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public boolean isVMReady(VMachine vm) {
    DescribeInstancesRequest request = new DescribeInstancesRequest().withInstanceIds(vm.getId());
    DescribeInstancesResult result = client.describeInstances(request);
    Instance instance = result.getReservations().get(0).getInstances().get(0);
    if (instance.getState().getCode() == 16) {
      boolean instanceStatus = false;
      boolean systemStatus = false;
      
      DescribeInstanceStatusRequest statusRequest = new DescribeInstanceStatusRequest().withInstanceIds(vm.getId());
      DescribeInstanceStatusResult statusResult = client.describeInstanceStatus(statusRequest);
      
      for (InstanceStatus status :  statusResult.getInstanceStatuses()) {
        //Check system status
        for (InstanceStatusDetails systemDetail : status.getSystemStatus().getDetails()) {
          if (systemDetail.getName().equals("reachability") && systemDetail.getStatus().equals("passed")) {
            systemStatus = true;
            break;
          }
        }
        
        //Check instance status
        for (InstanceStatusDetails instanceDetail : status.getInstanceStatus().getDetails()) {
          if (instanceDetail.getName().equals("reachability") && instanceDetail.getStatus().equals("passed")) {
            instanceStatus = true;
            break;
          }
        }
      }
      
      if (systemStatus && instanceStatus) {
        vm.setPublicIp(instance.getPublicIpAddress());
        vmachineService.update(vm);
        logger.info("Update public ip for VM " + vm);
        return true;
      }
    }
    return false;
  }
}
