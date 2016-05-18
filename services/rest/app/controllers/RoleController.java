package controllers;

import java.util.ArrayList;
import java.util.List;

import org.ats.common.PageList;
import org.ats.services.OrganizationContext;
import org.ats.services.organization.RoleService;
import org.ats.services.organization.SpaceService;
import org.ats.services.organization.UserService;
import org.ats.services.organization.acl.Authenticated;
import org.ats.services.organization.entity.Role;
import org.ats.services.organization.entity.Space;
import org.ats.services.organization.entity.Tenant;
import org.ats.services.organization.entity.User;
import org.ats.services.organization.entity.fatory.PermissionFactory;
import org.ats.services.organization.entity.fatory.ReferenceFactory;
import org.ats.services.organization.entity.fatory.RoleFactory;
import org.ats.services.organization.entity.reference.RoleReference;
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
	ReferenceFactory<RoleReference> roleReferenceFactory;
	
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
	
	public Result listUser(String roleId) {
		ArrayNode array = Json.newObject().arrayNode();
		
		PageList<User> users = userService.findIn("roles", roleReferenceFactory.create(roleId));
		
		while(users.hasNext()){
			for (User user : users.next()) {
				array.add(Json.parse(user.toString()));
			}
		}
		return ok(array);
	}
	
	public Result get(String roleId) {
		
		Role role = roleService.get(roleId);
		return ok(Json.parse(role.toString()));
	}
	public Result delete(String roleId){
		roleService.delete(roleId);
		PageList<User> users = userService.findIn("roles", roleReferenceFactory.create(roleId));
		List<User> listUser = new ArrayList<User>();
		while(users.hasNext()){
			for (User user : users.next()) {
				user.removeRole(roleReferenceFactory.create(roleId));
				listUser.add(user);
			}
		}
		for (User user : listUser) {
			userService.update(user);
		}
		return status(201);
	}

	public Result update() {
		
		JsonNode node = request().body().asJson();
		String roleName = node.get("name").asText();
		String spaceId = node.get("space").get("_id").asText();
		JsonNode array = node.get("listUser");
		Tenant tenant = context.getTenant();
		String roleId = node.get("_id") == null ? null : node.get("_id").asText();
		
		Role role;
		if (roleId == null) {
			role = roleFactory.create(roleName);
			role.setSpace(spaceReference.create(spaceId));
			roleId = role.getId();
		} else {
			role = roleService.get(roleId);
		}
		for (JsonNode jsonNode : array) {
			User user = userService.get(jsonNode.get("_id").asText());
			user.addRole(roleReferenceFactory.create(role.getId()));
			userService.update(user);
		}
		
		boolean tenantView = node.get("permissions").get("viewSpaces").asBoolean();
		if (tenantView && !role.hasPermisison(permissionFactory.create("tenant:view_spaces@" + tenant.getId() + ":" + spaceId))) {
			role.addPermission(permissionFactory.create("tenant:view_spaces@" + tenant.getId() + ":" + spaceId));
		}
		
		boolean tenantManage = node.get("permissions").get("manageSpaces").asBoolean();
		if (tenantManage && !role.hasPermisison(permissionFactory.create("tenant:manage_spaces@" + tenant.getId() + ":" + spaceId))) {
			role.addPermission(permissionFactory.create("tenant:manage_spaces@" + tenant.getId() + ":" + spaceId));
		}
		
		boolean tenantGrant = node.get("permissions").get("grantTenant").asBoolean();
		if (tenantGrant && !role.hasPermisison(permissionFactory.create("tenant:grant_permission@" + tenant.getId() + ":" + spaceId))) {
			role.addPermission(permissionFactory.create("tenant:grant_permission@" + tenant.getId() + ":" + spaceId));
		}
		
		boolean spaceView = node.get("permissions").get("viewProjects").asBoolean();
		if (spaceView && !role.hasPermisison(permissionFactory.create("space:view_projects@" + tenant.getId() + ":" + spaceId))) {
			role.addPermission(permissionFactory.create("space:view_projects@" + tenant.getId() + ":" + spaceId));
		}
		
		boolean spaceManage = node.get("permissions").get("manageProjects").asBoolean();
		if (spaceManage && !role.hasPermisison(permissionFactory.create("space:manage_projects@" + tenant.getId() + ":" + spaceId))) {
			role.addPermission(permissionFactory.create("space:manage_projects@" + tenant.getId() + ":" + spaceId));
		}
		
		boolean spaceUpdate = node.get("permissions").get("updateSpace").asBoolean();
		if (spaceUpdate && !role.hasPermisison(permissionFactory.create("space:update_space@" + tenant.getId() + ":" + spaceId))) {
			role.addPermission(permissionFactory.create("space:update_space@" + tenant.getId() + ":" + spaceId));
		}
		
		boolean spaceGrant = node.get("permissions").get("grantSpace").asBoolean();
		if (spaceGrant  && !role.hasPermisison(permissionFactory.create("space:grant_premission@" + tenant.getId() + ":" + spaceId))) {
			role.addPermission(permissionFactory.create("space:grant_premission@" + tenant.getId() + ":" + spaceId));
		}
		
		boolean projectManageFunc = node.get("permissions").get("manageFunction").asBoolean();
		if (projectManageFunc  && !role.hasPermisison(permissionFactory.create("project:manage_functional@" + tenant.getId() + ":" + spaceId))) {
			role.addPermission(permissionFactory.create("project:manage_functional@" + tenant.getId() + ":" + spaceId));
		}
		
		boolean projectViewFunc = node.get("permissions").get("viewFunction").asBoolean();
		if (projectViewFunc  && !role.hasPermisison(permissionFactory.create("project:view_functional@" + tenant.getId() + ":" + spaceId))) {
			role.addPermission(permissionFactory.create("project:view_functional@" + tenant.getId() + ":" + spaceId));
		}
		
		boolean projectUploadSele= node.get("permissions").get("uploadSelenium").asBoolean();
		if (projectUploadSele  && !role.hasPermisison(permissionFactory.create("project:upload_selenium@" + tenant.getId() + ":" + spaceId))) {
			role.addPermission(permissionFactory.create("project:upload_selenium@" + tenant.getId() + ":" + spaceId));
		}
		
		boolean projectManagePerf = node.get("permissions").get("managePerformance").asBoolean();
		if (projectManagePerf  && !role.hasPermisison(permissionFactory.create("project:manage_performance@" + tenant.getId() + ":" + spaceId))) {
			role.addPermission(permissionFactory.create("project:manage_performance@" + tenant.getId() + ":" + spaceId));
		}
		
		boolean projectViewPerf = node.get("permissions").get("viewPerformance").asBoolean();
		if (projectViewPerf  && !role.hasPermisison(permissionFactory.create("project:view_performance@" + tenant.getId() + ":" + spaceId))) {
			role.addPermission(permissionFactory.create("project:view_performance@" + tenant.getId() + ":" + spaceId));
		}
		boolean projectGrantPerm= node.get("permissions").get("grantPermisson").asBoolean();
		if (projectGrantPerm  && !role.hasPermisison(permissionFactory.create("project:grant_permission@" + tenant.getId() + ":" + spaceId))) {
			role.addPermission(permissionFactory.create("project:grant_permission@" + tenant.getId() + ":" + spaceId));
		}
		if (roleId == null) {
			roleService.create(role);
		} else {
			roleService.update(role);
		}
		
		
		return status(201, Json.parse(role.toString()));
		
	}

	

}
