package org.ats.services.schedule;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.ats.common.PageList;
import org.ats.services.OrganizationContext;
import org.ats.services.executor.ExecutorService;
import org.ats.services.keyword.KeywordProject;
import org.ats.services.keyword.KeywordProjectService;
import org.ats.services.keyword.Suite;
import org.ats.services.keyword.SuiteFactory;
import org.ats.services.keyword.SuiteReference;
import org.ats.services.keyword.SuiteService;
import org.ats.services.organization.UserService;
import org.ats.services.organization.base.AuthenticationService;
import org.ats.services.organization.entity.User;
import org.ats.services.organization.entity.fatory.ReferenceFactory;
import org.ats.services.project.MixProject;
import org.ats.services.project.MixProjectService;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.inject.Inject;
import com.mongodb.BasicDBObject;

import akka.actor.UntypedActor;


public class ScheduleEventActor extends UntypedActor{
  
  @Inject
  private ScheduleService scheduleService;
  
  @Inject
  private ExecutorService executorService;
  
  @Inject
  private MixProjectService mpService;
  
  @Inject 
  private KeywordProjectService keywordProjectService;
  
  @Inject
  private ReferenceFactory<SuiteReference> suiteRefFactory;
  
  @Inject
  private SuiteService suiteService;
  
  @Inject
  private OrganizationContext context;
  
  @Inject
  private UserService userService;
  
  public void onReceive(Object message) throws Exception {
    unhandled(message);
    SimpleDateFormat fromUser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    SimpleDateFormat myFormat = new SimpleDateFormat("yyyy-MM-dd");
    PageList<Schedule> listSchedules = scheduleService.list();
    Date date = new Date();
    String curTime = date.getHours() + ":" + date.getMinutes();
    String curDay = myFormat.format(date);
    while (listSchedules.hasNext()) {
      for (Schedule schedule : listSchedules.next()) {
        schedule.put("hour", schedule.get("hour"));
        schedule.put("minute", schedule.get("minute"));
        schedule.put("day", schedule.get("day"));
        schedule.put("dateRepeat", schedule.get("dateRepeat"));
        schedule.put("options", schedule.get("options"));
        schedule.put("suites", schedule.get("suites"));
        String expTime = schedule.get("hour").toString() + ":" + schedule.get("minute").toString();
        
        BasicDBObject obj = (BasicDBObject) schedule.get("creator");
        User user = userService.transform(obj);
        context.setUser(user);
        
        if(schedule.get("dateRepeat") !=null){
          JSONArray jsonArray = new JSONArray(schedule.get("dateRepeat").toString()); 
          String currentDayOfWeek =  checkDayOfWeek(date);
          for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonobject = new JSONObject(jsonArray.get(i).toString());
            if(jsonobject.get("date").equals(currentDayOfWeek)){
              if(expTime.equals(curTime)){
                runJob(schedule.get("project_id").toString(), schedule.get("suites").toString(),
                    schedule.get("options").toString());
              }
            }
          }
        } else if(schedule.get("day") !=null) {
          String expDay = myFormat.format(fromUser.parse(schedule.get("day").toString()));
          if(expDay.equals(curDay) && expTime.equals(curTime)){
             runJob(schedule.get("project_id").toString(), schedule.get("suites").toString(),
                 schedule.get("options").toString());
          }
        }
      }
    }
  }
  
  public void runJob(String projectId, String suites, String options) throws Exception {
    JSONArray jsonArray = new JSONArray(suites); 
    JSONObject jsonobject = new JSONObject(options);
    String seleniumVersion = jsonobject.get("selenium_version").toString();
    String browser = jsonobject.get("browser").toString();
    String browserVersion = jsonobject.get("browser_version").toString();
    
    StringBuilder initDriver = new StringBuilder();
    if ("firefox".equals(browser)) {
      initDriver.append("System.setProperty(\"webdriver.firefox.bin\", \"C:\\\\Program Files (x86)\\\\Mozilla Firefox\\\\firefox\");\n");
      initDriver.append("wd = new FirefoxDriver();");
    } else if ("chrome".equals(browser)) {
      initDriver.append("System.setProperty(\"webdriver.chrome.driver\", \"C:/browsersDriver/chromedriver.exe\");\n wd = new ChromeDriver();");
    } else if ("ie".equals(browser)) {
      initDriver.append("System.setProperty(\"webdriver.ie.driver\", \"C:/browsersDriver/IEDriverServer.exe\");\n wd = new InternetExplorerDriver();");
    }
    
    List<SuiteReference> listSuites = new ArrayList<SuiteReference>(jsonArray.length());
    for (int i = 0; i < jsonArray.length(); i++) {
      JSONObject suiteJson = new JSONObject(jsonArray.get(i).toString());
      SuiteReference ref = suiteRefFactory.create(suiteJson.getString("_id"));
      if (initDriver.length() > 0) {
        Suite suite = ref.get();
        suite.put("init_driver", initDriver.toString());
        suiteService.update(suite);
      }
      listSuites.add(ref);
    }
    
    MixProject mp = mpService.get(projectId);
    KeywordProject project = keywordProjectService.get(mp.getKeywordId(), "value_delay");
    
    project.setVersionSelenium(seleniumVersion);
    keywordProjectService.update(project);
    
    BasicDBObject listOptions = new BasicDBObject("browser", browser).append("browser_version", browserVersion).append("selenium_version", seleniumVersion);
    if (project.getStatus() == KeywordProject.Status.READY){
      executorService.execute(project, listSuites, listOptions);
    }
  }
  
  public String checkDayOfWeek(Date date){
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
    String day = "";
    switch (dayOfWeek) {
      case Calendar.MONDAY:
        day = "Monday";
        break;
      case Calendar.TUESDAY:
        day = "Tuesday";
        break;
      case Calendar.WEDNESDAY:
        day = "Wednesday";
        break;
      case Calendar.THURSDAY:
        day = "Thursday";
        break;
      case Calendar.FRIDAY:
        day = "Friday";
        break;
      case Calendar.SATURDAY:
        day = "Saturday";
        break;
      case Calendar.SUNDAY:
        day = "Sunday";
        break;
    }
    return day;
  }
  
}
