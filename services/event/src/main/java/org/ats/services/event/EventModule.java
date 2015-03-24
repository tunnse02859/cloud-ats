/**
 * 
 */
package org.ats.services.event;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import akka.actor.Actor;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Names;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Mar 24, 2015
 */
public class EventModule extends AbstractModule {
  
  /** .*/
  public static final String EVENT_CONF = "ats.cloud.event.conf";
  
  /** .*/
  private Properties configuration;
  
  public EventModule() throws FileNotFoundException, IOException {
    String configPath = System.getProperty(EVENT_CONF);
    buildConfiguration(configPath);
  }
  
  public EventModule(String configPath) throws FileNotFoundException, IOException {
    buildConfiguration(configPath);
  }
  
  private void buildConfiguration(String configPath) throws FileNotFoundException, IOException {
    Properties configuration = new Properties();
    if (configPath != null && !configPath.isEmpty()) configuration.load(new FileInputStream(configPath));
    this.configuration = configuration;
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void configure() {
    Map<Class<? extends Actor>, String> clazzes = new HashMap<Class<? extends Actor>, String>();
    for(Entry<Object, Object> entry : this.configuration.entrySet()) {
      String actorClazz = (String) entry.getKey();
      String actorName = (String) entry.getValue();
      try {
        clazzes.put((Class<? extends Actor>) Class.forName(actorClazz), actorName);
      } catch (ClassNotFoundException e) {
        throw new RuntimeException(e);
      }
    }
    
    bind(new TypeLiteral<Map<Class<? extends Actor>, String>>(){})
      .annotatedWith(Names.named("ats.cloud.event.actors")).toInstance(clazzes);
    
    bind(EventService.class);
    install(new FactoryModuleBuilder().build(new TypeLiteral<EventFactory<String>>(){}));
  }
  
}
