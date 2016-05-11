package controllers;

import org.ats.common.PageList;
import org.ats.services.OrganizationContext;
import org.ats.services.organization.SpaceService;
import org.ats.services.organization.UserService;
import org.ats.services.organization.acl.Authenticated;
import org.ats.services.organization.entity.Space;
import org.ats.services.organization.entity.User;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.inject.Inject;
import com.mongodb.BasicDBObject;

import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import actions.CorsComposition;

@CorsComposition.Cors
@Authenticated
public class SpaceController extends Controller {
	
	@Inject OrganizationContext context;
	
	@Inject UserService userService;
	  
	@Inject SpaceService spaceService;
	
	public Result list(){
	  
	  User user = context.getUser();
	  user = userService.get(user.getEmail(), "isTenantAdmin");
	  if (user.getBoolean("isTenantAdmin")) {
	    ArrayNode array = Json.newObject().arrayNode();
	    PageList<Space> spaces = spaceService.query(new BasicDBObject("tenant", new BasicDBObject("_id", context.getTenant().getId())));
	    while (spaces.hasNext()) {
	      for (Space space : spaces.next()) {
	        array.add(Json.parse(space.toString()));
	      }
	    }
	    
	    return ok(array);
	  }
		return ok();
	}

	public Result create() {

		return ok();
	}

	public Result update() {

		return ok();
	}

	public Result delete() {

		return ok();
	}
}
