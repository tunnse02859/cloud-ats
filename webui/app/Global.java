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
    Logger.info("Application has started... hehe");
  }
  
  @Override
  public void onStop(Application app) {
    Logger.info("Application shutdown...");
  }
}
