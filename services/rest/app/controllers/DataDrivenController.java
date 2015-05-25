/**
 * 
 */
package controllers;

import java.util.List;

import org.ats.common.PageList;
import org.ats.services.datadriven.DataDriven;
import org.ats.services.datadriven.DataDrivenService;
import org.ats.services.organization.acl.Authenticated;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.inject.Inject;
import com.mongodb.BasicDBObject;

import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import actions.CorsComposition;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * May 21, 2015
 */
@CorsComposition.Cors
@Authenticated
public class DataDrivenController extends Controller {
  
  @Inject
  DataDrivenService service;
  
  public Result list(String tenant, String space) {
    BasicDBObject query = new BasicDBObject("tenant", new BasicDBObject("_id", tenant));
    query.append("space", new BasicDBObject("space", new BasicDBObject("_id", space)));
    PageList<DataDriven> list = service.query(query);
    ArrayNode array = Json.newObject().arrayNode();
    while(list.hasNext()) {
      List<DataDriven> page = list.next();
      for (DataDriven item : page) {
        array.add(Json.parse(item.toString()));
      }
    }
    return ok(array);
  }
}
