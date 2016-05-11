package controllers;

import org.ats.services.OrganizationContext;
import org.ats.services.organization.UserService;
import org.ats.services.organization.acl.Authenticated;

import com.google.inject.Inject;

import play.mvc.Controller;
import play.mvc.Result;
import actions.CorsComposition;

@CorsComposition.Cors
@Authenticated
public class SpaceController extends Controller {
	
	@Inject OrganizationContext context;
	@Inject UserService userService;
	  
	public Result list(){

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
