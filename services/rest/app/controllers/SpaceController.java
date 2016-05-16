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
import org.ats.services.organization.entity.reference.TenantReference;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
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

	public Result list() {
		ArrayNode array = Json.newObject().arrayNode();
		PageList<Space> spaces = spaceService.query(new BasicDBObject("tenant",
				new BasicDBObject("_id", context.getTenant().getId())));
		while (spaces.hasNext()) {
			for (Space space : spaces.next()) {
				array.add(Json.parse(space.toString()));
			}
		}

		return ok(array);
	}
	public Result listUser() {
		ArrayNode listUser = Json.newObject().arrayNode();
		PageList<User> users = userService.query(new BasicDBObject(
				"tenant", new BasicDBObject("_id", context.getTenant().getId())));
		while (users.hasNext()){
			for (User us : users.next()) {
				listUser.add(Json.parse(us.toString()));
			}
		}
		return ok(listUser);
	}

	public Result get(String spaceId) {
		ArrayNode array = Json.newObject().arrayNode();
		PageList<Space> spaces = spaceService.query(new BasicDBObject("_id",
				spaceId));
		while (spaces.hasNext()) {
			for (Space space : spaces.next()) {
				space.put("created_date", space.getDate("created_date")
						.getTime());
				array.add(Json.parse(space.toString()));
			}
		}

		return ok(array);
	}

	public Result create() {
		JsonNode node = request().body().asJson();
		String spaceName = node.get("name").asText();
		String desc = node.get("desc").asText();
		Space space = spaceFactory.create(spaceName);
		space.setDescription(desc);
		space.setTenant(tenantReference.create(context.getTenant().getId()));
		spaceService.create(space);
		return status(201, Json.parse(space.toString()));
	}

	public Result update() {
		JsonNode node = request().body().asJson();
	    Space space = spaceService.get(node.get("_id").asText());
	    if (space == null) return status(404);
	    else {
	      String name = node.get("name").asText();
	      String desc = node.get("desc").asText();
	      space.put("name", name);
	      space.setDescription(desc);
	      spaceService.update(space);
	    }
	    return status(200);
	}

	public Result delete(String spaceId) {
		System.out.println("ID:"+spaceId);
		Space space = spaceService.get(spaceId);
		if (space == null)
			return status(404);
		spaceService.delete(spaceId);
		return status(200);

	}
}
