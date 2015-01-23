
import helpertest.JenkinsJobExecutor;
import listener.DeleteGroupListener;

import org.ats.component.usersmgt.EventExecutor;

import controllers.vm.VMLogActor;
import controllers.vm.VMStatusActor;
import play.Application;
import play.GlobalSettings;
import play.Logger;
import play.Play;

/**
 * 
 */

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Jul 31, 2014
 */
public class Global extends GlobalSettings {
  

  @Override
  public void onStart(Application app) {
    String dbName = Play.application().configuration().getString("dbName");
    EventExecutor.getInstance(dbName).addListener(new DeleteGroupListener());
    EventExecutor.getInstance(dbName).start();
    JenkinsJobExecutor.getInstance().start();
    Logger.info("Application has started...");
    VMStatusActor.start();
    VMLogActor.start();
  }
  
  @Override
  public void onStop(Application app) {
    String dbName = Play.application().configuration().getString("dbName");
    EventExecutor.getInstance(dbName).stop();
    JenkinsJobExecutor.getInstance().stop();
    Logger.info("Application shutdown...");
    VMStatusActor.stop();
    VMLogActor.stop();
  }

}
