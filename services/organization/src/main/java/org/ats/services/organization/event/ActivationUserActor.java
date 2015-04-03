/**
 * 
 */
package org.ats.services.organization.event;

import org.ats.services.event.Event;
import org.ats.services.organization.ActivationService;
import org.ats.services.organization.UserService;
import org.ats.services.organization.entity.User;
import org.ats.services.organization.entity.fatory.ReferenceFactory;
import org.ats.services.organization.entity.reference.UserReference;

import akka.actor.UntypedActor;

import com.google.inject.Inject;

/**
 * @author TrinhTV3
 *
 * Email: TrinhTV3@fsoft.com.vn
 */
public class ActivationUserActor extends UntypedActor {

  @Inject
  private ActivationService activationService; 
  
  @Inject
  private UserService userService;
  
  @Inject
  private ReferenceFactory<UserReference> userRefFactory;

  @Override
  public void onReceive(Object message) throws Exception {

    if (message instanceof Event) {
      
      Event event = (Event) message;
      if ("inactive-user".equals(event.getName())) {
        
        User user = (User) event.getSource();
        
        UserReference ref = userRefFactory.create(user.getEmail());
        movingProcessing(ref);
        
      } else if ("inactive-ref-user".equals(event.getName())) {
        
        UserReference ref = (UserReference) event.getSource();
        movingProcessing(ref);
      } else if ("active-user".equals(event.getName())) {
        
        User user = (User) event.getSource();
        UserReference ref = userRefFactory.create(user.getEmail());
        restoreProcessing(ref);
        
      } else if ("active-ref-user".equals(event.getName())) {
        UserReference ref = (UserReference) event.getSource();
        restoreProcessing(ref);
        
      } else unhandled(message);
    }
  }

  private void movingProcessing(UserReference ref) {
    
    activationService.moveUser(ref);
    
    userService.delete(ref.getId());
    if (!"deadLetters".equals(getSender().path().name())) {
      getSender().tell(ref, getSelf());
    }
  }
 
  private void restoreProcessing(UserReference ref) {
    
    userService.create(ref.get());
    
    activationService.restoreUser(ref);
    
    if (!"deadLetters".equals(getSender().path().name())) {
      getSender().tell(ref, getSelf());
    }
  }
  
}
