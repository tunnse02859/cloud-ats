/**
 * 
 */
package org.ats.services.iaas;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
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
import org.ats.services.organization.TenantService;
import org.ats.services.organization.entity.reference.SpaceReference;
import org.ats.services.organization.entity.reference.TenantReference;
import org.ats.services.vmachine.VMachine;
import org.ats.services.vmachine.VMachine.Status;
import org.ats.services.vmachine.VMachineFactory;
import org.ats.services.vmachine.VMachineService;
import org.jclouds.ContextBuilder;
import org.jclouds.logging.slf4j.config.SLF4JLoggingModule;
import org.jclouds.openstack.keystone.v2_0.KeystoneApi;
import org.jclouds.openstack.keystone.v2_0.domain.Role;
import org.jclouds.openstack.keystone.v2_0.domain.Tenant;
import org.jclouds.openstack.keystone.v2_0.domain.User;
import org.jclouds.openstack.keystone.v2_0.extensions.RoleAdminApi;
import org.jclouds.openstack.keystone.v2_0.extensions.TenantAdminApi;
import org.jclouds.openstack.keystone.v2_0.extensions.UserAdminApi;
import org.jclouds.openstack.keystone.v2_0.options.CreateTenantOptions;
import org.jclouds.openstack.keystone.v2_0.options.CreateUserOptions;
import org.jclouds.openstack.neutron.v2.NeutronApi;
import org.jclouds.openstack.neutron.v2.domain.ExternalGatewayInfo;
import org.jclouds.openstack.neutron.v2.domain.FloatingIP;
import org.jclouds.openstack.neutron.v2.domain.FloatingIP.CreateFloatingIP;
import org.jclouds.openstack.neutron.v2.domain.Network;
import org.jclouds.openstack.neutron.v2.domain.Network.CreateNetwork;
import org.jclouds.openstack.neutron.v2.domain.Router;
import org.jclouds.openstack.neutron.v2.domain.Router.CreateRouter;
import org.jclouds.openstack.neutron.v2.domain.Rule.CreateBuilder;
import org.jclouds.openstack.neutron.v2.domain.Rule.CreateRule;
import org.jclouds.openstack.neutron.v2.domain.RuleDirection;
import org.jclouds.openstack.neutron.v2.domain.RuleEthertype;
import org.jclouds.openstack.neutron.v2.domain.RuleProtocol;
import org.jclouds.openstack.neutron.v2.domain.SecurityGroup;
import org.jclouds.openstack.neutron.v2.domain.Subnet;
import org.jclouds.openstack.neutron.v2.domain.Subnet.CreateSubnet;
import org.jclouds.openstack.neutron.v2.extensions.FloatingIPApi;
import org.jclouds.openstack.neutron.v2.extensions.RouterApi;
import org.jclouds.openstack.neutron.v2.extensions.SecurityGroupApi;
import org.jclouds.openstack.neutron.v2.features.NetworkApi;
import org.jclouds.openstack.nova.v2_0.NovaApi;
import org.jclouds.openstack.nova.v2_0.domain.Address;
import org.jclouds.openstack.nova.v2_0.domain.Server;
import org.jclouds.openstack.nova.v2_0.domain.ServerCreated;
import org.jclouds.openstack.nova.v2_0.features.FlavorApi;
import org.jclouds.openstack.nova.v2_0.features.ImageApi;
import org.jclouds.openstack.nova.v2_0.features.ServerApi;
import org.jclouds.openstack.nova.v2_0.options.CreateServerOptions;
import org.jclouds.openstack.v2_0.domain.Resource;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Jul 2, 2015
 */
@Singleton
class OpenStackService implements IaaSService {
  
  private String keystoneURL, adminKeystoneEndpoint, keystoneProvider, neutronProvider, novaProvider, externalNetwork, defaultRole;
  
  private String systemImage, uiImage, nonUIImage, windowsImage;
  
  private DBCollection col;
  
  @Inject
  private TenantService tenantService;
  
  @Inject
  private VMachineService vmachineService;
  
  @Inject
  private VMachineFactory vmachineFactory;
  
  @Inject 
  private EventFactory eventFactory;
  
  @Inject
  private Logger logger;
  
  @Inject
  OpenStackService(@Named("org.ats.cloud.openstack.keystone.url") String keystoneURL, 
      @Named("org.ats.cloud.openstack.keystone.admin") String adminKeystoneEndpoint,
      @Named("org.ats.cloud.openstack.keystone.provider") String keystoneProvider,
      @Named("org.ats.cloud.openstack.neutron.provider") String neutronProvider,
      @Named("org.ats.cloud.openstack.nova.provider") String novaProvider,
      @Named("org.ats.cloud.openstack.externalnetwork") String externalNetwork,
      @Named("org.ats.cloud.openstack.defaultrole") String defaultRole,
      @Named("org.ats.cloud.openstack.image.system") String systemImage,
      @Named("org.ats.cloud.openstack.image.ui") String uiImage,
      @Named("org.ats.cloud.openstack.image.nonui") String nonUIImage,
      @Named("org.ats.cloud.openstack.image.windows") String windowsImage,
      MongoDBService mongo) {
    
    this.keystoneURL = keystoneURL;
    this.adminKeystoneEndpoint = adminKeystoneEndpoint;
    this.keystoneProvider = keystoneProvider;
    this.neutronProvider = neutronProvider;
    this.novaProvider = novaProvider;
    this.externalNetwork = externalNetwork;
    this.defaultRole = defaultRole;
    
    this.systemImage = systemImage;
    this.uiImage = uiImage;
    this.nonUIImage = nonUIImage;
    this.windowsImage = windowsImage;
    
    this.col = mongo.getDatabase().getCollection("openstack-identity");
  }
  
  @Override
  public void addCredential(String tenant, String username, String password) {
    String identity = tenant + ":" + username;
    BasicDBObject obj = new BasicDBObject("_id", tenant).append("identity", identity).append("password", password);
    this.col.remove(obj);
    this.col.insert(obj);
  }
  
  @Override
  public void addCredential(String tenant) {
    String identity = tenant + ":" + tenant; 
    String password = tenant;
    BasicDBObject obj = new BasicDBObject("_id", tenant).append("identity", identity).append("password", password);
    this.col.remove(obj);
    this.col.insert(obj);
  }
  
  /** 
   * Initialize OpenStack system for new tenant
   */
  @Override
  public void initTenant(TenantReference tenantRef) throws InitializeTenantException, CreateVMException {
    addCredential(tenantRef.getId());
    org.ats.services.organization.entity.Tenant atsTenant = tenantRef.get();
    
    KeystoneApi keystoneApi = createKeystoneAPI("admin");
    Optional<? extends TenantAdminApi> tenantAdminApiExtension = keystoneApi.getTenantAdminApi();
    
    if (tenantAdminApiExtension.isPresent()) {

      //Create tenant on openstack
      TenantAdminApi tenantAdminApi = tenantAdminApiExtension.get();
      CreateTenantOptions tenantOptions = CreateTenantOptions.Builder.description(tenantRef.getId()).enabled(true);
      Tenant tenant = tenantAdminApi.create(tenantRef.getId(), tenantOptions);
      atsTenant.put("tenant_id", tenant.getId());
      logger.log(Level.INFO, "Created tenant " + tenantRef.getId());
      
      
      Optional<? extends UserAdminApi> userAdminApiExtension = keystoneApi.getUserAdminApi();
      if (userAdminApiExtension.isPresent()) {
        UserAdminApi userAdminApi = userAdminApiExtension.get();
        CreateUserOptions userOptions = CreateUserOptions.Builder
            .tenant(tenant.getId()).email("admin@" + tenantRef.getId()).enabled(true);

        //Create user on openstack and add credential to database
        User user = userAdminApi.create(tenantRef.getId(), tenantRef.getId(), userOptions);
        Optional<? extends RoleAdminApi> roleApiExtension = keystoneApi.getRoleAdminApi();
        if (roleApiExtension.isPresent()) {
          RoleAdminApi roleAdminApi = roleApiExtension.get();
          String roleId = null;
          for(Role role : roleAdminApi.list()) {
            if (defaultRole.equals(role.getName())) {
              roleId = role.getId();
              break;
            }
          }
          if (roleId == null) {
            throw new InitializeTenantException("The default role did not exist. Contact system administrator");
          }
          tenantAdminApi.addRoleOnTenant(tenant.getId(), user.getId(), roleId);
          addCredential(tenantRef.getId());
          atsTenant.put("user_id", user.getId());
          logger.log(Level.INFO, "Created user's tenant " + tenantRef.getId());
          
          //Create tenant's network on openstack
          NeutronApi neutronAPI = createNeutronAPI(tenantRef.getId());
          String region = neutronAPI.getConfiguredRegions().iterator().next();
          NetworkApi networkAPI = neutronAPI.getNetworkApi(region); 
          Network network = networkAPI.create(CreateNetwork.createBuilder(tenantRef.getId() + "-network").build());
          atsTenant.put("network_id", network.getId());
          logger.log(Level.INFO, "Created network for tenant " + tenantRef.getId());
          
          CreateSubnet createSubnet = CreateSubnet
              .createBuilder(network.getId(), "192.168.1.0/24")
              .ipVersion(4)
              .name(tenantRef.getId() + "-subnet")
              .dnsNameServers(ImmutableSet.<String>of("8.8.8.8", "8.8.4.4"))
              .build();
          Subnet subnet = neutronAPI.getSubnetApi(region).create(createSubnet);
          atsTenant.put("subnet_id", subnet.getId());
          logger.log(Level.INFO, "Created subnet for tenant " + tenantRef.getId());
          
          String externalNetworkId = null;
          for (Network sel : networkAPI.list().concat()) {
            if (externalNetwork.equals(sel.getName())) {
              externalNetworkId = sel.getId();
              break;
            }
          }
          if (externalNetworkId == null) {
            throw new InitializeTenantException("Can not create a tenant without external network. Contact system administrator");
          }
          
          //Add security group
          updateDefaultSecurityGroup(tenantRef);
          logger.log(Level.INFO, "Update security group for tenant " + tenantRef.getId());
          
          //Create tenant's router
          Optional<? extends RouterApi> routerApiExtension = neutronAPI.getRouterApi(region);
          if(routerApiExtension.isPresent()){
            RouterApi routerApi = routerApiExtension.get();
            ExternalGatewayInfo externalGateway = ExternalGatewayInfo.builder().networkId(externalNetworkId).build();
            Router router = routerApi.create(CreateRouter.createBuilder()
                .name(tenantRef.getId() + "-router").adminStateUp(true)                                       
                .externalGatewayInfo(externalGateway)
                .build());

            routerApi.addInterfaceForSubnet(router.getId(), subnet.getId());
            atsTenant.put("router_id", router.getId());
            tenantService.update(atsTenant);
            logger.log(Level.INFO, "Created router for tenant " + tenantRef.getId());
            
            //Create system vm for public space
            createSystemVM(tenantRef, null);
            logger.log(Level.INFO, "Created system vm for public space on tenant " + tenantRef.getId());
          } else{
            throw new InitializeTenantException("Can not create a tenant without router. Contact system administrator");
          }
          
        } else {
          throw new InitializeTenantException("Can not create a tenant without role. Contact system administrator");
        }
        
      } else {
        throw new InitializeTenantException("Can not create a tenant without admin user. Contact system administrator");
      }
      
    } else {
      throw new InitializeTenantException("Can not create a tenant without admin tenant. Contact system administrator");
    }
  }
  
  @Override
  public void destroyTenant(TenantReference tenantRef) throws DestroyTenantException, DestroyVMException {
   org.ats.services.organization.entity.Tenant atsTenant = tenantService.get(tenantRef.getId(), "tenant_id", "user_id", "network_id", "subnet_id", "router_id"); 

   NeutronApi neutronAPI = createNeutronAPI("admin");
   String region = neutronAPI.getConfiguredRegions().iterator().next();
   NetworkApi networkAPI = neutronAPI.getNetworkApi(region);
   
   //Destroy vm of tenant
   PageList<VMachine> page = vmachineService.query(new BasicDBObject("tenant", tenantRef.toJSon()));
   while (page.hasNext()) {
     List<VMachine> list = page.next();
     for (VMachine vm : list) {
       if (vm.isSystem()) {
         deallocateFloatingIp(vm);
       }
       destroy(vm);
     }
   }
   
 //Destroy router of tenant
   Optional<? extends RouterApi> routerApiExtension = neutronAPI.getRouterApi(region);
   if(routerApiExtension.isPresent()){
     RouterApi routerApi = routerApiExtension.get();
     routerApi.removeInterfaceForSubnet(atsTenant.getString("router_id"), atsTenant.getString("subnet_id"));
     
     routerApi.delete(atsTenant.getString("router_id"));
     atsTenant.put("router_id", null);
     atsTenant.put("subnet_id", null);
   }
   
   //destroy network
   networkAPI.delete(atsTenant.getString("network_id"));
   atsTenant.put("network_id", null);
   
   //release floating ip
   neutronAPI = createNeutronAPI(tenantRef.getId());
   FloatingIPApi floatingIpApi = neutronAPI.getFloatingIPApi(region).get();
   for (FloatingIP ip : floatingIpApi.list().concat()) {
     floatingIpApi.delete(ip.getId());
   }
   
   //Destroy tenant and user
   KeystoneApi keystoneApi = createKeystoneAPI("admin");
   Optional<? extends TenantAdminApi> tenantAdminApiExtension = keystoneApi.getTenantAdminApi();
   
   if (tenantAdminApiExtension.isPresent()) {
     TenantAdminApi tenantAdminApi = tenantAdminApiExtension.get();
     tenantAdminApi.delete(atsTenant.getString("tenant_id"));
     atsTenant.put("tenant_id", null);
     
     Optional<? extends UserAdminApi> userAdminApiExtension = keystoneApi.getUserAdminApi();
     if (userAdminApiExtension.isPresent()) {
       UserAdminApi userAdminApi = userAdminApiExtension.get();
       userAdminApi.delete(atsTenant.getString("user_id"));
       atsTenant.put("user_id", null);
       
       tenantService.update(atsTenant);
       
     } else {
       throw new DestroyTenantException("Can not destroy a tenant without admin user. Contact system administrator");
     }
   } else {
     throw new DestroyTenantException("Can not destroy a tenant without admin tenant. Contact system administrator");
   }
  }

  @Override
  public VMachine start(VMachine machine) throws StartVMException {
    NovaApi novaApi = createNovaApi(machine.getTenant().getId());
    ServerApi serverApi = novaApi.getServerApi(novaApi.getConfiguredRegions().iterator().next());
    serverApi.start(machine.getId());
    Server server = null;
    do {
      server = serverApi.get(machine.getId());
    } while(!server.getStatus().equals(Server.Status.ACTIVE));

    try {
      if (!machine.isSystem()) 
        machine = allocateFloatingIp(machine);
      
      Thread.sleep(60 * 1000); //sleep 1m to connection established
      
      if (SSHClient.checkEstablished(machine.getPublicIp(), 22, 300)) {
        logger.log(Level.INFO, "Connection to  " + machine.getPublicIp() + " is established");
        
        if (!machine.isSystem())
          machine = deallocateFloatingIp(machine);
        
        machine.setStatus(VMachine.Status.Started);
//        vmachineService.update(machine);
        return machine;
      } else {
        throw new StartVMException("Cannot connect to vm " + machine.getPrivateIp()); 
      }
    } catch (Exception e) {
      StartVMException ex = new StartVMException("Cannot connect to vm " + machine.getPrivateIp());
      ex.setStackTrace(e.getStackTrace());
      throw ex;
    }
    
  }

  @Override
  public VMachine stop(VMachine machine) throws StopVMException {
    NovaApi novaApi = createNovaApi(machine.getTenant().getId());
    ServerApi serverApi = novaApi.getServerApi(novaApi.getConfiguredRegions().iterator().next());
    serverApi.stop(machine.getId());
    Server server = null;
    do {
      server = serverApi.get(machine.getId());
    } while(!server.getStatus().equals(Server.Status.SHUTOFF));
    machine.setStatus(VMachine.Status.Stopped);
    vmachineService.update(machine);
    return machine;
  }

  @Override
  public VMachine rebuild(VMachine machine) throws RebuildVMException {
    throw new RebuildVMException("Unsupported operation");
  }

  @Override
  public void destroy(VMachine machine) throws DestroyVMException {
    NovaApi novaApi = createNovaApi(machine.getTenant().getId());
    ServerApi serverApi = novaApi.getServerApi(novaApi.getConfiguredRegions().iterator().next());
    serverApi.delete(machine.getId());
    Server server = null;
    do {
      server = serverApi.get(machine.getId());
    }
    while (server != null);
    vmachineService.delete(machine);
  }

  @Override
  public VMachine allocateFloatingIp(VMachine vm) {
    if (vm.getPublicIp() != null) return vm;
    
    NovaApi novaApi = createNovaApi(vm.getTenant().getId());
    String region = novaApi.getConfiguredRegions().iterator().next();
    
    FloatingIP floatingIp = getFloatingIPAvailable(vm.getTenant());
    org.jclouds.openstack.nova.v2_0.extensions.FloatingIPApi floatingIpApi = novaApi.getFloatingIPApi(region).get();
    floatingIpApi.addToServer(floatingIp.getFloatingIpAddress(), vm.getId());
    
    vm.setPublicIp(floatingIp.getFloatingIpAddress());
    logger.log(Level.INFO, "Associate a floating ip " + vm.getPublicIp() + " with fixed ip " + vm.getPrivateIp());
    return vm;
  }
  
  @Override
  public VMachine deallocateFloatingIp(VMachine vm) {
    
    if (vm.getPublicIp() == null) return vm;
    
    NovaApi novaApi = createNovaApi(vm.getTenant().getId());
    NeutronApi neutronApi = createNeutronAPI(vm.getTenant().getId());
    
    String region = novaApi.getConfiguredRegions().iterator().next();
    org.jclouds.openstack.nova.v2_0.extensions.FloatingIPApi floatingIpApi = novaApi.getFloatingIPApi(region).get();
    floatingIpApi.removeFromServer(vm.getPublicIp(), vm.getId());
    
    FloatingIPApi neutronFloatingApi = neutronApi.getFloatingIPApi(region).get();
    for (FloatingIP floatingIp : neutronFloatingApi.list().concat()) {
      if (vm.getPublicIp().equals(floatingIp.getFloatingIpAddress())) {
        neutronFloatingApi.delete(floatingIp.getId());
        break;
      }
    }
    vm.setPublicIp(null);
    vmachineService.update(vm);
    logger.log(Level.INFO, "Deallocated floating ip on test vm " + vm.getPrivateIp());
    return vm;
  }
  
  @Override
  public VMachine createSystemVM(TenantReference tenant, SpaceReference space) throws CreateVMException {
    String flavorId = getFlavorIdByName(tenant, "m1.small");
    String imageId = getImageIdByName(tenant, systemImage);
    return createVM(imageId, flavorId, true, false, false, tenant, space);
  }
  
  @Override
  public VMachine createSystemVMAsync(TenantReference tenant, SpaceReference space) throws CreateVMException {
    return createSystemVM(tenant, space);
  }

  @Override
  public VMachine createTestVM(TenantReference tenant, SpaceReference space, boolean hasUI, boolean isWindows) throws CreateVMException {
    String flavorId = getFlavorIdByName(tenant, isWindows ? "m1.medium" : "m1.small");
    String imageId = null;
    if (isWindows) {
      imageId = getImageIdByName(tenant, windowsImage);
    } else if (hasUI) {
      imageId = getImageIdByName(tenant, uiImage);
    } else {
      imageId = getImageIdByName(tenant, nonUIImage);
    }
    return createVM(imageId, flavorId, false, hasUI, isWindows, tenant, space);
  }
  
  @Override
  public VMachine createTestVMAsync(TenantReference tenant, SpaceReference space, boolean hasUI, boolean isWindows) throws CreateVMException {

    String flavorId = getFlavorIdByName(tenant, isWindows ? "m1.medium" : "m1.small");
    String imageId = null;
    if (isWindows) {
      imageId = getImageIdByName(tenant, windowsImage);
    } else if (hasUI) {
      imageId = getImageIdByName(tenant, uiImage);
    } else {
      imageId = getImageIdByName(tenant, nonUIImage);
    }
    VMachine vm = createVMAsync(imageId, flavorId, false, hasUI, isWindows, tenant, space);
    
    Event event = eventFactory.create(vm, "initialize-vm");
    event.broadcast();
    return vm;
  }
  
  private VMachine createVMAsync(String imageId, String flavorId, boolean system, boolean hasUI, boolean isWindows,TenantReference tenant, SpaceReference space) throws CreateVMException {
    NovaApi novaApi = createNovaApi(tenant.getId());
    String region = novaApi.getConfiguredRegions().iterator().next();
    ServerApi serverApi = novaApi.getServerApi(region);
    
    StringBuilder sb = new StringBuilder(tenant.getId()).append(system ? "-system" : "").append(hasUI ? "-ui-" : "-nonui-");
    sb.append(space == null ? "public" : space.getId());
    String serverName = sb.toString();
    sb.setLength(0);

    sb.append("#cloud-config").append("\n");
    if (hasUI || system) {
      sb.append("hostname: ").append(serverName).append("\n");
      sb.append("fqdn: ").append(serverName).append(".cloud-ats.org").append("\n");
      sb.append("manage_etc_hosts: true").append("\n");
    }
    
    logger.info("Requesting OpenStack to create new instance");
    ServerCreated serverCreated = serverApi.create(serverName, imageId, flavorId, new CreateServerOptions().userData(sb.toString().getBytes()));
    
    VMachine vm = vmachineFactory.create(serverCreated.getId(), tenant, space, system, hasUI, isWindows, null, null, Status.Initializing);
    vmachineService.create(vm);
    logger.info("Created VM " + vm);
    
    return vm;
  }
  
  private VMachine createVM(String imageId, String flavorId, boolean system, boolean hasUI, boolean isWindows, TenantReference tenant, SpaceReference space) throws CreateVMException {

    VMachine vm = createVMAsync(imageId, flavorId, system, hasUI, isWindows, tenant, space);
    
   vm = waitUntilInstanceRunning(vm);
   
   try {
     if (system) {
       return initSystemVM(vm);
     } else if (hasUI) {
       return initTestVmUI(vm);
     } else {
       return initTestVMNonUI(vm);
     }
   } catch (Exception e) {
     CreateVMException ex = new CreateVMException("Can not initialize vm " + vm);
     ex.setStackTrace(e.getStackTrace());
     throw ex;
   }
  }
  
  @Override
  public boolean isVMReady(VMachine vm) {
    NovaApi novaApi = createNovaApi(vm.getTenant().getId());
    String region = novaApi.getConfiguredRegions().iterator().next();
    ServerApi serverApi = novaApi.getServerApi(region);
    Server server = serverApi.get(vm.getId());
    
    if( server.getStatus().equals(Server.Status.ACTIVE)) {
      Address address = server.getAddresses().entries().iterator().next().getValue();
      vm.setPrivateIp(address.getAddr());
      vmachineService.update(vm);
      return true;
    }
    
    return false;
  }

  @Override
  public VMachine initSystemVM(VMachine vm) throws CreateVMException {
    //create floating ip for vm
    vm = allocateFloatingIp(vm);
    
    JenkinsMaster jenkinsMaster = new JenkinsMaster(vm.getPublicIp(), "http", "jenkins", 8080);
    try {
      if (SSHClient.checkEstablished(vm.getPublicIp(), 22, 300)) {
        
        jenkinsMaster.isReady(5 * 60 * 1000);
        logger.info("Jenkins service is ready");

        vm.setStatus(VMachine.Status.Started);
        vmachineService.update(vm);
        logger.info("Updated VM " + vm);

        return vm;
      } else {
        throw new CreateVMException("Connection to VM can not established");
      }
    } catch (Exception e) {
      CreateVMException ex = new CreateVMException("The jenkins vm test can not start properly");
      ex.setStackTrace(e.getStackTrace());
      throw ex;
    }
  }

  @Override
  public VMachine initTestVmUI(VMachine vm) throws Exception {
    //create floating ip for vm
    vm = allocateFloatingIp(vm);
    
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
    
    boolean isWindows = vm.isWindows();
    
    if (SSHClient.checkEstablished(vm.getPublicIp(), 22, 300)) {
      logger.log(Level.INFO, "Connection to  " + vm.getPublicIp() + " is established");
      
      if (!isWindows) {
        try {
          if (!new JenkinsSlave(jenkinsMaster, vm.getPrivateIp(), false).join(5 * 60 * 1000)) throw new CreateVMException("Can not create jenkins slave for test vm");
          logger.info("Created Jenkins slave by " + vm);
          
        } catch (Exception e) {
          CreateVMException ex = new CreateVMException("The vm test can not join jenkins master");
          ex.setStackTrace(e.getStackTrace());
          throw ex;
        }
      } else {
        JenkinsSlave winSlave = new JenkinsSlave(jenkinsMaster, vm.getPrivateIp(), true);
        try {
          winSlave.join(5 * 1000);
        } catch (Exception e) {
          //No problem
        }
        //TODO: that is trick to reload jenkins slave configuration
        String cmd = "/cygdrive/c/jenkin_slave/change_jenkinconfig.sh " + systemVM.getPrivateIp() + " " + vm.getPrivateIp();
        Session session = SSHClient.getSession(vm.getPublicIp(), 22, "cloudats", "#CloudATS");              
        ChannelExec channel = (ChannelExec) session.openChannel("exec");
        channel.setCommand(cmd);
        channel.connect();
        
        while(true) {
          if (channel.isClosed()) {
            logger.info("Register Windows Jenkins Slave exit code: " + channel.getExitStatus());
            break;
          }
        }
        
        channel.disconnect();
        session.disconnect();
        logger.info("Registered Windows Jenkins slave.");
        //The vm will be restart
        
        if (SSHClient.checkEstablished(vm.getPublicIp(), 22, 300)) {
          logger.log(Level.INFO, "Connection to  " + vm.getPublicIp() + " is established");
          //wait jenkins slave available
          String fetchUrl = jenkinsMaster.buildURL(new StringBuilder("computer/").append(vm.getPrivateIp()).append("/api/json").toString());
          try {
            if (!winSlave.waitJenkinsSlaveJoinUntil(System.currentTimeMillis(), 10 * 60 * 1000, fetchUrl)) throw new CreateVMException("Can not create jenkins slave for test vm");
            logger.info("Created Jenkins slave by " + vm);
            
          } catch (Exception e) {
            CreateVMException ex = new CreateVMException("The vm test can not join jenkins master");
            ex.setStackTrace(e.getStackTrace());
            throw ex;
          }
        } else {
          throw new CreateVMException("Connection to VM can not established");
        }
      }
      
    //register Guacamole vnc
      try {
        String command = "sudo -S -p '' /etc/guacamole/manage_con.sh " + vm.getPrivateIp() + (isWindows ? " 3389 " : " 5900 ") + "'#CloudATS' "  + (isWindows ? "rdp" : "vnc")  +  " 0";
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
  public VMachine initTestVMNonUI(VMachine vm) throws CreateVMException {
    //create floating ip for vm
    vm = allocateFloatingIp(vm);
    try {
      if (SSHClient.checkEstablished(vm.getPublicIp(), 22, 300)) {
        logger.log(Level.INFO, "Connection to  " + vm.getPublicIp() + " is established");

        waitJMeterServerRunning(vm);
        logger.info("JMeter service is ready");
        
        vm = deallocateFloatingIp(vm);
        
        vm.setStatus(VMachine.Status.Started);
        vmachineService.update(vm);
        logger.info("Updated VM " + vm);
        
        return vm;
      } else {
        throw new CreateVMException("Connection to VM can not established");
      }
    } catch (Exception e) {
      CreateVMException ex = new CreateVMException("Can not init test vm non ui " + vm);
      ex.setStackTrace(e.getStackTrace());
      throw ex;
    } finally {
      vm = deallocateFloatingIp(vm);
    }
  }
  
  private VMachine waitUntilInstanceRunning(VMachine vm) {
    try {
      while (!isVMReady(vm)) {
        Thread.sleep(5000);
      }
      //update vm model
      vm = vmachineService.get(vm.getId());
      return vm;
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }
  
  private KeystoneApi createKeystoneAPI(String tenant) {
    Iterable<Module> modules = ImmutableSet.<Module> of(new SLF4JLoggingModule());
    
    BasicDBObject credential = getCredential(tenant);
    String identity= credential.getString("identity");
    String password = credential.getString("password");
    
    return ContextBuilder
        .newBuilder(keystoneProvider)
        .credentials(identity, password)
        .endpoint(adminKeystoneEndpoint).modules(modules).buildApi(KeystoneApi.class);
  }
  
  private NeutronApi createNeutronAPI(String tenant) {
    Iterable<Module> modules = ImmutableSet.<Module> of(new SLF4JLoggingModule());
    
    BasicDBObject credential = getCredential(tenant);
    String identity= credential.getString("identity");
    String password = credential.getString("password");
    
    return ContextBuilder
        .newBuilder(neutronProvider)
        .credentials(identity, password)
        .endpoint(keystoneURL)
        .modules(modules)
        .buildApi(NeutronApi.class);
  }
  
  private NovaApi createNovaApi(String tenant) {
    Iterable<Module> modules = ImmutableSet.<Module> of(new SLF4JLoggingModule());
    
    BasicDBObject credential = getCredential(tenant);
    String identity= credential.getString("identity");
    String password = credential.getString("password");
    
    return ContextBuilder
        .newBuilder(novaProvider)
        .credentials(identity, password)
        .endpoint(keystoneURL)
        .modules(modules)
        .buildApi(NovaApi.class);
  }
  
  private BasicDBObject getCredential(String tenant) {
    return (BasicDBObject) this.col.findOne(new BasicDBObject("_id", tenant));
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
  
  private String getFlavorIdByName(TenantReference tenantRef, String flavorName) {
    NovaApi novaApi = createNovaApi(tenantRef.getId());
    FlavorApi flavorApi = novaApi.getFlavorApi(novaApi.getConfiguredRegions().iterator().next());
    for (Resource resource : flavorApi.list().concat()) {
      if (flavorName.equals(resource.getName())) return resource.getId();
    }
    return null;
  }
  
  private String getImageIdByName(TenantReference tenantRef, String imageName) {
    NovaApi novaApi = createNovaApi(tenantRef.getId());
    ImageApi imageApi = novaApi.getImageApi(novaApi.getConfiguredRegions().iterator().next());
    for (Resource resource : imageApi.list().concat()) {
      if (imageName.equals(resource.getName())) return resource.getId();
    }
    return null;
  }
  
  private FloatingIP getFloatingIPAvailable(TenantReference tenant) {
    NeutronApi neutronApi = createNeutronAPI(tenant.getId());
    NovaApi novaApi = createNovaApi(tenant.getId());
    
    String region = novaApi.getConfiguredRegions().iterator().next();
    
    FloatingIPApi floatingIpApi = neutronApi.getFloatingIPApi(region).get();
    
    for (FloatingIP sel : floatingIpApi.list().concat()) {
      if (sel.getFixedIpAddress() == null) {
        return sel;
      }
    }
    
    NetworkApi networkApi = neutronApi.getNetworkApi(region);

    String externalNetworkId = null;
    for (Network sel : networkApi.list().concat()) {
      if (externalNetwork.equals(sel.getName())) {
        externalNetworkId = sel.getId();
        break;
      }
    }
    
    return floatingIpApi.create(CreateFloatingIP.createBuilder(externalNetworkId).build());
  }
  
  private void updateDefaultSecurityGroup(TenantReference tenantRef) {
    NovaApi novaApi = createNovaApi(tenantRef.getId());
    NeutronApi neutronApi = createNeutronAPI(tenantRef.getId());
    Optional<SecurityGroupApi> optional = neutronApi.getSecurityGroupApi(novaApi.getConfiguredRegions().iterator().next());
    if (optional.isPresent()) {
      SecurityGroupApi securityGroupApi = optional.get();
      SecurityGroup defaultGroup = securityGroupApi.listSecurityGroups().concat().get(0);

      //create icmp 
      CreateBuilder createBuilder = CreateRule.createBuilder(RuleDirection.INGRESS, defaultGroup.getId());
      createBuilder.ethertype(RuleEthertype.IPV4);
      createBuilder.protocol(RuleProtocol.ICMP);
      createBuilder.portRangeMin(0);
      createBuilder.portRangeMax(255);
      securityGroupApi.create(createBuilder.build());

      //create port 22
      createBuilder = CreateRule.createBuilder(RuleDirection.INGRESS, defaultGroup.getId());
      createBuilder.ethertype(RuleEthertype.IPV4);
      createBuilder.protocol(RuleProtocol.TCP);
      createBuilder.portRangeMin(22);
      createBuilder.portRangeMax(22);
      securityGroupApi.create(createBuilder.build());

      //create port 80
      createBuilder = CreateRule.createBuilder(RuleDirection.INGRESS, defaultGroup.getId());
      createBuilder.ethertype(RuleEthertype.IPV4);
      createBuilder.protocol(RuleProtocol.TCP);
      createBuilder.portRangeMin(80);
      createBuilder.portRangeMax(80);
      securityGroupApi.create(createBuilder.build());

      //create port 443
      createBuilder = CreateRule.createBuilder(RuleDirection.INGRESS, defaultGroup.getId());
      createBuilder.ethertype(RuleEthertype.IPV4);
      createBuilder.protocol(RuleProtocol.TCP);
      createBuilder.portRangeMin(443);
      createBuilder.portRangeMax(443);
      securityGroupApi.create(createBuilder.build());

      //create port 1099
      createBuilder = CreateRule.createBuilder(RuleDirection.INGRESS, defaultGroup.getId());
      createBuilder.ethertype(RuleEthertype.IPV4);
      createBuilder.protocol(RuleProtocol.TCP);
      createBuilder.portRangeMin(1099);
      createBuilder.portRangeMax(1099);
      securityGroupApi.create(createBuilder.build());

      //create port 5900
      createBuilder = CreateRule.createBuilder(RuleDirection.INGRESS, defaultGroup.getId());
      createBuilder.ethertype(RuleEthertype.IPV4);
      createBuilder.protocol(RuleProtocol.TCP);
      createBuilder.portRangeMin(5900);
      createBuilder.portRangeMax(5900);
      securityGroupApi.create(createBuilder.build());

      //create port 8080
      createBuilder = CreateRule.createBuilder(RuleDirection.INGRESS, defaultGroup.getId());
      createBuilder.ethertype(RuleEthertype.IPV4);
      createBuilder.protocol(RuleProtocol.TCP);
      createBuilder.portRangeMin(8080);
      createBuilder.portRangeMax(8080);
      securityGroupApi.create(createBuilder.build());

      //create port 8081
      createBuilder = CreateRule.createBuilder(RuleDirection.INGRESS, defaultGroup.getId());
      createBuilder.ethertype(RuleEthertype.IPV4);
      createBuilder.protocol(RuleProtocol.TCP);
      createBuilder.portRangeMin(8081);
      createBuilder.portRangeMax(8081);
      securityGroupApi.create(createBuilder.build());
    }
  }
}
