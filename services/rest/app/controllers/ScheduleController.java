/**
 * 
 */
package controllers;



import org.ats.common.PageList;
import org.ats.services.OrganizationContext;
import org.ats.services.organization.acl.Authenticated;
import org.ats.services.schedule.Schedule;
import org.ats.services.schedule.ScheduleFactory;
import org.ats.services.schedule.ScheduleService;

import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import actions.CorsComposition;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.inject.Inject;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 *         Jul 28, 2015
 */
@CorsComposition.Cors
@Authenticated
public class ScheduleController extends Controller {

  @Inject
  OrganizationContext context;

  @Inject
  ScheduleService scheduleSevice;

  @Inject
  ScheduleFactory scheduleFactory;
  

  public Result list(String project_id) {
    PageList<Schedule> list = scheduleSevice.query(new BasicDBObject(
        "project_id", project_id));
    ArrayNode array = Json.newObject().arrayNode();
    
    while (list.hasNext()) {
      for (Schedule schedule : list.next()) {
        schedule.put("hour", schedule.get("hour"));
        schedule.put("minute", schedule.get("minute"));
        schedule.put("day", schedule.get("day"));
        schedule.put("dateRepeat", schedule.get("dateRepeat"));
        schedule.put("options", schedule.get("options"));
        schedule.put("suites", schedule.get("suites"));
        array.add(Json.parse(schedule.toString()));
      }
    }
    return ok(array);
  }

  public Result create(String project_id, String name) {
    JsonNode data = request().body().asJson();
    JsonNode jsonSuites = data.get("suites");
    JsonNode jsonOptions = data.get("options");
    JsonNode jsonSchedule = data.get("schedule");

    BasicDBList listSuites = new BasicDBList();
    for (JsonNode sel : jsonSuites) {
      listSuites.add(new BasicDBObject("_id", sel.asText()));
    }
    BasicDBObject listOptions = new BasicDBObject("browser", jsonOptions.get(
        "browser").asText()).append("browser_version",
        jsonOptions.get("browser_version").asText()).append("selenium_version",
        jsonOptions.get("selenium_version").asText()).append("os",
            jsonOptions.get("os").asText());

    Schedule schedule = scheduleFactory.create(name, project_id);
    schedule.put("suites", listSuites);
    schedule.put("options", listOptions);
    schedule.put("hour", jsonSchedule.get("hour").asText());
    schedule.put("minute", jsonSchedule.get("minute").asText());
    
    if (jsonSchedule.get("day").asText() != null)
      schedule.put("day", jsonSchedule.get("day").asText());

    if (jsonSchedule.get("value_delay").asText() != null)
      schedule.put("value_delay", jsonSchedule.get("value_delay").asText());

    BasicDBList listRepeat = new BasicDBList();
    ArrayNode arrayRepeat = (ArrayNode) Json.parse(jsonSchedule.get("dateRepeat").toString());
    if (arrayRepeat.size() != 0) {
      for (JsonNode repeat : arrayRepeat) {
        listRepeat.add(new BasicDBObject("date", repeat.asText()));
      }
      schedule.put("dateRepeat", listRepeat);
    }

    scheduleSevice.create(schedule);

    return status(201, Json.parse(schedule.toString()));
  }
  
  public Result delete(String projectId, String schedule_id){
    Schedule schedule = scheduleSevice.get(schedule_id);
    if (schedule == null) {
      return status(404);
    }
    scheduleSevice.delete(schedule_id);
    
    return status(200);
  }
  
  public void checkScheduleFunctional(){
    PageList<Schedule> list = scheduleSevice.list();
    
    while (list.hasNext()) {
      for (Schedule schedule : list.next()) {
        System.out.println(schedule.get("hour").toString() + schedule.get("minute").toString());
      }
    }
  }
  
}
