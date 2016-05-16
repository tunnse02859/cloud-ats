package controllers;

import org.ats.services.OrganizationContext;
import org.ats.services.organization.RoleService;
import org.ats.services.organization.UserService;
import org.ats.services.organization.acl.Authenticated;
import org.ats.services.organization.entity.Role;
import org.ats.services.organization.entity.Space;
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
import com.google.inject.Inject;


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
	ReferenceFactory<TenantReference> tenantReference;
	
	@Inject
	ReferenceFactory<SpaceReference> spaceReference;
	
	@Inject
	RoleFactory roleFactory;
	
	@Inject
	PermissionFactory permissionFactory;

	public Result list() {
		
		return ok();
	}


	public Result create() {
		JsonNode node = request().body().asJson();
		String roleName = node.get("name").asText();
		String spaceId = node.get("spaceId").asText();
		boolean tenantView = node.get("tenant").get("view").asBoolean();
		boolean tenantManage = node.get("tenant").get("manage").asBoolean();
		boolean tenantGrant = node.get("tenant").get("grant").asBoolean();
		boolean spaceView = node.get("space").get("view").asBoolean();
		boolean spaceManage = node.get("space").get("manage").asBoolean();
		boolean spaceGrant = node.get("space").get("grant").asBoolean();
		boolean projectView = node.get("project").get("manage").asBoolean();
		boolean projectManageFunc = node.get("project").get("manageFunction").asBoolean();
		boolean projectViewFunc = node.get("project").get("viewFunction").asBoolean();
		boolean projectUploadSele= node.get("project").get("uploadSelenium").asBoolean();
		boolean projectManageData = node.get("project").get("manageData").asBoolean();
		boolean projectManagePerf = node.get("project").get("managePerformance").asBoolean();
		boolean projectViewPerf = node.get("project").get("viewPerformance").asBoolean();
		boolean projectGrantPerm= node.get("project").get("grantPermisson").asBoolean();
		
		
		Role role = roleFactory.create(roleName);
//		role.setSpace(spaceReference.create("spaceId"));
		role.addPermission(permissionFactory.create("isTeantView:"+tenantView));
		
		roleService.create(role);
		
		return status(201, Json.parse(role.toString()));
		
	}

	

}
