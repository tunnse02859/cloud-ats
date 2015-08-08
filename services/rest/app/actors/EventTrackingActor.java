/**
 * 
 */
package actors;

import org.ats.services.event.Event;
import org.ats.services.executor.job.KeywordJob;
import org.ats.services.executor.job.PerformanceJob;
import org.ats.services.keyword.KeywordProject;
import org.ats.services.keyword.KeywordProjectService;
import org.ats.services.performance.PerformanceProject;
import org.ats.services.performance.PerformanceProjectService;

import akka.actor.UntypedActor;

import com.google.inject.Inject;

import controllers.EventController;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Aug 8, 2015
 */
public class EventTrackingActor extends UntypedActor {

  @Inject EventController eventController;

  @Inject KeywordProjectService keywordService;
  
  @Inject PerformanceProjectService perfService;
  
  @Override
  public void onReceive(Object obj) throws Exception {
    if (obj instanceof Event) {
      Event event = (Event) obj;
      if ("keyword-job-tracking".equals(event.getName())) {

        KeywordJob job = (KeywordJob) event.getSource();
        KeywordProject project = keywordService.get(job.getProjectId());
        job.put("project_status", project.getStatus().toString());
        eventController.send(project.getCreator().get(), job);
        
      } else if ("performance-job-tracking".equals(event.getName())) {
        
        PerformanceJob job = (PerformanceJob) event.getSource();
        PerformanceProject project = perfService.get(job.getId());
        eventController.send(project.getCreator().get(), job);
        
      }
    }
  }


}
