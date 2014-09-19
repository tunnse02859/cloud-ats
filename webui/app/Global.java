
import listener.DeleteGroupListener;

import org.ats.component.usersmgt.EventExecutor;

import play.Application;
import play.GlobalSettings;
import play.Logger;

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
    EventExecutor.INSTANCE.addListener(new DeleteGroupListener());
    EventExecutor.INSTANCE.start();
    Logger.info("Application has started...");
  }
  
  @Override
  public void onStop(Application app) {
    EventExecutor.INSTANCE.stop();
    Logger.info("Application shutdown...");
  }

}
