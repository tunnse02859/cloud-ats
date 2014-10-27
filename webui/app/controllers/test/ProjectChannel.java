/**
 * 
 */
package controllers.test;

import play.mvc.WebSocket;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Oct 27, 2014
 */
public class ProjectChannel {

  final String sessionId;
  
  final String userId;
  
  final String type;
  
  final WebSocket.Out<JsonNode> out;
  
  public ProjectChannel(String sessionId, String userId, String type, WebSocket.Out<JsonNode> channel) {
    this.sessionId = sessionId;
    this.userId = userId;
    this.type = type;
    this.out = channel;
  }
}
