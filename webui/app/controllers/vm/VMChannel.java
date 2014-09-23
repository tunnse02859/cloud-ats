/**
 * 
 */
package controllers.vm;

import play.mvc.WebSocket;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Sep 22, 2014
 */
class VMChannel {
  
  final String sessionId;
  
  final String groupId;
  
  final WebSocket.Out<JsonNode> out;
  
  public VMChannel(String sessionId, String groupId, WebSocket.Out<JsonNode> channel) {
    this.sessionId = sessionId;
    this.groupId = groupId;
    this.out = channel;
  }
}
