package controllers;

import org.ats.common.PageList;
import org.ats.services.OrganizationContext;
import org.ats.services.organization.RoleService;
import org.ats.services.organization.SpaceService;
import org.ats.services.organization.UserService;
import org.ats.services.organization.acl.Authenticated;
import org.ats.services.organization.entity.Role;
import org.ats.services.organization.entity.Space;
import org.ats.services.organization.entity.Tenant;
import org.ats.services.organization.entity.fatory.PermissionFactory;
import org.ats.services.organization.entity.fatory.ReferenceFactory;
import org.ats.services.organization.entity.fatory.RoleFactory;
import org.ats.services.organization.entity.reference.SpaceReference;
import org.ats.services.organization.entity.reference.TenantReference;

import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import actions.CorsComposition;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.inject.Inject;
import com.mongodb.BasicDBObject;


@CorsComposition.Cors
@Authenticated
public class RoleController extends Controller{
	@Inject
	OrganizationContext context;

	@Inject
	UserService userService;

	@Inject
	RoleService roleService;
	
	@Inject
	SpaceService spaceService;

	@Inject
	ReferenceFactory<TenantReference> tenantReference;
	
	@Inject
	ReferenceFactory<SpaceReference> spaceReference;
	
	@Inject
	RoleFactory roleFactory;
	
	@Inject
	PermissionFactory permissionFactory;
	
	public Result list() {
		ArrayNode array = Json.newObject().arrayNode();
		PageList<Role> roles = roleService.list();
		while(roles.hasNext()){
			for (Role role : roles.next()) {
				array.add(Json.parse(role.toString()));
			}
		}
		return ok(array);
	}
	
	public Result get(String roleId) {
		
		Role role = roleService.get(roleId);
		return ok(Json.parse(role.toString()));
	}


	public Result create() {
		
		JsonNode node = request().body().asJson();
		String roleName = node.get("name").asText();
		String spaceId = node.get("spaceId").asText();
		Tenant tenant = context.getTenant();
		Role role = roleFactory.create(roleName);
		role.setSpace(spaceReference.create(spaceId));
		
		boolean tenantView = node.get("permissions").get("viewSpaces").asBoolean();
		if (tenantView) {
			role.addPermission(permissionFactory.create("tenant:view_spaces@" + tenant.getId() + ":" + spaceId));
		}
		
		boolean tenantManage = node.get("permissions").get("manageSpaces").asBoolean();
		if (tenantManage) {
			role.addPermission(permissionFactory.create("tenant:manage_spaces@" + tenant.getId() + ":" + spaceId));
		}
		
		boolean tenantGrant = node.get("permissions").get("grantTenant").asBoolean();
		if (tenantGrant) {
			role.addPermission(permissionFactory.create("tenant:grant_permission@" + tenant.getId() + ":" + spaceId));
		}
		
		boolean spaceView = node.get("permissions").get("viewProjects").asBoolean();
		if (spaceView) {
			role.addPermission(permissionFactory.create("space:view_projects@" + tenant.getId() + ":" + spaceId));
		}
		
		boolean spaceManage = node.get("permissions").get("manageProjects").asBoolean();
		if (spaceManage) {
			role.addPermission(permissionFactory.create("space:manage_projects@" + tenant.getId() + ":" + spaceId));
		}
		
		boolean spaceUpdate = node.get("permissions").get("updateSpace").asBoolean();
		if (spaceUpdate) {
			role.addPermission(permissionFactory.create("space:update_space@" + tenant.getId() + ":" + spaceId));
		}
		
		boolean spaceGrant = node.get("permissions").get("grantSpace").asBoolean();
		if (spaceGrant) {
			role.addPermission(permissionFactory.create("space:grant_premission@" + tenant.getId() + ":" + spaceId));
		}
		
		boolean projectManageFunc = node.get("permissions").get("manageFunction").asBoolean();
		if (projectManageFunc) {
			role.addPermission(permissionFactory.create("project:manage_functional@" + tenant.getId() + ":" + spaceId));
		}
		
		boolean projectViewFunc = node.get("permissions").get("viewFunction").asBoolean();
		if (projectViewFunc) {
			role.addPermission(permissionFactory.create("project:view_functional@" + tenant.getId() + ":" + spaceId));
		}
		
		boolean projectUploadSele= node.get("permissions").get("uploadSelenium").asBoolean();
		if (projectUploadSele) {
			role.addPermission(permissionFactory.create("project:upload_selenium@" + tenant.getId() + ":" + spaceId));
		}
		
		boolean projectManageData = node.get("permissions").get("manageData").asBoolean();
		if (projectManageData) {
			role.addPermission(permissionFactory.create("project:manage_data@" + tenant.getId() + ":" + spaceId));
		}
		
		boolean projectManagePerf = node.get("permissions").get("managePerformance").asBoolean();
		if (projectManagePerf) {
			role.addPermission(permissionFactory.create("project:manage_performance@" + tenant.getId() + ":" + spaceId));
		}
		
		boolean projectViewPerf = node.get("permissions").get("viewPerformance").asBoolean();
		if (projectViewPerf) {
			role.addPermission(permissionFactory.create("project:view_performance@" + tenant.getId() + ":" + spaceId));
		}
		boolean projectGrantPerm= node.get("permissions").get("grantPermisson").asBoolean();
		if (projectGrantPerm) {
			role.addPermission(permissionFactory.create("project:grant_permission@" + tenant.getId() + ":" + spaceId));
		}
		
		roleService.create(role);
		
		return status(201, Json.parse(role.toString()));
		
	}

	

}
