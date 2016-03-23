/**
 * 
 */
package org.ats.services.iaas;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ats.common.PageList;
import org.ats.common.ssh.SSHClient;
import org.ats.jenkins.JenkinsMaster;
import org.ats.jenkins.JenkinsSlave;
import org.ats.services.data.MongoDBService;
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

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.Session;
import com.microsoft.windowsazure.Configuration;
import com.microsoft.windowsazure.core.utils.KeyStoreType;
import com.microsoft.windowsazure.management.compute.ComputeManagementClient;
import com.microsoft.windowsazure.management.compute.ComputeManagementService;
import com.microsoft.windowsazure.management.compute.models.ConfigurationSet;
import com.microsoft.windowsazure.management.compute.models.ConfigurationSetTypes;
import com.microsoft.windowsazure.management.compute.models.InputEndpoint;
import com.microsoft.windowsazure.management.compute.models.RoleInstance;
import com.microsoft.windowsazure.management.compute.models.RoleInstanceStatus;
import com.microsoft.windowsazure.management.compute.models.VirtualMachineCreateParameters;
import com.microsoft.windowsazure.management.compute.models.VirtualMachineRoleSize;
import com.microsoft.windowsazure.management.configuration.ManagementConfiguration;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Feb 15, 2016
 */
@Singleton
public class AzureService implements IaaSService {

  @Inject
  private Logger logger;

  @Inject
  private VMachineService vmachineService;

  @Inject
  private VMachineFactory vmachineFactory;

  @Inject 
  private EventFactory eventFactory;

  private String keystorePath;

  private String keystorePassword;

  private String endpoint;

  private String subscriptionId;

  private String serviceName;

  private String deploymentName;

  private String availabilitySet;

  private String systemImage;

  private String uiImage;

  private String nonUIImage;
  
  private String windowsImage;

  private DBCollection col;

  @Inject
  AzureService(
      @Named("org.ats.cloud.azure.subscriptionId") String subscriptionId,
      @Named("org.ats.cloud.azure.keystore.path") String keystorePath,
      @Named("org.ats.cloud.azure.keystore.pwd") String keystorePassword,
      @Named("org.ats.cloud.azure.endpoint") String endpoint,
      @Named("org.ats.cloud.azure.service.name") String serviceName,
      @Named("org.ats.cloud.azure.deployment.name") String deploymentName,
      @Named("org.ats.cloud.azure.availabilitySet") String availabilitySet,
      @Named("org.ats.cloud.azure.image.system") String systemImage,
      @Named("org.ats.cloud.azure.image.ui") String uiImage,
      @Named("org.ats.cloud.azure.image.nonui") String nonUIImage,
      @Named("org.ats.cloud.azure.image.windows") String windowsImage,
      MongoDBService mongo) {

    this.keystorePath = keystorePath;
    this.keystorePassword = keystorePassword;
    this.endpoint = endpoint;
    this.subscriptionId = subscriptionId;
    this.serviceName = serviceName;
    this.deploymentName = deploymentName;
    this.availabilitySet = availabilitySet;
    this.systemImage = systemImage;
    this.uiImage = uiImage;
    this.nonUIImage = nonUIImage;
    this.windowsImage = windowsImage;
    this.col = mongo.getDatabase().getCollection("azure-public-port");
  }

  private Configuration createDefaultConfiguration() throws IOException, URISyntaxException {
    return ManagementConfiguration.configure(new URI(endpoint), subscriptionId, keystorePath, keystorePassword, KeyStoreType.jks);
  }

  private ComputeManagementClient getComputeClient() throws IOException, URISyntaxException {
    return ComputeManagementService.create(createDefaultConfiguration());
  }

  @Override
  public void initTenant(TenantReference tenant) throws InitializeTenantException, CreateVMException {
    createSystemVM(tenant, null);
    logger.log(Level.INFO, "Created tenant " + tenant.getId());
  }

  @Override
  public void destroyTenant(TenantReference tenant) throws DestroyTenantException, DestroyVMException {
    try {
      PageList<VMachine> page = vmachineService.query(new BasicDBObject("tenant", tenant.toJSon()));
      while (page.hasNext()) {
        List<VMachine> list = page.next();
        for (VMachine vm : list) {
          destroy(vm);
        }
      }
      logger.log(Level.INFO, "Destroyed tenant " + tenant.getId());
    } catch (Exception e) {
      e.printStackTrace();
      throw new DestroyTenantException(e.getMessage());
    }
  }

  private String generateVMName(boolean system, boolean hasUI,TenantReference tenant, SpaceReference space) {
    StringBuilder sb = new StringBuilder(tenant.getId()).append(system ? "-system" : "").append(hasUI ? "-ui-" : "-nonui-");
    sb.append(space == null ? "public" : space.getId());
    sb.append("-").append(UUID.randomUUID().toString().substring(0, 8));
    return  sb.toString();
  }

  private int getAvailablePublicPort() {
    int from = 1024;
    int port = from + new Random().nextInt(20000);
    if (this.col.findOne(new BasicDBObject("_id", port)) == null) {
      this.col.insert(new BasicDBObject("_id", port));
      return port;
    }
    else return getAvailablePublicPort();
  }

  private RoleInstance getVMByName(String vmName) throws Exception {
    for (RoleInstance instance : getComputeClient().getDeploymentsOperations().getByName(serviceName, deploymentName).getRoleInstances()) {
      if(instance.getRoleName().equals(vmName)) return instance;
    }
    //
    return null;
  }

  private VMachine waitUntilInstanceRunning(VMachine vm) throws InterruptedException {
    while (!isVMReady(vm)) {
      Thread.sleep(5000);
    }
    vm = vmachineService.get(vm.getId(), "remote_url");
    return vm;
  }

  @Override
  public VMachine createSystemVMAsync(TenantReference tenant, SpaceReference space) throws CreateVMException {
    try {
      logger.log(Level.INFO, "Creating system vm for tenant " + tenant.getId());

      VirtualMachineCreateParameters params = new VirtualMachineCreateParameters();
      params.setRoleName(generateVMName(true, false, tenant, space));
      params.setRoleSize(VirtualMachineRoleSize.MEDIUM);
      params.setVMImageName(this.systemImage);

      ArrayList<ConfigurationSet> configSetList = new ArrayList<ConfigurationSet>();
      ConfigurationSet configSet = new ConfigurationSet();
      configSet.setConfigurationSetType(ConfigurationSetTypes.NETWORKCONFIGURATION);
      InputEndpoint inputEndpoint = new InputEndpoint();

      int remotePort = getAvailablePublicPort();

      inputEndpoint.setPort(remotePort);
      inputEndpoint.setName("remote");
      inputEndpoint.setLocalPort(8081);
      inputEndpoint.setProtocol("tcp");

      ArrayList<InputEndpoint> endpoints = new ArrayList<InputEndpoint>();
      endpoints.add(inputEndpoint);

      configSet.setInputEndpoints(endpoints);
      configSetList.add(configSet);

      params.setConfigurationSets(configSetList);
      params.setAvailabilitySetName(this.availabilitySet);

      logger.info("Requesting Azure to create new instance");
      getComputeClient().getVirtualMachinesOperations().create(serviceName, deploymentName, params);

      Thread.sleep(5000); //sleep 5s to request stable

      RoleInstance roleInstance = getVMByName(params.getRoleName());
      VMachine vm = vmachineFactory
          .create(params.getRoleName(), tenant, space, true, false,  false,
              roleInstance.getIPAddress().getHostAddress(),
              roleInstance.getIPAddress().getHostAddress(), 
              Status.Initializing);
      vm.put("remote_url", serviceName + ".cloudapp.net:" + remotePort);

      vmachineService.create(vm);
      logger.info("Created VM " + vm);

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
  public VMachine createSystemVM(TenantReference tenant, SpaceReference space) throws CreateVMException {
    VMachine vm = createSystemVMAsync(tenant, space);

    logger.info("Waiting for instance's state is running");
    try {
      vm = waitUntilInstanceRunning(vm);
      return initSystemVM(vm);
    } catch (InterruptedException e) {
      e.printStackTrace();
      return null;
    }
  }

  @Override
  public VMachine createTestVM(TenantReference tenant, SpaceReference space, boolean hasUI, boolean isWindows) throws CreateVMException {

    VMachine vm = createTestVMAsync(tenant, space, hasUI, isWindows);

    logger.info("Waiting for instance's state is running");
    try {
      vm = waitUntilInstanceRunning(vm);
      if (hasUI) {
        return initTestVmUI(vm);
      } else {
        return initTestVMNonUI(vm);
      }
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  @Override
  public VMachine createTestVMAsync(TenantReference tenant, SpaceReference space, boolean hasUI, boolean isWindows) throws CreateVMException {
    try {
      VirtualMachineCreateParameters params = new VirtualMachineCreateParameters();
      params.setRoleName(generateVMName(false, hasUI, tenant, space));
      params.setRoleSize(VirtualMachineRoleSize.SMALL);
      params.setVMImageName(hasUI ? this.uiImage : this.nonUIImage);

      ArrayList<ConfigurationSet> configSetList = new ArrayList<ConfigurationSet>();
      ConfigurationSet configSet = new ConfigurationSet();
      configSet.setConfigurationSetType(ConfigurationSetTypes.NETWORKCONFIGURATION);
      InputEndpoint inputEndpoint = new InputEndpoint();

      int remotePort = getAvailablePublicPort();

      inputEndpoint.setPort(remotePort);
      inputEndpoint.setName("remote");
      inputEndpoint.setLocalPort(22);
      inputEndpoint.setProtocol("tcp");

      ArrayList<InputEndpoint> endpoints = new ArrayList<InputEndpoint>();
      endpoints.add(inputEndpoint);

      configSet.setInputEndpoints(endpoints);
      configSetList.add(configSet);

      params.setConfigurationSets(configSetList);
      params.setAvailabilitySetName(this.availabilitySet);

      logger.info("Requesting Azure to create new instance");
      getComputeClient().getVirtualMachinesOperations().create(serviceName, deploymentName, params);

      Thread.sleep(5000); //sleep 5s to request stable

      RoleInstance roleInstance = getVMByName(params.getRoleName());
      VMachine vm = vmachineFactory
          .create(params.getRoleName(), tenant, space, false, hasUI,  isWindows,
              roleInstance.getIPAddress().getHostAddress(),
              roleInstance.getIPAddress().getHostAddress(), 
              Status.Initializing);

      vmachineService.create(vm);
      logger.info("Created VM " + vm);

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
  public VMachine start(VMachine machine) throws StartVMException {
    return machine;
  }

  @Override
  public VMachine stop(VMachine machine) throws StopVMException {
    return machine;
  }

  @Override
  public VMachine rebuild(VMachine machine) throws RebuildVMException {
    return machine;
  }

  @Override
  public void destroy(VMachine machine) throws DestroyVMException {
    try {
      getComputeClient().getVirtualMachinesOperations().delete(this.serviceName, this.deploymentName, machine.getId(), true);
      vmachineService.delete(machine.getId());
      logger.log(Level.INFO, "Destroyed vm " + machine.getId());
    } catch (Exception e) {
      e.printStackTrace();
      throw new DestroyVMException(e.getMessage());
    }
  }

  @Override
  public VMachine allocateFloatingIp(VMachine vm) {
    return vm;
  }

  @Override
  public VMachine deallocateFloatingIp(VMachine vm) {
    return vm;
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
    try {
      RoleInstance roleInstance = getVMByName(vm.getId());
      return RoleInstanceStatus.READYROLE.equals(roleInstance.getInstanceStatus());
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
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
        if (!new JenkinsSlave(jenkinsMaster, vm.getPrivateIp(), false).join(5 * 60 * 1000)) throw new CreateVMException("Can not create jenkins slave for test vm");
        logger.info("Created Jenkins slave by " + vm);

      } catch (Exception e) {
        CreateVMException ex = new CreateVMException("The vm test can not join jenkins master");
        ex.setStackTrace(e.getStackTrace());
        throw ex;
      }

      //register Guacamole vnc
      try {
        registerGuacamole(vm, systemVM, System.currentTimeMillis(), 5 * 60 * 1000);
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

      waitJMeterServerRunning(vm, System.currentTimeMillis(), 5 * 60 * 1000);
      logger.info("JMeter service is ready");

      vm.setStatus(VMachine.Status.Started);
      vmachineService.update(vm);
      logger.info("Updated VM " + vm);

      return vm;
    } else {
      throw new CreateVMException("Connection to VM can not established");
    }
  }
  
  private void registerGuacamole(VMachine vm, VMachine systemVM, long start, long timeout) throws Exception {
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
      Thread.sleep(5000);
      if (System.currentTimeMillis() - start > timeout) throw e;
      else registerGuacamole(vm, systemVM, start, timeout);
    }
  }

  private void waitJMeterServerRunning(VMachine vm, long start, long timeout) throws Exception {
    try {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      Channel channel = SSHClient.execCommand(vm.getPublicIp(), 22, "cloudats", "#CloudATS", "service jmeter-2.13 status", null, null);
      SSHClient.write(bos, channel);
      String status = new String(bos.toByteArray());
      logger.info("JMeter Server is " + status);
      
      bos.close();
      channel.disconnect();
      
      if (!"Running".equals(status.trim())) {
        Thread.sleep(5000);
        waitJMeterServerRunning(vm, start, timeout);
      }
    } catch (Exception e) {
      e.printStackTrace();
      if (System.currentTimeMillis() - start > timeout) throw e;
      else {
        Thread.sleep(5000);
        waitJMeterServerRunning(vm, start, timeout);
      }
    }
  }
}
