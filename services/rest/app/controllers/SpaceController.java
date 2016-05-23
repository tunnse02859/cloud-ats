package controllers;

import org.ats.common.PageList;
import org.ats.services.OrganizationContext;
import org.ats.services.organization.SpaceService;
import org.ats.services.organization.UserService;
import org.ats.services.organization.acl.Authenticated;
import org.ats.services.organization.entity.Space;
import org.ats.services.organization.entity.User;
import org.ats.services.organization.entity.fatory.ReferenceFactory;
import org.ats.services.organization.entity.fatory.SpaceFactory;
import org.ats.services.organization.entity.reference.SpaceReference;
import org.ats.services.organization.entity.reference.TenantReference;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.inject.Inject;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import actions.CorsComposition;

@CorsComposition.Cors
@Authenticated
public class SpaceController extends Controller {

	@Inject
	OrganizationContext context;

	@Inject
	UserService userService;

	@Inject
	SpaceService spaceService;

	@Inject
	SpaceFactory spaceFactory;

	@Inject
	ReferenceFactory<TenantReference> tenantReference;
	
	@Inject
	ReferenceFactory<SpaceReference> spaceReference;

	public Result list() {
		ArrayNode listSpace = Json.newObject().arrayNode();
		PageList<Space> spaces = spaceService.query(new BasicDBObject("tenant",
				new BasicDBObject("_id", context.getTenant().getId())));
		while (spaces.hasNext()) {
			for (Space space : spaces.next()) {
				BasicDBList listUser = new BasicDBList();
				PageList<User> users = userService.findUsersInSpace(spaceReference.create(space.getId()));
				while(users.hasNext()){
					for (User user : users.next()) {
						user.removeField("password");
						listUser.add(user);
					}
				}
				space.put("listUser", listUser);
				space.put("created_date", space.getDate("created_date")
						.getTime());
				listSpace.add(Json.parse(space.toString()));
			}
		}

		return ok(listSpace);
	}

	public Result update() {
		JsonNode node = request().body().asJson();
		String spaceId = node.get("_id") == null ? null : node.get("_id").asText();
		String spaceName = node.get("name").asText();
		String spaceDesc = node.get("desc").asText();
		JsonNode listUser = node.get("listUser");
		
		Space space ;
	    if (spaceId == null) {
	    	space = spaceFactory.create(spaceName);
	    	space.put("name", spaceName);
	    	space.setDescription(spaceDesc);
	    	space.setTenant(tenantReference.create(context.getTenant().getId()));
			spaceId = space.getId();
			spaceService.create(space);
			for (JsonNode jsonUser : listUser) {
				User user = userService.get(jsonUser.get("_id").asText());
				user.joinSpace(spaceReference.create(spaceId));
				userService.update(user);
			}
		} else {
		  space = spaceService.get(spaceId);
		  space.put("name", spaceName);
	      space.setDescription(spaceDesc);
	      spaceService.update(space);
	      PageList<User> users = userService.findUsersInSpace(spaceReference.create(space.getId()));
		  while (users.hasNext()) {
			for (User user : users.next()) {
				user.leaveSpace(spaceReference.create(spaceId));
				userService.update(user);
			}
		  }
	      for (JsonNode jsonUser : listUser) {
	    	  User user = userService.get(jsonUser.get("_id").asText());
			  user.joinSpace(spaceReference.create(spaceId));
			  userService.update(user);
		  }
	    }
	    return status(201,  Json.parse(space.toString()));
	}

	public Result delete(String spaceId) {
		Space space = spaceService.get(spaceId);
		if (space == null)
			return status(404);
		PageList<User> users = userService.findUsersInSpace(spaceReference.create(space.getId()));
		while(users.hasNext()){
			for (User user : users.next()) {
				user.leaveSpace(spaceReference.create(spaceId));
				userService.update(user);
			}
		}
		spaceService.delete(spaceId);
		return status(200);

	}
}
